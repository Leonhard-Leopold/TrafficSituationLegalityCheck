package at.tugraz.ist.ais.is.practical;

public class Pedestrian {
    private String name;
    private Lane lane;

    public Pedestrian(String name, Lane lane) {
        this.name = name;
        if(name == null)
            this.name = "Some pedestrian";
        this.lane = lane;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Lane getLane() {
        return lane;
    }

    public void setLane(Lane lane) {
        this.lane = lane;
    }
}
