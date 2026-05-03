package at.fh_technikum.group09.tourplanner.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_logs")
@Getter
@Setter
@NoArgsConstructor
public class TourLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private TourEntity tour;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    private String comment;

    private int difficulty;

    @Column(name = "total_distance")
    private double totalDistance;

    @Column(name = "total_time")
    private double totalTime;

    private int rating;
}
