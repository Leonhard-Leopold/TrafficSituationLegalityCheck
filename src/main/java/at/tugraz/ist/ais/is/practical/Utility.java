package at.tugraz.ist.ais.is.practical;

import java.util.List;

public class Utility {

    // if lane1 is left of lane2
    public static boolean leftOf(Lane lane1, Lane lane2){
        String pos1 = lane1.getPosition();
        String pos2 = lane2.getPosition();
        if((pos1 == "bottom" && pos2 == "right") || (pos1 == "right" && pos2 == "top") ||
                (pos1 == "top" && pos2 == "left") || (pos1 == "left" && pos2 == "bottom"))
            return true;
        return false;
    }

    // if lane1 is right of lane2
    public static boolean rightOf(Lane lane1, Lane lane2){
        String pos1 = lane1.getPosition();
        String pos2 = lane2.getPosition();
        if((pos1 == "bottom" && pos2 == "left") || (pos1 == "right" && pos2 == "bottom") ||
                (pos1 == "top" && pos2 == "right") || (pos1 == "left" && pos2 == "top"))
            return true;
        return false;
    }

    // if lane1 is on the other side of lane2
    public static boolean otherSideOf(Lane lane1, Lane lane2){
        String pos1 = lane1.getPosition();
        String pos2 = lane2.getPosition();
        if((pos1 == "bottom" && pos2 == "top") || (pos1 == "right" && pos2 == "left") ||
                (pos1 == "top" && pos2 == "bottom") || (pos1 == "left" && pos2 == "right"))
            return true;
        return false;
    }

    public static Car rightOfWay(Car car1, Car car2) {
        Lane lane1 = car1.getLane();
        Lane lane2 = car2.getLane();

        // check if a traffic light is red
        if (lane1.getTraffic_light() == "green" && lane1.getTraffic_light() == "red")
            return car1;
        if (lane1.getTraffic_light() == "red" && lane1.getTraffic_light() == "green")
            return car2;

        // check if someone has to give way based on the traffic sign
        if(lane1.getTraffic_sign() == null && (lane2.getTraffic_sign() == "stop" || lane2.getTraffic_sign() == "yield"))
            return car1;
        if(lane2.getTraffic_sign() == null && (lane1.getTraffic_sign() == "stop" || lane1.getTraffic_sign() == "yield"))
            return car2;

        // check if someone is on the right side
        if(rightOf(lane1, lane2))
            return car1;

        if(rightOf(lane2, lane1))
            return car2;

        //now only situations where both cars are going straight. Now the direction decides.
        // Only matters when a car is going left since going straight or right does not impact the other car
        if(car1.getTarget_direction() == "left" && (car2.getTarget_direction() == "right"
                || car2.getTarget_direction() == "straight")){
            return car2;
        }

        //only happens when the have the same traffic light & sign & are on opposite sides & no car is going left
        return null;
    }
}
