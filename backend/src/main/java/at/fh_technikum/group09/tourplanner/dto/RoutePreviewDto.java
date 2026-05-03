package at.fh_technikum.group09.tourplanner.dto;

public class RoutePreviewDto {

    private double distance;
    private double estimatedTime;
    private String routeInfo;

    public RoutePreviewDto() {}

    public RoutePreviewDto(double distance, double estimatedTime, String routeInfo) {
        this.distance = distance;
        this.estimatedTime = estimatedTime;
        this.routeInfo = routeInfo;
    }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(double estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getRouteInfo() { return routeInfo; }
    public void setRouteInfo(String routeInfo) { this.routeInfo = routeInfo; }
}
