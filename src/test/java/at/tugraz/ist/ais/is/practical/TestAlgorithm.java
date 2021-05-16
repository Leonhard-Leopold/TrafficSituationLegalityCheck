package at.tugraz.ist.ais.is.practical;
import static at.tugraz.ist.ais.is.practical.TrafficSituationLegalityCheck.checkLegality;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class TestAlgorithm {

    private final Logger log = LoggerFactory.getLogger(TrafficSituationLegalityCheck.class);
    private List<Lane> lanes;
    private List<Cyclist> cyclists;
    private List<Pedestrian> pedestrians;
    private List<Car> cars;
    private HashMap<String, Lane> laneMap;


    @Before
    public void testSetup() {
        lanes = new ArrayList<Lane>();
        Lane lane_top = new Lane("top",false, false, null, null);
        Lane lane_right = new Lane("right",false, false, null, null);
        Lane lane_left = new Lane("left",false, false, null, null);
        Lane lane_bottom = new Lane("bottom",false, false, null, null);
        Collections.addAll(lanes, lane_top, lane_right, lane_left, lane_bottom);

        cyclists = new ArrayList<Cyclist>();
        pedestrians = new ArrayList<Pedestrian>();

        cars = new ArrayList<Car>();
        Car car1 = new Car("Car 1", null, null, null);
        Car car2 = new Car("Car 2", null, null, null);
        Car car3 = new Car("Car 3", null, null, null);
        Car car4 = new Car("Car 4", null, null, null);
        Collections.addAll(cars, car1, car2, car3, car4);

        laneMap = new HashMap<String, Lane>();
        for (Lane lane : lanes){
            laneMap.put(lane.getPosition(), lane);
        }

    }

    @Test
    public void noCars() {
        log.info("\n\n---------- No Cars ----------");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    // Basic Rules

    @Test
    public void blinkingInTheWrongDirection() {
        log.info("\n\n---------- Blinking in the wrong direction ----------");
        cars.get(0).setLane(laneMap.get("top")).setDirection("right").setBlinking("left");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void blinkingButGoinfStraight() {
        log.info("\n\n---------- Blinking but going straight ----------");
        cars.get(0).setLane(laneMap.get("top")).setDirection("straight").setBlinking("left");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void forgettingToBlink() {
        log.info("\n\n---------- Forgetting to blink ----------");
        cars.get(0).setLane(laneMap.get("top")).setDirection("right").setBlinking(null);
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void cyclistCrossingStreetWithoutMarking() {
        log.info("\n\n---------- Cyclist Crossing Street Without Marking ----------");
        Cyclist p1 = new Cyclist("Cyclist Bob", laneMap.get("right"));
        Collections.addAll(cyclists, p1);
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void pedestrianCrossingStreetWithoutMarking() {
        log.info("\n\n---------- Pedestrian Crossing Street Without Marking ----------");
        laneMap.get("left").setPedestrian_crossing(true);
        Pedestrian p1 = new Pedestrian("Pedestrian Bob", laneMap.get("right"));
        Collections.addAll(pedestrians, p1);
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void drivingAtARedLight() {
        log.info("\n\n---------- Driving at a red light ----------");
        laneMap.get("top").setTraffic_light("red");
        cars.get(0).setLane(laneMap.get("top")).setDirection("right").setBlinking("right");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void drivingAtAGreenLight() {
        log.info("\n\n---------- Driving at a green light ----------");
        laneMap.get("top").setTraffic_light("green");
        cars.get(0).setLane(laneMap.get("top")).setDirection("right").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void waitingAtARedLight() {
        log.info("\n\n---------- Waiting at a red light ----------");
        laneMap.get("top").setTraffic_light("red");
        cars.get(0).setLane(laneMap.get("top")).setDirection("standing").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void drivingTowardsAnEmptyCyclistCrossing() {
        log.info("\n\n---------- Driving Towards an empty Cyclist Crossing ----------");
        laneMap.get("right").setCyclist_crossing(true);
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("right").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void drivingTowardsACyclist() {
        log.info("\n\n---------- Driving Towards A Cyclist ----------");
        laneMap.get("right").setCyclist_crossing(true);
        Cyclist c1 = new Cyclist("Cyclist Bob", laneMap.get("right"));
        Collections.addAll(cyclists, c1);
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("right").setBlinking("right");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void blinkingTowardsACyclist() {
        log.info("\n\n---------- Standing and Blinking Towards A Cyclist ----------");
        laneMap.get("right").setCyclist_crossing(true);
        Cyclist c1 = new Cyclist("Cyclist Bob", laneMap.get("right"));
        Collections.addAll(cyclists, c1);
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("standing").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void drivingTowardsAnEmptyPedestrianCrossing() {
        log.info("\n\n---------- Driving Towards an empty Pedestrian Crossing ----------");
        laneMap.get("right").setPedestrian_crossing(true);
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("right").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void drivingTowardsAPedestrian() {
        log.info("\n\n---------- Driving Towards A Pedestrian ----------");
        laneMap.get("right").setPedestrian_crossing(true);
        Pedestrian p1 = new Pedestrian("Pedestrian Bob", laneMap.get("right"));
        Collections.addAll(pedestrians, p1);
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("right").setBlinking("right");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }
    @Test
    public void blinkingTowardsAPedestrian() {
        log.info("\n\n---------- Standing and Blinking Towards A Pedestrian ----------");
        laneMap.get("right").setPedestrian_crossing(true);
        Pedestrian p1 = new Pedestrian("Pedestrian Bob", laneMap.get("right"));
        Collections.addAll(pedestrians, p1);
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("standing").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }


    // Traffic Situations

    @Test
    public void allDrivingRight() {
        log.info("\n\n---------- 4 Cars driving right ----------");
        cars.get(0).setLane(laneMap.get("top")).setDirection("right").setBlinking("right");
        cars.get(1).setLane(laneMap.get("right")).setDirection("right").setBlinking("right");
        cars.get(2).setLane(laneMap.get("bottom")).setDirection("right").setBlinking("right");
        cars.get(3).setLane(laneMap.get("left")).setDirection("right").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void allDrivingStraight() {
        log.info("\n\n---------- 4 Cars driving straight ----------");
        cars.get(0).setLane(laneMap.get("top")).setDirection("straight").setBlinking(null);
        cars.get(1).setLane(laneMap.get("right")).setDirection("straight").setBlinking(null);
        cars.get(2).setLane(laneMap.get("bottom")).setDirection("straight").setBlinking(null);
        cars.get(3).setLane(laneMap.get("left")).setDirection("straight").setBlinking(null);
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void allDrivingLeft() {
        log.info("\n\n---------- 4 Cars driving left ----------");
        cars.get(0).setLane(laneMap.get("top")).setDirection("left").setBlinking("left");
        cars.get(1).setLane(laneMap.get("right")).setDirection("left").setBlinking("left");
        cars.get(2).setLane(laneMap.get("bottom")).setDirection("left").setBlinking("left");
        cars.get(3).setLane(laneMap.get("left")).setDirection("left").setBlinking("left");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void allDrivingRight2Yield() {
        log.info("\n\n---------- 4 Cars driving left and 2 have to yield ----------");
        cars.get(0).setLane(laneMap.get("top").setTraffic_sign("yield")).setDirection("right").setBlinking("right");
        cars.get(1).setLane(laneMap.get("right")).setDirection("right").setBlinking("right");
        cars.get(2).setLane(laneMap.get("bottom").setTraffic_sign("yield")).setDirection("right").setBlinking("right");
        cars.get(3).setLane(laneMap.get("left")).setDirection("right").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void allDrivingLeft2Yield() {
        log.info("\n\n---------- 4 Cars driving left and 2 have to yield ----------");
        cars.get(0).setLane(laneMap.get("top").setTraffic_sign("yield")).setDirection("left").setBlinking("left");
        cars.get(1).setLane(laneMap.get("right")).setDirection("left").setBlinking("left");
        cars.get(2).setLane(laneMap.get("bottom").setTraffic_sign("yield")).setDirection("left").setBlinking("left");
        cars.get(3).setLane(laneMap.get("left")).setDirection("left").setBlinking("left");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void allDrivingLeft2YieldWaiting() {
        log.info("\n\n---------- 4 Cars driving left and 2 are yielding ----------");
        cars.get(0).setLane(laneMap.get("top").setTraffic_sign("yield")).setDirection("standing").setBlinking("left");
        cars.get(1).setLane(laneMap.get("right")).setDirection("left").setBlinking("left");
        cars.get(2).setLane(laneMap.get("bottom").setTraffic_sign("yield")).setDirection("standing").setBlinking("left");
        cars.get(3).setLane(laneMap.get("left")).setDirection("left").setBlinking("left");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void allDrivingLeft2YieldNotWaiting() {
        log.info("\n\n---------- 4 Cars driving left, 2 should yield but drive, 2 can drive but yield ----------");
        cars.get(0).setLane(laneMap.get("top").setTraffic_sign("yield")).setDirection("left").setBlinking("left");
        cars.get(1).setLane(laneMap.get("right")).setDirection("standing").setBlinking("left");
        cars.get(2).setLane(laneMap.get("bottom").setTraffic_sign("yield")).setDirection("left").setBlinking("left");
        cars.get(3).setLane(laneMap.get("left")).setDirection("standing").setBlinking("left");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void rechtsRegel() {
        log.info("\n\n---------- Testing the 'Rechtsregel' ----------");
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("standing").setBlinking("right");
        cars.get(1).setLane(laneMap.get("left")).setDirection("straight").setBlinking(null);
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void rechtsRegel2() {
        log.info("\n\n---------- Testing the 'Rechtsregel' again ----------");
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("right").setBlinking("right");
        cars.get(1).setLane(laneMap.get("left")).setDirection("standing").setBlinking(null);
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void rechtsRegel3() {
        log.info("\n\n---------- Testing the 'Rechtsregel' again ----------");
        cars.get(0).setLane(laneMap.get("bottom").setTraffic_sign("stop")).setDirection("left").setBlinking("left");
        cars.get(1).setLane(laneMap.get("left").setTraffic_sign("yield")).setDirection("standing").setBlinking(null);
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void directionShouldDecide1() {
        log.info("\n\n---------- Equal signs, traffic lights and cars are on the opposite site of each other " +
                "- direction should decide - one is driving straight the other right----------");
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("straight").setBlinking(null);
        cars.get(1).setLane(laneMap.get("top")).setDirection("right").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void directionShouldDecide2() {
        log.info("\n\n---------- Equal signs, traffic lights and cars are on the opposite site of each other " +
                "- direction should decide - one is driving straight the other left ----------");
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("straight").setBlinking(null);
        cars.get(1).setLane(laneMap.get("top")).setDirection("left").setBlinking("left");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void directionShouldDecide3() {
        log.info("\n\n---------- Equal signs, traffic lights and cars are on the opposite site of each other " +
                "- direction should decide - one is driving right the other left ----------");
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("left").setBlinking("left");
        cars.get(1).setLane(laneMap.get("top")).setDirection("right").setBlinking("right");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void directionShouldDecide4() {
        log.info("\n\n---------- Equal signs, traffic lights and cars are on the opposite site of each other " +
                "- direction should decide - both are driving left ----------");
        cars.get(0).setLane(laneMap.get("bottom")).setDirection("left").setBlinking("left");
        cars.get(1).setLane(laneMap.get("top")).setDirection("left").setBlinking("left");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void concreteExample1() {
        log.info("\n\n---------- Example given in the task introductions ----------");
        cars.get(0).setLane(laneMap.get("top").setCyclist_crossing(true).setTraffic_sign("yield")).setDirection("straight").setBlinking(null);
        cars.get(1).setLane(laneMap.get("bottom").setTraffic_sign("yield")).setDirection("standing").setBlinking(null);
        cars.get(2).setLane(laneMap.get("right")).setDirection("right").setBlinking("right");
        Cyclist c1 = new Cyclist("Cyclist Bob", laneMap.get("top"));
        Collections.addAll(cyclists, c1);
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void concreteExample1WithoutCyclist() {
        log.info("\n\n---------- Example given in the task introductions with out the cyclist ----------");
        cars.get(0).setLane(laneMap.get("top").setCyclist_crossing(true).setTraffic_sign("yield")).setDirection("straight").setBlinking(null);
        cars.get(1).setLane(laneMap.get("bottom").setTraffic_sign("yield")).setDirection("standing").setBlinking(null);
        cars.get(2).setLane(laneMap.get("right")).setDirection("right").setBlinking("right");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void concreteExample1IncorrectRightOfWay() {
        log.info("\n\n---------- Example given in the task introductions but a car ignores the yield sign ----------");
        cars.get(0).setLane(laneMap.get("top").setCyclist_crossing(true).setTraffic_sign("yield")).setDirection("straight").setBlinking(null);
        cars.get(1).setLane(laneMap.get("bottom").setTraffic_sign("yield")).setDirection("straight").setBlinking(null);
        cars.get(2).setLane(laneMap.get("right")).setDirection("standing").setBlinking("right");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void concreteExample2() {
        log.info("\n\n---------- Example given in the task introductions ----------");
        cars.get(0).setLane(laneMap.get("top").setTraffic_sign("stop")).setDirection("standing").setBlinking("right");
        cars.get(1).setLane(laneMap.get("right").setTraffic_sign("stop")).setDirection("straight").setBlinking(null);
        cars.get(2).setLane(laneMap.get("left").setTraffic_sign("stop")).setDirection("straight").setBlinking(null);
        laneMap.get("bottom").setTraffic_sign("stop");
        assertFalse( checkLegality(lanes, cars, cyclists, pedestrians));
    }

    @Test
    public void concreteExample2WithoutTakingRightOfWay() {
        log.info("\n\n---------- Example given in the task introductions with the car on the right " +
                "taking the right of way from the car on the top ----------");
        cars.get(0).setLane(laneMap.get("top").setTraffic_sign("stop")).setDirection("standing").setBlinking("right");
        cars.get(1).setLane(laneMap.get("right").setTraffic_sign("stop")).setDirection("standing").setBlinking(null);
        cars.get(2).setLane(laneMap.get("left").setTraffic_sign("stop")).setDirection("straight").setBlinking(null);
        laneMap.get("bottom").setTraffic_sign("stop");
        assertTrue( checkLegality(lanes, cars, cyclists, pedestrians));
    }


}
