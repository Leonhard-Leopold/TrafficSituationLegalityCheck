package at.tugraz.ist.ais.is.practical;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RiotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static at.tugraz.ist.ais.is.practical.Utility.*;

public class TrafficSituationLegalityCheck {

    private static final Logger log = LoggerFactory.getLogger(TrafficSituationLegalityCheck.class);

    public static void main(String[] args) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            while (true) {
                System.out.println("\nWelcome to the Traffic Situation Legality Checker!\n" +
                        "There are X different predefined ontologies you can test.\n" +
                        "Which one do you want to use (Enter a number [1-12] or 'e' for exit): ");
                String file_nr = reader.readLine();
                if (file_nr.equals("e")) {
                    System.out.println("\nClosing...");
                    break;
                }
                try {
                    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
                    model.read("src/main/java/at/tugraz/ist/ais/is/practical/owl/crossroads_example" + file_nr + ".owl");
                    OntData data = new OntData();
                    data.loadModelAndParseObjects(model);
                    checkLegality(data.getLanes(), data.getCars(), data.getCyclists(), data.getPedestrians());
                } catch(RiotNotFoundException e) {
                    System.out.println("Ontology not found...");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // checking if the situation is legal
    public static boolean checkLegality(List<Lane> lanes, List<Car> cars, List<Cyclist> cyclists, List<Pedestrian> pedestrians) {
        // Setup: remove all cars that are not on a lane
        cars.removeIf(car -> car.getLane() == null);

        // Setup: create help attribute to know where every car wants to go
        for (Car car : cars) {
            if (!car.findTarget(log, lanes)) {
                return false;
            }
        }

        // check if car is parking incorrectly
        for (Car car : cars) {
            if (car.getParking_on() != null) {
                if (!car.getParking_on().getParking_spot()) {
                    log.error(car.getName() + " is parking where there is no parking spot!");
                    return false;
                }
                if (car.getParking_on() != car.getLane()) {
                    log.error(car.getName() + " is parking on a different lane than it is one!");
                    return false;
                }
            }
        }
        log.info("No car is parking incorrectly");

        // check if driving on red traffic light
        for (Car car : cars) {
            if (!Objects.equals(car.getDirection(), "standing") && Objects.equals(car.getLane().getTraffic_light(), "red")) {
                log.error(car.getName() + " is driving over a red light");
                return false;
            }
        }
        log.info("No car is driving over a red light");

        // check if a car is blinking in the wrong direction
        for (Car car : cars) {
            if(car.getOvertaking() == null){
                if ((car.getBlinking() == null && !Objects.equals(car.getDirection(), "standing") && !Objects.equals(car.getDirection(), "straight")) ||
                        (car.getBlinking() != null && Objects.equals(car.getDirection(), "straight")) || (car.getBlinking() != null && !Objects.equals(car.getDirection(), "standing")
                        && !Objects.equals(car.getBlinking(), car.getDirection()))) {
                    log.error(car.getName() + " is blinking incorrectly");
                    return false;
                }
            }
        }
        log.info("No car is blinking incorrectly");

        // check if cyclist is crossing stress without marked cyclist crossing
        for (Cyclist cyclist : cyclists) {
            if (!cyclist.getLane().getCyclist_crossing()) {
                log.error(cyclist.getName() + " is crossing the street without a marked crossing");
                return false;
            }
        }
        log.info("No cyclist is crossing the street without a marked crossing");


        // check if pedestrian is crossing stress without marked cyclist crossing
        for (Pedestrian pedestrian : pedestrians) {
            if (!pedestrian.getLane().getPedestrian_crossing()) {
                log.error(pedestrian.getName() + " is crossing the street without a marked crossing");
                return false;
            }
        }
        log.info("No pedestrian is pedestrian the street without a marked crossing");


        // check if driving towards cyclists
        for (Car car : cars) {
            Lane tl = car.getTarget_lane();
            for (Cyclist cyclist : cyclists) {
                if (!Objects.equals(car.getDirection(), "standing") && tl == cyclist.getLane()) {
                    log.error(car.getName() + " is going to drive over a cyclist");
                    return false;
                }
            }
        }
        log.info("No car is going to drive over a cyclist");


        // check if driving towards pedestrians
        for (Car car : cars) {
            Lane tl = car.getTarget_lane();
            for (Pedestrian pedestrian : pedestrians) {
                if (!Objects.equals(car.getDirection(), "standing") && tl == pedestrian.getLane()) {
                    log.error(car.getName() + " is going to drive over a pedestrian");
                    return false;
                }
            }
        }
        log.info("No car is going to drive over a pedestrian");


        // check if a car is overtaking incorrectly
        for (Car car : cars) {
            if (car.getOvertaking() != null) {
                //a car cannot overtake while not driving
                if (Objects.equals(car.getDirection(), "standing")) {
                    log.error(car.getName() + " is overtaking, but not moving!");
                    return false;
                }

                //a car cannot overtake without blinking left
                if (!Objects.equals(car.getBlinking(), "left")) {
                    log.error(car.getName() + " is overtaking without blinking correctly!");
                    return false;
                }

                //a car cannot overtake a car that is on another lane
                for (Car car1 : cars) {
                    if (Objects.equals(car.getOvertaking(), car1.getName()) && !Objects.equals(car.getLane(), car1.getLane())) {
                        log.error(car.getName() + " is overtaking " + car1.getName() + ", but they are not on the same lane!");
                        return false;
                    }
                }

                // if overtaking on a red light or subordinate street
                Lane cl = car.getLane();
                if (Objects.equals(cl.getTraffic_sign(), "yield") || Objects.equals(cl.getTraffic_sign(), "stop") || Objects.equals(cl.getTraffic_light(), "red")) {
                    log.error(car.getName() + " is overtaking on a red light or subordinate street!");
                    return false;
                }

                // if there is a car on the target lane - possible crash
                Lane tl = car.getTarget_lane();
                for (Car car1 : cars) {
                    if (!Objects.equals(car.getDirection(), "standing") && tl == car1.getLane()) {
                        log.error(car.getName() + " is possibly going to crash into " + car1.getName() + " while overtaking!");
                        return false;
                    }
                }

                // if overtaking and impeding the other car
                for (Car car1 : cars) {
                    if (Objects.equals(car.getOvertaking(), car1.getName())) {
                        if ((Objects.equals(car.getDirection(), "straight") && Objects.equals(car1.getDirection(), "left")) ||
                                (Objects.equals(car.getDirection(), "right") && (Objects.equals(car1.getDirection(), "left") ||
                                        Objects.equals(car1.getDirection(), "straight")))) {
                            log.error(car.getName() + " is impeding " + car1.getName() + " while overtaking!");
                            return false;
                        }
                    }
                }

            }

        }
        log.info("No car is overtaking illegally");


        // check if turning right and someone to your left who does not have to give way is driving straight
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "right")) {
                for (Car car2 : cars) {
                    if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && Objects.equals(car2.getTarget_direction(), "straight")) {
                        log.debug("Issue at turning part 1");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }

        // check if turning right and someone on the opposite side who does not have to give way is driving left
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "right")) {
                for (Car car2 : cars) {
                    if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && Objects.equals(car2.getTarget_direction(), "left")) {
                        log.debug("Issue at turning part 2");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }
        log.info("Every car that is driving right is legal!");


        // check if driving straight and someone to your right who does not have to give way is driving in any direction
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "straight")) {
                for (Car car2 : cars) {
                    if (rightOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2) {
                        log.debug("Issue at turning part 3");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }
        // check if driving straight and someone on the opposite side who does not have to give way is driving left
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "straight")) {
                for (Car car2 : cars) {
                    if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && Objects.equals(car2.getTarget_direction(), "left")) {
                        log.debug("Issue at turning part 4");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }

        // check if driving straight and someone to your left who does not have to give way is driving straight or left
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "straight")) {
                for (Car car2 : cars) {
                    if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "left"))) {
                        log.debug("Issue at turning part 5");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }
        log.info("Every car that is driving straight is legal!");

        // check if driving left and someone to your right who does not have to give way is driving straight or left
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "left")) {
                for (Car car2 : cars) {
                    if (rightOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "left"))) {
                        log.debug("Issue at turning part 6");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }
        // check if driving left and someone on the opposite side who does not have to give way is driving straight or right
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "left")) {
                for (Car car2 : cars) {
                    if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "right"))) {
                        log.debug("Issue at turning part 7");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }

        // check if driving left and someone to your left who does not have to give way is driving straight or left
        for (Car car1 : cars) {
            if (Objects.equals(car1.getDirection(), "left")) {
                for (Car car2 : cars) {
                    if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "left"))) {
                        log.debug("Issue at turning part 8");
                        log.error(car1.getName() + " is impeding " + car2.getName());
                        return false;
                    }
                }
            }
        }
        log.info("Every car that is driving left is legal!");

        log.info("--- The situation is legal!! ---");
        return true;

    }


}