package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.Impl.JpaTourLogService;
import at.fh_technikum.group09.tourplanner.service.exception.TourLogNotFoundException;
import at.fh_technikum.group09.tourplanner.service.exception.TourNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaTourLogServiceTest {

    @Mock private TourLogDal tourLogDal;
    @Mock private TourDal tourDal;

    @InjectMocks
    private JpaTourLogService service;

    /** Tour gehört nicht dem User → TourNotFoundException */
    @Test
    void findAllByTourId_throwsTourNotFound_whenTourDoesNotBelongToUser() {
        when(tourDal.existsByIdAndUserId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.findAllByTourId(10L, 1L))
                .isInstanceOf(TourNotFoundException.class);
    }

    /** Logs korrekt zurückgeben */
    @Test
    void findAllByTourId_returnsLogs_whenTourExists() {
        when(tourDal.existsByIdAndUserId(10L, 1L)).thenReturn(true);

        TourLogEntity e = new TourLogEntity();
        e.setId(100L);
        e.setComment("Schöner Tag");
        e.setDifficulty(2);
        e.setTotalDistance(15.0);
        e.setTotalTime(3.0);
        e.setRating(4);
        e.setDateTime(LocalDateTime.now());

        when(tourLogDal.findByTourIdOrderByDateTimeAsc(10L)).thenReturn(List.of(e));

        List<TourLog> result = service.findAllByTourId(10L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComment()).isEqualTo("Schöner Tag");
    }

    /** Tour nicht vorhanden → TourNotFoundException beim Erstellen */
    @Test
    void create_throwsTourNotFound_whenTourMissing() {
        when(tourDal.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        TourLog log = new TourLog();
        log.setComment("Test");
        log.setDifficulty(1);

        assertThatThrownBy(() -> service.create(10L, log, 1L))
                .isInstanceOf(TourNotFoundException.class);
    }

    /** Log erfolgreich erstellt → zurückgegebenes Objekt hat korrekte Werte */
    @Test
    void create_returnsCreatedLog_whenTourExists() {
        TourEntity tour = new TourEntity();
        tour.setId(10L);
        tour.setUserId(1L);
        tour.setName("Tour A");

        TourLogEntity saved = new TourLogEntity();
        saved.setId(200L);
        saved.setTour(tour);
        saved.setComment("Super Tour");
        saved.setDifficulty(1);
        saved.setTotalDistance(8.0);
        saved.setTotalTime(2.0);
        saved.setRating(5);
        saved.setDateTime(LocalDateTime.now());

        when(tourDal.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(tour));
        when(tourLogDal.save(any())).thenReturn(saved);

        TourLog input = new TourLog();
        input.setComment("Super Tour");
        input.setDifficulty(1);
        input.setTotalDistance(8.0);
        input.setTotalTime(2.0);
        input.setRating(5);

        TourLog result = service.create(10L, input, 1L);

        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getComment()).isEqualTo("Super Tour");
        assertThat(result.getTourId()).isEqualTo(10L);
    }
}
