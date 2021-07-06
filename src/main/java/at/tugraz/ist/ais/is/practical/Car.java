package at.tugraz.ist.ais.is.practical;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Car {
    private String name;
    private Lane lane;
    private String blinking;
    private String direction;
    private String target_direction;
    private Lane target_lane;
    private Lane parking_on;
    private String type;
    private String overtaking;

    public Car(String name, Lane lane, String blinking, String direction, Lane parking_on, String type, String overtaking) {
        this.name = name;
        if (name == null)
            this.name = "Some car";
        this.lane = lane;
        this.parking_on = parking_on;
        if (blinking != null)
            this.blinking = blinking.replace("blinking_", "");
        if (Objects.equals(blinking, "straight"))
            this.blinking = null;
        if (direction == null)
            this.direction = "standing";
        else
            this.direction = direction.replace("driving_", "");
        if (type == null)
            this.type = "Car";
        else
            this.type = type;

        this.overtaking = overtaking;

    }


    public Boolean findTarget(Logger log, List<Lane> lanes) {
        if (this.getOvertaking() == null && this.getBlinking() != null && !this.getDirection().equals("standing") && !this.getDirection().equals(this.getBlinking())) {
            log.error("Blinking and driving direction not consistent on " + this.getName() + "!");
            return false;
        }
        String position = this.getLane().getPosition();
        String target = this.getBlinking() != null ? this.getBlinking() : this.getDirection();

        //if a car is overtaking it has to blink left. Since it is also moving we can use the direction
        if(this.getOvertaking() != null)
            target = this.getDirection();

        //if a car is doing nothing (no blinking no driving) I assume it is going straight
        if (target == null || target.equals("standing"))
            target = "straight";
        this.setTarget_direction(target);

        //find the correct lane
        HashMap<String, Lane> laneMap = new HashMap<String, Lane>();
        for (Lane lane : lanes) {
            laneMap.put(lane.getPosition(), lane);
        }

        if ((target.equals("right") && position.equals("right")) || (target.equals("straight") && position.equals("bottom")) || (target.equals("left") && position.equals("left")))
            this.setTarget_lane(laneMap.get("top"));
        else if ((target.equals("right") && position.equals("left")) || (target.equals("straight") && position.equals("top")) || (target.equals("left") && position.equals("right")))
            this.setTarget_lane(laneMap.get("bottom"));
        else if ((target.equals("right") && position.equals("top")) || (target.equals("straight") && position.equals("right")) || (target.equals("left") && position.equals("bottom")))
            this.setTarget_lane(laneMap.get("left"));
        else if ((target.equals("right") && position.equals("bottom")) || (target.equals("straight") && position.equals("left")) || (target.equals("left") && position.equals("top")))
            this.setTarget_lane(laneMap.get("right"));

        log.info(this.getName() + " is " + (this.getBlinking() == null ? "not blinking" : "blinking " + this.getBlinking()) + " and is driving " + this.getDirection() +
                " while being located on the " + position + " lane! Thus, it wants to drive to the lane on the " + this.getTarget_lane().getPosition() + "!");
        return true;
    }

    public String getName() {
        return name;
    }

    public Car setName(String name) {
        this.name = name;
        return this;
    }

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

    public Lane getTarget_lane() {
        return target_lane;
    }

    public void setTarget_lane(Lane target_lane) {
        this.target_lane = target_lane;
    }

    public String getTarget_direction() {
        return target_direction;
    }

    public void setTarget_direction(String target_direction) {
        this.target_direction = target_direction;
    }

    public Lane getParking_on() {
        return parking_on;
    }

    public void setParking_on(Lane parking_on) {
        this.parking_on = parking_on;
    }

    public String getType() {
        return type;
    }

    public Car setType(String type) {
        this.type = type;
        return this;
    }

    public String getOvertaking() {
        return overtaking;
    }

    public Car setOvertaking(String overtaking) {
        this.overtaking = overtaking;
        return this;
    }
}
