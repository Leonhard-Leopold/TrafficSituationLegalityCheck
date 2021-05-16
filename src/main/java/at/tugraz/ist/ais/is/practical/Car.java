package at.tugraz.ist.ais.is.practical;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;

public class Car {
    private String name = null;
    private Lane lane = null;
    private String blinking = null;
    private String direction = null;
    private String target_direction = null;
    private Lane target_lane = null;

    public Car(String name, Lane lane, String blinking, String direction) {
        this.name = name;
        if(name == null)
            this.name = "Some car";
        this.lane = lane;
        this.blinking = blinking;
        if(blinking == "straight")
            this.blinking = null;
        this.direction = direction;
        if(direction == null)
            this.direction = "standing";
    }

    public Boolean findTarget(Logger log, List<Lane> lanes){
        if (this.getBlinking() != null && this.getDirection() != "standing" && this.getDirection() != this.getBlinking()) {
            log.error("Blinking and Driving Direction not consistent on " + this.getName() + "!");
            return false;
        }
        String position = this.getLane().getPosition();
        String target = this.getBlinking() != null ? this.getBlinking() : this.getDirection();
        //TODO if a car is doing nothing (no blinking no driving) I assume it is going straight
        if (target == null || target == "standing")
            target = "straight";
        this.setTarget_direction(target);

        //find the correct lane
        HashMap<String, Lane> laneMap = new HashMap<String, Lane>();
        for (Lane lane : lanes){
            laneMap.put(lane.getPosition(), lane);
        }

        if ((target == "right" && position == "right") ||(target == "straight" && position == "bottom") ||(target == "left" && position == "left"))
            this.setTarget_lane(laneMap.get("top"));
        else if ((target == "right" && position == "left") ||(target == "straight" && position == "top") ||(target == "left" && position == "right"))
            this.setTarget_lane(laneMap.get("bottom"));
        else if ((target == "right" && position == "top") ||(target == "straight" && position == "right") ||(target == "left" && position == "bottom"))
            this.setTarget_lane(laneMap.get("left"));
        else if ((target == "right" && position == "bottom") ||(target == "straight" && position == "left") ||(target == "left" && position == "top"))
            this.setTarget_lane(laneMap.get("right"));

        log.info(this.getName() + " is blinking " + this.getBlinking() + " and is driving " + this.getDirection() +
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
}
