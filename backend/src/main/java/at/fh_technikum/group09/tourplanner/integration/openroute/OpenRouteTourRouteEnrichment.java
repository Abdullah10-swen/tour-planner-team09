package at.fh_technikum.group09.tourplanner.integration.openroute;

import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dto.LocationSuggestionDto;
import at.fh_technikum.group09.tourplanner.dto.RoutePreviewDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Ruft OpenRouteService Geocoding + Directions auf und setzt Distanz (km), geschätzte Zeit (h)
 * und {@link TourEntity#setRouteInfo} (GeoJSON-Antwort) auf der Tour, sofern ein API-Key konfiguriert ist.
 */
@Component
public class OpenRouteTourRouteEnrichment {

    private static final Logger log = LoggerFactory.getLogger(OpenRouteTourRouteEnrichment.class);

    /** application/geo+json – korrekte Accept-Header für den ORS /geojson-Endpoint. */
    private static final MediaType GEO_JSON = MediaType.valueOf("application/geo+json");

    private final String apiKey;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    /** Focus-point parameters appended to every geocoding request (biases results, no hard restriction). */
    private final String geocodeFocusParams;

    public OpenRouteTourRouteEnrichment(
            @Value("${openrouteservice.api.key:}") String apiKey,
            @Value("${openrouteservice.base-url:https://api.openrouteservice.org}") String baseUrl,
            @Value("${openrouteservice.geocode.focus.lat:48.2082}") double focusLat,
            @Value("${openrouteservice.geocode.focus.lon:16.3738}") double focusLon) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(12));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl.replaceAll("/+$", ""))
                .requestFactory(factory)
                .build();
        this.geocodeFocusParams = String.format(Locale.US,
                "&focus.point.lat=%f&focus.point.lon=%f", focusLat, focusLon);
    }

    // -------------------------------------------------------------------------
    // Tour-Enrichment (beim Speichern)
    // -------------------------------------------------------------------------

    /**
     * Überschreibt Distanz, Zeit und Routen-GeoJSON aus ORS.
     * Die zwei Geocoding-Calls laufen parallel; bei Fehler bleiben die Werte unverändert.
     */
    public void enrichIfPossible(TourEntity tour) {
        if (apiKey.isEmpty()) return;
        String from = tour.getFromLocation();
        String to   = tour.getToLocation();
        if (from == null || from.isBlank() || to == null || to.isBlank()) return;

        // Route already provided by the frontend preview – skip the redundant API call.
        if (tour.getRouteInfo() != null && !tour.getRouteInfo().isBlank()) {
            log.debug("enrichIfPossible: routeInfo already set, skipping ORS call.");
            return;
        }

        String profile = mapTransportToOrsProfile(tour.getTransportType());
        try {
            // Geocoding parallel ausführen
            CompletableFuture<Optional<double[]>> startFuture =
                    CompletableFuture.supplyAsync(() -> resolveCoordinates(from.trim()));
            CompletableFuture<Optional<double[]>> endFuture =
                    CompletableFuture.supplyAsync(() -> resolveCoordinates(to.trim()));

            Optional<double[]> start = startFuture.get(14, TimeUnit.SECONDS);
            Optional<double[]> end   = endFuture.get(14, TimeUnit.SECONDS);

            if (start.isEmpty() || end.isEmpty()) {
                log.warn("ORS enrichIfPossible: Geocoding lieferte keine Koordinaten.");
                return;
            }
            callDirections(start.get(), end.get(), profile).ifPresent(r -> {
                tour.setDistance(r.getDistance());
                tour.setEstimatedTime(r.getEstimatedTime());
                tour.setRouteInfo(r.getRouteInfo());
            });
        } catch (Exception ex) {
            log.warn("ORS enrichIfPossible fehlgeschlagen: {}", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Autocomplete-Suche
    // -------------------------------------------------------------------------

    /**
     * Returns up to 5 geocoding suggestions for the given free-text query.
     */
    public List<LocationSuggestionDto> searchLocations(String text) {
        if (apiKey.isEmpty() || text == null || text.isBlank()) return List.of();
        String encoded = UriUtils.encodeQueryParam(text.trim(), StandardCharsets.UTF_8);
        String uri = "/geocode/search?text=" + encoded + "&size=5" + geocodeFocusParams;
        try {
            String responseBody = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", apiKey)
                    .retrieve()
                    .body(String.class);
            if (responseBody == null || responseBody.isBlank()) return List.of();

            JsonNode features = objectMapper.readTree(responseBody).path("features");
            if (!features.isArray()) return List.of();

            List<LocationSuggestionDto> suggestions = new ArrayList<>();
            for (JsonNode f : features) {
                String label = f.path("properties").path("label").asText(null);
                JsonNode coords = f.path("geometry").path("coordinates");
                if (label == null || !coords.isArray() || coords.size() < 2) continue;
                suggestions.add(new LocationSuggestionDto(label, coords.get(0).asDouble(), coords.get(1).asDouble()));
            }
            return suggestions;
        } catch (Exception ex) {
            log.debug("Geocoding-Suche fehlgeschlagen für '{}': {}", text, ex.getMessage());
            return List.of();
        }
    }

    // -------------------------------------------------------------------------
    // Routen-Vorschau
    // -------------------------------------------------------------------------

    /**
     * Schnelle Variante: Koordinaten sind bereits bekannt (aus dem Autocomplete-Dropdown).
     * Kein Geocoding nötig – nur ein einziger HTTP-Call.
     */
    public Optional<RoutePreviewDto> previewRouteByCoordinates(
            double fromLon, double fromLat, double toLon, double toLat, String transport) {
        if (apiKey.isEmpty()) return Optional.empty();
        return callDirections(
                new double[]{fromLon, fromLat},
                new double[]{toLon, toLat},
                mapTransportToOrsProfile(transport));
    }

    /**
     * Fallback: Adressen als Freitext – führt zwei Geocoding-Calls parallel durch.
     */
    public Optional<RoutePreviewDto> previewRoute(String from, String to, String transport) {
        if (apiKey.isEmpty() || from == null || from.isBlank() || to == null || to.isBlank()) {
            return Optional.empty();
        }
        String profile = mapTransportToOrsProfile(transport);
        try {
            CompletableFuture<Optional<double[]>> startFuture =
                    CompletableFuture.supplyAsync(() -> resolveCoordinates(from.trim()));
            CompletableFuture<Optional<double[]>> endFuture =
                    CompletableFuture.supplyAsync(() -> resolveCoordinates(to.trim()));

            Optional<double[]> start = startFuture.get(14, TimeUnit.SECONDS);
            Optional<double[]> end   = endFuture.get(14, TimeUnit.SECONDS);

            if (start.isEmpty() || end.isEmpty()) {
                log.warn("ORS previewRoute: Geocoding lieferte keine Koordinaten.");
                return Optional.empty();
            }
            return callDirections(start.get(), end.get(), profile);
        } catch (Exception ex) {
            log.warn("ORS previewRoute fehlgeschlagen: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------
    // Interner Directions-Call
    // -------------------------------------------------------------------------

    /**
     * Ein einzelner Directions-Call. s = [lon, lat] Start; e = [lon, lat] Ziel.
     * Verwendet Accept: application/geo+json (Pflicht für den /geojson-Endpoint).
     */
    private Optional<RoutePreviewDto> callDirections(double[] s, double[] e, String profile) {
        String coordinatesJson = String.format(Locale.US,
                "[[%f,%f],[%f,%f]]", s[0], s[1], e[0], e[1]);
        String requestBody = "{\"coordinates\":" + coordinatesJson + "}";
        try {
            String responseBody = restClient.post()
                    .uri("/v2/directions/{profile}/geojson", profile)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(GEO_JSON)
                    .header("Authorization", apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) return Optional.empty();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode features = root.path("features");
            if (!features.isArray() || features.isEmpty()) {
                log.warn("ORS Directions: Antwort ohne features.");
                return Optional.empty();
            }
            JsonNode summary = features.get(0).path("properties").path("summary");
            if (!summary.has("distance") || !summary.has("duration")) {
                log.warn("ORS Directions: kein summary.distance/duration.");
                return Optional.empty();
            }
            double distanceKm = summary.get("distance").asDouble() / 1000.0;
            double estimatedTimeH = summary.get("duration").asDouble() / 3600.0;
            return Optional.of(new RoutePreviewDto(distanceKm, estimatedTimeH, responseBody));
        } catch (RestClientException ex) {
            log.warn("ORS Directions fehlgeschlagen: {}", ex.getMessage());
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("ORS Directions Parse-Fehler: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    static String mapTransportToOrsProfile(String transportType) {
        if (transportType == null) return "foot-hiking";
        return switch (transportType.toLowerCase(Locale.ROOT)) {
            case "bike"     -> "cycling-regular";
            case "run"      -> "foot-walking";
            case "vacation" -> "driving-car";
            default         -> "foot-hiking";
        };
    }

    private Optional<double[]> resolveCoordinates(String text) {
        Optional<double[]> direct = tryParseLatLonPair(text);
        return direct.isPresent() ? direct : geocode(text);
    }

    /** Zwei Zahlen komma-getrennt (lat,lon) → ORS [lon,lat]. */
    private Optional<double[]> tryParseLatLonPair(String text) {
        String[] parts = text.split(",");
        if (parts.length != 2) return Optional.empty();
        try {
            double a = Double.parseDouble(parts[0].trim());
            double b = Double.parseDouble(parts[1].trim());
            if (Math.abs(a) > 90 || Math.abs(b) > 180) return Optional.empty();
            return Optional.of(new double[]{b, a}); // ORS: [lon, lat]
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Optional<double[]> geocode(String text) {
        String encoded = UriUtils.encodeQueryParam(text, StandardCharsets.UTF_8);
        String uri = "/geocode/search?text=" + encoded + "&size=1" + geocodeFocusParams;
        try {
            String responseBody = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", apiKey)
                    .retrieve()
                    .body(String.class);
            if (responseBody == null || responseBody.isBlank()) return Optional.empty();

            JsonNode features = objectMapper.readTree(responseBody).path("features");
            if (!features.isArray() || features.isEmpty()) return Optional.empty();

            JsonNode coords = features.get(0).path("geometry").path("coordinates");
            if (!coords.isArray() || coords.size() < 2) return Optional.empty();
            return Optional.of(new double[]{coords.get(0).asDouble(), coords.get(1).asDouble()});
        } catch (Exception ex) {
            log.debug("Geocoding fehlgeschlagen für '{}': {}", text, ex.getMessage());
            return Optional.empty();
        }
    }
}
