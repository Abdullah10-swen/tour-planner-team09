package at.fh_technikum.group09.tourplanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {

    private Long id;
    private String name;
    private String description;
    private String fromLocation;
    private String toLocation;
    private String transportType;
    private double distance; // km
    private double estimatedTime; // Stunden
    private String imageUrl;
    private String routeInfo;
}
