package at.tugraz.ist.ais.is.practical;

import org.apache.jena.base.Sys;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ontology.*;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static at.tugraz.ist.ais.is.practical.Utility.*;

//inner class
class OntData {

    List<Car> cars;
    List<Lane> lanes;
    List<Cyclist> cyclists;
    List<Pedestrian> pedestrians;

    public OntData(List<Car> cars, List<Lane> lanes, List<Cyclist> cyclists, List<Pedestrian> pedestrians) {

        this.cars = cars;
        this.lanes = lanes;
        this.cyclists = cyclists;
        this.pedestrians = pedestrians;
    }

}

public class TrafficSituationLegalityCheck {

    private static final Logger log = LoggerFactory.getLogger(TrafficSituationLegalityCheck.class);

    public static OntData loadModelAndParseObjects(OntModel model) {
        String query = String.join(System.lineSeparator(),
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>",
                "SELECT distinct ?individual ?property ?relatedObject",
                "WHERE {",
                "?individual rdf:type owl:NamedIndividual.",
                "?individual ?property ?relatedObject .",
                "filter (?relatedObject != owl:NamedIndividual).",
                "}",
                "ORDER BY ?individual");

        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        log.info("Obtains the result set");
        ResultSet results = qexec.execSelect();


        List<Car> cars = new ArrayList<Car>();
        List<Lane> lanes = new ArrayList<Lane>();
        List<Cyclist> cyclists = new ArrayList<Cyclist>();
        List<Pedestrian> pedestrians = new ArrayList<Pedestrian>();


        Map<String, Map<String, String>> map = new HashMap<>();
        while (results.hasNext()) {
            QuerySolution sol = results.nextSolution();
            String individual = sol.getResource("individual").toString().split(("#"))[1];
            String property = sol.getResource("property").toString().split(("#"))[1];
            String relatedObject = sol.getResource("relatedObject").toString().split(("#"))[1];

            if (map.containsKey(individual)) {
                map.get(individual).put(property, relatedObject);
            } else {
                map.put(individual, new HashMap() {{
                    put(property, relatedObject);
                }});
            }
        }

        // First create the lanes, since they are needed for the position of other elements
        HashMap<String, Lane> laneMap = new HashMap<String, Lane>();
        for (String key : map.keySet()) {
            Map<String, String> attributes = map.get(key);
            String type = attributes.get("type");
            if (type.equals("lanes")) {
                Lane l = new Lane(attributes.get("position"), false, false, false, null, null);
                lanes.add(l);
                laneMap.put(key, l);
            }
        }

        for (String key : map.keySet()) {
            Map<String, String> attributes = map.get(key);
            String type = attributes.get("type");

            switch (type) {
                case "cars":
                    cars.add(new Car(key, laneMap.get(attributes.get("on_lane")),
                            attributes.get("blinking"), attributes.get("direction"), laneMap.get(attributes.get("parking_on")),
                            "car", attributes.get("is_overtaking")));
                    break;
                case "streetcar":
                    cars.add(new Car(key, laneMap.get(attributes.get("on_lane")),
                            attributes.get("blinking"), attributes.get("direction"), laneMap.get(attributes.get("parking_on")),
                            "streetcar", attributes.get("is_overtaking")));
                    break;
                case "cyclists":
                    cyclists.add(new Cyclist(key, laneMap.get(attributes.get("on_lane"))));
                    break;
                case "pedestrians":
                    pedestrians.add(new Pedestrian(key, laneMap.get(attributes.get("on_lane"))));
                    break;
                case "sign_yield":
                    laneMap.get(attributes.get("on_lane")).setTraffic_sign("yield");
                    break;
                case "sign_stop":
                    laneMap.get(attributes.get("on_lane")).setTraffic_sign("stop");
                    break;
                case "traffic_light_green":
                    laneMap.get(attributes.get("on_lane")).setTraffic_light("green");
                    break;
                case "traffic_light_red":
                    laneMap.get(attributes.get("on_lane")).setTraffic_light("red");
                    break;
                case "lane_markings_cyclists":
                    laneMap.get(attributes.get("on_lane")).setCyclist_crossing(true);
                    break;
                case "lane_markings_pedestrians":
                    laneMap.get(attributes.get("on_lane")).setPedestrian_crossing(true);
                    break;
                case "lane_markings_parking_spot":
                    laneMap.get(attributes.get("on_lane")).setParking_spot(true);
                    break;
            }
        }

        return new OntData(cars, lanes, cyclists, pedestrians);
    }

    public static void main(String[] args) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> list = Lists.newArrayList("1");
        String file_nr = "1";

        try {
            while (true) {
                System.out.println("\nWelcome to the Traffic Situation Legality Checker!\n" +
                        "There are X different predefined ontologies you can test.\n" +
                        "Which one do you want to use (Enter a number [X-Y] or 'e' for exit): ");
                file_nr = reader.readLine();
                if (file_nr.equals("e")) {
                    System.out.println("\nEnd");
                    break;
                }
                if (list.contains(file_nr)) {
                    model.read("src/main/java/at/tugraz/ist/ais/is/practical/owl/crossroads_example" + file_nr + ".owl");
                    OntData data = loadModelAndParseObjects(model);
                    checkLegality(data.lanes, data.cars, data.cyclists, data.pedestrians);
                } else {
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
        log.info("No car is driving over a red light");

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
                if (Objects.equals(car.getDirection(), "standing")) {
                    for (Car car1 : cars) {
                        if (Objects.equals(car.getOvertaking(), car1.getName()) && !Objects.equals(car.getLane(), car1.getLane())) {
                            log.error(car.getName() + " is overtaking " + car1.getName() + ", but they are not on the same lane!");
                            return false;
                        }
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
                        if ((car.getDirection() == "straight" && car1.getDirection() == "left") ||
                                (car.getDirection() == "right" && (car1.getDirection() == "left" || car1.getDirection() == "straight"))) {
                            log.error(car.getName() + " is impeding " + car1.getName() + " while overtaking!");
                            return false;
                        }
                    }
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