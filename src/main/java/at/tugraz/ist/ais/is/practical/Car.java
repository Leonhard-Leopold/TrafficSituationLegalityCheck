package at.tugraz.ist.ais.is.practical;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Car {
    private String name = null;
    private Lane lane = null;
    private String blinking = null;
    private String direction = null;
    private String target_direction = null;
    private Lane target_lane = null;
    private boolean isParking = false;

    // https://www.oeamtc.at/thema/vorschriften-strafen/die-groessten-parksuenden-was-ist-erlaubt-was-ist-verboten-36486960
    // parken nur erlaubt wenn 2 weitere fahrbanstreifen für fließverkehr da

    public Car(String name, Lane lane, String blinking, String direction) {
        this.name = name;
        if(name == null)
            this.name = "Some car";
        this.lane = lane;
        if (blinking!=null)
            this.blinking = blinking.replace("blinking_", "");
        if(Objects.equals(blinking, "straight"))
            this.blinking = null;
        if(direction == null)
            this.direction = "standing";
        else
            this.direction = direction.replace("driving_", "");;

    }



    public Boolean findTarget(Logger log, List<Lane> lanes){
        if (this.getBlinking() != null && !this.getDirection().equals("standing") && !this.getDirection().equals(this.getBlinking())) {
            log.error("Blinking and driving direction not consistent on " + this.getName() + "!");
            return false;
        }
        String position = this.getLane().getPosition();
        String target = this.getBlinking() != null ? this.getBlinking() : this.getDirection();

        //if a car is doing nothing (no blinking no driving) I assume it is going straight
        if (target == null || target.equals("standing"))
            target = "straight";
        this.setTarget_direction(target);

        //find the correct lane
        HashMap<String, Lane> laneMap = new HashMap<String, Lane>();
        for (Lane lane : lanes){
            laneMap.put(lane.getPosition(), lane);
        }

        if ((target.equals("right") && position.equals("right")) ||(target.equals("straight") && position.equals("bottom")) ||(target.equals("left") && position.equals("left")))
            this.setTarget_lane(laneMap.get("top"));
        else if ((target.equals("right") && position.equals("left")) ||(target.equals("straight") && position.equals("top")) ||(target.equals("left") && position.equals("right")))
            this.setTarget_lane(laneMap.get("bottom"));
        else if ((target.equals("right") && position.equals("top")) ||(target.equals("straight") && position.equals("right")) ||(target.equals("left") && position.equals("bottom")))
            this.setTarget_lane(laneMap.get("left"));
        else if ((target.equals("right") && position.equals("bottom")) ||(target.equals("straight") && position.equals("left")) ||(target.equals("left") && position.equals("top")))
            this.setTarget_lane(laneMap.get("right"));

        log.info(this.getName() + " is " + (this.getBlinking() == null ? "not blinking" : "blinking " + this.getBlinking()) + " and is driving " + this.getDirection() +
                " while being located on the " + position + " lane! Thus, it wants to drive to the lane on the " + this.getTarget_lane().getPosition() + "!");
        return true;
    }

    public String getName() { return name; }

    public Lane getLane() {
        return lane;
    }

    public Car setLane(Lane lane) {
        this.lane = lane;
        return this;
    }

    public String getBlinking() {
        return blinking;
    }

    public Car setBlinking(String blinking) {
        this.blinking = blinking;
        return this;
    }

    public String getDirection() {
        return direction;
    }

    public Car setDirection(String direction) {
        this.direction = direction;
        return this;
    }

    public Lane getTarget_lane() { return target_lane; }

    public void setTarget_lane(Lane target_lane) { this.target_lane = target_lane; }

    public String getTarget_direction() { return target_direction; }

    public void setTarget_direction(String target_direction) { this.target_direction = target_direction; }

    public boolean isParking() { return isParking;}
}
