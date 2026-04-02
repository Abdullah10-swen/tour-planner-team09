package at.fh_technikum.group09.tourplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourLogDto {

    private Long id;
    private Long tourId;
    private LocalDateTime dateTime;
    private String comment;
    private int difficulty;
    private double totalDistance;
    private double totalTime;
    private int rating;
}

