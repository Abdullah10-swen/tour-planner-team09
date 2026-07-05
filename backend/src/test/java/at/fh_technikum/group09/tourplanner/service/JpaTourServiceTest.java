package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.integration.openroute.OpenRouteTourRouteEnrichment;
import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.service.Impl.JpaTourService;
import at.fh_technikum.group09.tourplanner.service.exception.TourNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaTourServiceTest {

    @Mock private TourDal tourDal;
    @Mock private TourLogDal tourLogDal;
    @Mock private OpenRouteTourRouteEnrichment routeEnrichment;

    @InjectMocks
    private JpaTourService service;

    private TourEntity tour(long id, String name, String desc, String from, String to, String transport) {
        TourEntity e = new TourEntity();
        e.setId(id);
        e.setUserId(1L);
        e.setName(name);
        e.setDescription(desc);
        e.setFromLocation(from);
        e.setToLocation(to);
        e.setTransportType(transport);
        return e;
    }

    private TourLogEntity log(int difficulty, double time, double dist) {
        TourLogEntity l = new TourLogEntity();
        l.setDifficulty(difficulty);
        l.setTotalTime(time);
        l.setTotalDistance(dist);
        l.setRating(3);
        l.setComment("test");
        return l;
    }

    // ── Full-Text-Search ──────────────────────────────────────────────

    /** Leere Suche → alle Touren zurückgeben */
    @Test
    void searchTours_returnsAllTours_whenQueryIsNull() {
        TourEntity e = tour(1L, "Alpine Tour", "desc", "Wien", "Graz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of());

        assertThat(service.searchTours(null, 1L)).hasSize(1);
    }

    /** Suche nach Tour-Name */
    @Test
    void searchTours_matchesByName() {
        TourEntity e = tour(1L, "Alpine Tour", "desc", "Wien", "Graz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of());

        List<Tour> result = service.searchTours("alpine", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alpine Tour");
    }

    /** Suche nach Kommentar im TourLog */
    @Test
    void searchTours_matchesByLogComment() {
        TourEntity e = tour(1L, "Tour A", "desc", "Wien", "Linz", "hike");
        TourLogEntity l = log(1, 2.0, 10.0);
        l.setComment("Ausflug mit Kindern");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of(l));

        assertThat(service.searchTours("kindern", 1L)).hasSize(1);
    }

    /** Suche nach dem berechneten Popularity-Label */
    @Test
    void searchTours_matchesByPopularityLabel() {
        // 0 Logs → Popularity-Label = "unpopular"
        TourEntity e = tour(1L, "Tour A", "desc", "Wien", "Linz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of());

        assertThat(service.searchTours("unpopular", 1L)).hasSize(1);
    }

    /** Suche nach dem berechneten ChildFriendliness-Label */
    @Test
    void searchTours_matchesByChildFriendlinessLabel() {
        // difficulty=1, time=1h, dist=5km → Score > 0.67 → "very child-friendly"
        TourEntity e = tour(1L, "Tour A", "desc", "Wien", "Linz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of(log(1, 1.0, 5.0)));

        assertThat(service.searchTours("very child-friendly", 1L)).hasSize(1);
    }

    /** Suche ist case-insensitiv */
    @Test
    void searchTours_isCaseInsensitive() {
        TourEntity e = tour(1L, "ALPINE TOUR", "desc", "Wien", "Graz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of());

        assertThat(service.searchTours("alpine tour", 1L)).hasSize(1);
    }

    /** Kein Treffer → leere Liste */
    @Test
    void searchTours_returnsEmpty_whenNoMatch() {
        TourEntity e = tour(1L, "Tour A", "desc", "Wien", "Linz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of());

        assertThat(service.searchTours("xyz123notfound", 1L)).isEmpty();
    }

    // ── Berechnetes Attribut: Popularity ─────────────────────────────

    /** Popularity = Anzahl der TourLogs */
    @Test
    void getAllTours_setsPopularityFromLogCount() {
        TourEntity e = tour(1L, "Tour A", "desc", "Wien", "Graz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(
                List.of(log(1, 2.0, 5.0), log(2, 3.0, 10.0), log(1, 1.0, 3.0))
        );

        assertThat(service.getAllTours(1L).get(0).getPopularity()).isEqualTo(3);
    }

    // ── Berechnetes Attribut: ChildFriendliness ───────────────────────

    /** Keine Logs → ChildFriendliness = 0.0 */
    @Test
    void getAllTours_childFriendlinessIsZero_whenNoLogs() {
        TourEntity e = tour(1L, "Tour A", "desc", "Wien", "Graz", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of());

        assertThat(service.getAllTours(1L).get(0).getChildFriendliness()).isEqualTo(0.0);
    }

    /** difficulty=1, time=1h, dist=5km → Score > 0.67 */
    @Test
    void getAllTours_childFriendliness_isHighForEasyShortTour() {
        TourEntity e = tour(1L, "Easy Tour", "desc", "A", "B", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of(log(1, 1.0, 5.0)));

        assertThat(service.getAllTours(1L).get(0).getChildFriendliness()).isGreaterThan(0.67);
    }

    /** difficulty=3, time=8h, dist=30km → Score = 0.0 */
    @Test
    void getAllTours_childFriendliness_isZeroForHardLongTour() {
        TourEntity e = tour(1L, "Hard Tour", "desc", "A", "B", "hike");
        when(tourDal.findAllByUserId(1L)).thenReturn(List.of(e));
        when(tourLogDal.findByTourIdOrderByDateTimeAsc(1L)).thenReturn(List.of(log(3, 8.0, 30.0)));

        assertThat(service.getAllTours(1L).get(0).getChildFriendliness()).isEqualTo(0.0);
    }

    // ── CRUD Fehlerbehandlung ────────────────────────────────────────

    /** Tour nicht gefunden → TourNotFoundException */
    @Test
    void getTourById_throwsTourNotFoundException_whenMissing() {
        when(tourDal.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTourById(99L, 1L))
                .isInstanceOf(TourNotFoundException.class)
                .hasMessageContaining("99");
    }

    /** Tour löschen wenn nicht vorhanden → TourNotFoundException */
    @Test
    void deleteTour_throwsTourNotFoundException_whenMissing() {
        when(tourDal.existsByIdAndUserId(99L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteTour(99L, 1L))
                .isInstanceOf(TourNotFoundException.class);
    }
}
