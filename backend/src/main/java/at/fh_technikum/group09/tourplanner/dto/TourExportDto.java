package at.fh_technikum.group09.tourplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Portable representation of a tour (including its logs) used for JSON import/export.
 * Does not carry database IDs so the data can be re-imported into any instance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourExportDto {

    private String name;
    private String description;
    private String fromLocation;
    private String toLocation;
    private String transportType;
    private double distance;
    private double estimatedTime;
    private String imageUrl;
    private String routeInfo;
    private List<TourLogExportDto> logs = new ArrayList<>();
}
