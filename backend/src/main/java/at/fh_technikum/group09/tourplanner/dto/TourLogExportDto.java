package at.fh_technikum.group09.tourplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Portable representation of a single tour-log entry used for JSON import/export.
 * Does not contain IDs or foreign keys so the data can be re-imported cleanly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourLogExportDto {

    private LocalDateTime dateTime;
    private String comment;
    private int difficulty;
    private double totalDistance;
    private double totalTime;
    private int rating;
}
