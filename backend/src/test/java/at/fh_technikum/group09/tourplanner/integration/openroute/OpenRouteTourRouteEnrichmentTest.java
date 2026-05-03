package at.fh_technikum.group09.tourplanner.integration.openroute;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenRouteTourRouteEnrichmentTest {

    @Test
    void mapTransportToOrsProfile_mapsKnownTypes() {
        assertEquals("foot-hiking", OpenRouteTourRouteEnrichment.mapTransportToOrsProfile("hike"));
        assertEquals("cycling-regular", OpenRouteTourRouteEnrichment.mapTransportToOrsProfile("bike"));
        assertEquals("foot-walking", OpenRouteTourRouteEnrichment.mapTransportToOrsProfile("run"));
        assertEquals("driving-car", OpenRouteTourRouteEnrichment.mapTransportToOrsProfile("vacation"));
    }

    @Test
    void mapTransportToOrsProfile_defaultsForUnknown() {
        assertEquals("foot-hiking", OpenRouteTourRouteEnrichment.mapTransportToOrsProfile("other"));
        assertEquals("foot-hiking", OpenRouteTourRouteEnrichment.mapTransportToOrsProfile(null));
    }
}
