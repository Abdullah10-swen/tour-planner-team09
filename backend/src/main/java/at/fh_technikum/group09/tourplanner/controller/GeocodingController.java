package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.dto.LocationSuggestionDto;
import at.fh_technikum.group09.tourplanner.dto.RoutePreviewDto;
import at.fh_technikum.group09.tourplanner.integration.openroute.OpenRouteTourRouteEnrichment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class GeocodingController {

    private final OpenRouteTourRouteEnrichment enrichment;

    public GeocodingController(OpenRouteTourRouteEnrichment enrichment) {
        this.enrichment = enrichment;
    }

    /**
     * Gibt bis zu 5 Adress-Vorschläge für die Eingabe zurück.
     * GET /api/geocode/search?q=Wien
     */
    @GetMapping("/geocode/search")
    public List<LocationSuggestionDto> searchLocations(@RequestParam("q") String q) {
        return enrichment.searchLocations(q);
    }

    /**
     * Berechnet Route zwischen zwei Punkten und gibt Distanz, Zeit und GeoJSON zurück.
     *
     * Schnelle Variante (Koordinaten bekannt – kein Geocoding nötig):
     *   GET /api/route-preview?fromLon=16.37&fromLat=48.21&toLon=15.44&toLat=47.07&transport=hike
     *
     * Fallback (Freitext – zwei Geocoding-Calls parallel):
     *   GET /api/route-preview?from=Wien&to=Graz&transport=hike
     */
    @GetMapping("/route-preview")
    public ResponseEntity<RoutePreviewDto> previewRoute(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double fromLon,
            @RequestParam(required = false) Double fromLat,
            @RequestParam(required = false) Double toLon,
            @RequestParam(required = false) Double toLat,
            @RequestParam(defaultValue = "hike") String transport) {

        // Koordinaten-Pfad: nur ein einziger HTTP-Call nötig
        if (fromLon != null && fromLat != null && toLon != null && toLat != null) {
            return enrichment.previewRouteByCoordinates(fromLon, fromLat, toLon, toLat, transport)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        }

        // Freitext-Pfad als Fallback
        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            return enrichment.previewRoute(from, to, transport)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        }

        return ResponseEntity.badRequest().build();
    }
}
