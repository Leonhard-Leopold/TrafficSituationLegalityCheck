package at.tugraz.ist.ais.is.practical;

public class Lane {
    private String position;
    private Boolean cyclist_crossing;
    private Boolean pedestrian_crossing;
    private Boolean parking_spot;
    private String traffic_light;
    private String traffic_sign;

    public Lane(String position, Boolean cyclist_crossing, Boolean pedestrian_crossing, Boolean parking_spot, String traffic_light, String traffic_sign) {
        this.position = position;
        this.cyclist_crossing = cyclist_crossing;
        this.pedestrian_crossing = pedestrian_crossing;
        this.parking_spot = parking_spot;
        this.traffic_light = traffic_light;
        this.traffic_sign = traffic_sign;
    }

    public String getPosition() {
        return position;
    }

    public Lane setPosition(String position) {
        this.position = position;
        return this;
    }

    public Boolean getCyclist_crossing() {
        return cyclist_crossing;
    }


    public Lane setCyclist_crossing(Boolean cyclist_crossing) {
        this.cyclist_crossing = cyclist_crossing;
        return this;
    }

    public Boolean getPedestrian_crossing() {
        return pedestrian_crossing;
    }

    public Lane setPedestrian_crossing(Boolean pedestrian_crossing) {
        this.pedestrian_crossing = pedestrian_crossing;
        return this;
    }

    public String getTraffic_light() {
        return traffic_light;
    }

    public Lane setTraffic_light(String traffic_light) {
        this.traffic_light = traffic_light;
        return this;
    }

    public String getTraffic_sign() {
        return traffic_sign;
    }

    public Lane setTraffic_sign(String traffic_sign) {
        this.traffic_sign = traffic_sign;
        return this;
    }

    public Boolean getParking_spot() {
        return parking_spot;
    }

    public Lane setParking_spot(Boolean parking_spot) {
        this.parking_spot = parking_spot;
        return this;
    }
}
