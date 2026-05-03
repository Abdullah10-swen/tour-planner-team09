package at.fh_technikum.group09.tourplanner.dto;

public class LocationSuggestionDto {

    private String label;
    private double lon;
    private double lat;

    public LocationSuggestionDto() {}

    public LocationSuggestionDto(String label, double lon, double lat) {
        this.label = label;
        this.lon = lon;
        this.lat = lat;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
}
