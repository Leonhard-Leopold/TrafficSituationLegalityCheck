package at.tugraz.ist.ais.is.practical;

import java.util.List;
import java.util.Objects;

public class Utility {

    // if lane1 is left of lane2
    public static boolean leftOf(Lane lane1, Lane lane2){
        String pos1 = lane1.getPosition();
        String pos2 = lane2.getPosition();
        if((Objects.equals(pos1, "bottom") && Objects.equals(pos2, "right")) || (Objects.equals(pos1, "right") && Objects.equals(pos2, "top")) ||
                (Objects.equals(pos1, "top") && Objects.equals(pos2, "left")) || (Objects.equals(pos1, "left") && Objects.equals(pos2, "bottom")))
            return true;
        return false;
    }

    // if lane1 is right of lane2
    public static boolean rightOf(Lane lane1, Lane lane2){
        String pos1 = lane1.getPosition();
        String pos2 = lane2.getPosition();
        if((Objects.equals(pos1, "bottom") && Objects.equals(pos2, "left")) || (Objects.equals(pos1, "right") && Objects.equals(pos2, "bottom")) ||
                (Objects.equals(pos1, "top") && Objects.equals(pos2, "right")) || (Objects.equals(pos1, "left") && Objects.equals(pos2, "top")))
            return true;
        return false;
    }

    // if lane1 is on the other side of lane2
    public static boolean otherSideOf(Lane lane1, Lane lane2){
        String pos1 = lane1.getPosition();
        String pos2 = lane2.getPosition();
        if((Objects.equals(pos1, "bottom") && Objects.equals(pos2, "top")) || (Objects.equals(pos1, "right") && Objects.equals(pos2, "left")) ||
                (Objects.equals(pos1, "top") && Objects.equals(pos2, "bottom")) || (Objects.equals(pos1, "left") && Objects.equals(pos2, "right")))
            return true;
        return false;
    }

    public static Car rightOfWay(Car car1, Car car2) {
        Lane lane1 = car1.getLane();
        Lane lane2 = car2.getLane();

        // check if a traffic light is red
        if (Objects.equals(lane1.getTraffic_light(), "green") && Objects.equals(lane1.getTraffic_light(), "red"))
            return car1;
        if (Objects.equals(lane1.getTraffic_light(), "red") && Objects.equals(lane1.getTraffic_light(), "green"))
            return car2;

        //check if someone is a streetcar
        if(Objects.equals(car1.getType(), "streetcar") && !Objects.equals(car2.getType(), "streetcar"))
            return car1;
        if(Objects.equals(car2.getType(), "streetcar") && !Objects.equals(car1.getType(), "streetcar"))
            return car2;

        // check if someone has to give way based on the traffic sign
        if(lane1.getTraffic_sign() == null && (Objects.equals(lane2.getTraffic_sign(), "stop") || Objects.equals(lane2.getTraffic_sign(), "yield")))
            return car1;
        if(lane2.getTraffic_sign() == null && (Objects.equals(lane1.getTraffic_sign(), "stop") || Objects.equals(lane1.getTraffic_sign(), "yield")))
            return car2;

        // check if someone is on the right side
        if(rightOf(lane1, lane2))
            return car1;

        if(rightOf(lane2, lane1))
            return car2;

        //now only situations where both cars are going straight. Now the direction decides.
        // Only matters when a car is going left since going straight or right does not impact the other car
        if(Objects.equals(car1.getTarget_direction(), "left") && (Objects.equals(car2.getTarget_direction(), "right")
                || Objects.equals(car2.getTarget_direction(), "straight"))){
            return car2;
        }

        //only happens when the have the same traffic light & sign & are on opposite sides & no car is going left
        return null;
    }
}
