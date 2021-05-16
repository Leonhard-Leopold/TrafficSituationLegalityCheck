package at.tugraz.ist.ais.is.practical;

import org.apache.jena.ontology.*;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.tugraz.ist.ais.is.practical.Utility.*;


public class TrafficSituationLegalityCheck {

	private static final Logger log = LoggerFactory.getLogger(TrafficSituationLegalityCheck.class);

	public static void test_ontology(){
		String query = String.join(System.lineSeparator(),
				"                PREFIX knowrob: <http://ias.cs.tum.edu/kb/knowrob.owl#>",
				"                PREFIX comp:   <http://ias.cs.tum.edu/kb/srdl2-comp.owl#>",
				"                PREFIX owl:                      <http://www.w3.org/2002/07/owl#>",
				"                PREFIX rdfs:      <http://www.w3.org/2000/01/rdf-schema#>",
				"                                                                                                                                       ",
				"                SELECT ?x                                                                                                   ",
				"                WHERE {                                                                                                     ",
				"                {                                                                                                                     ",
				"                  ?x rdfs:subClassOf [                                                                             ",
				"                    a owl:Restriction;                                                                                ",
				"                    owl:onProperty ?p ;                                                                            ",
				"                    owl:someValuesFrom ?y                                                                   ",
				"                  ] .                                                                                                                 ",
				"                }}");

		log.info("Example SPARQL query: ");
		log.info(query);

		log.info("Creates the OWL model");
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		log.info("Reads the document");
		//model.read("http://ias.cs.tum.edu/kb/knowrob.owl#");
		model.read("src/main/java/at/tugraz/ist/ais/is/practical/owl/crossroads1_test.owl");

		log.info("Query execution is created for the example query");
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		log.info("Obtains the result set");
		ResultSet results = qexec.execSelect();

		log.info("Iterates over the result set");
		while (results.hasNext()) {
			QuerySolution sol = results.nextSolution();
			log.info("Solution: " + sol);
		}

		log.info("Obtain the properties of the model");
		ExtendedIterator<ObjectProperty> properties = model.listObjectProperties();

		log.info("Iterates over the properties");
		while (properties.hasNext()) {
			log.info("Property: " + properties.next().getLocalName());
		}

		log.info("Obtains an iterator over individual resources");
		ExtendedIterator<Individual> individualResources = model.listIndividuals();

		log.info("Iterates over the resources");
		while (individualResources.hasNext()) {
			log.info("Individual resource: " + individualResources.next());
		}

		log.info("Obtains an extended iterator over classes");
		ExtendedIterator<OntClass> classes = model.listClasses();

		log.info("Iterates over the classes");
		while (classes.hasNext()) {
			log.info("Class: " + classes.next().toString());
		}
	}

	public static void main(String[] args) {
		 //test_ontology();

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read("src/main/java/at/tugraz/ist/ais/is/practical/owl/crossroads1_test.owl");


		// TODO load the data from the ontology using queries
		// TODO if the data needs to be queried everytime instead of once and loaded into objects, then we need to
		//  replace all getter and setter methods with queries

		// TODO Theory questions:
		//  1. if one lane has a yield sign and the other a stop sign, are the equal, or not
		//  (if not --> add code to right of way function)
		//  2. is standing still illegal when you could drive
		//  (if yes --> add code to check for all standing cars if they could drive into the direction they are blinking)

        Test1();

	}
    public static void Test1(){
		// All of this should be queried from the ontology
        log.info("_____________________Example 1_____________________");
        List<Lane> lanes = new ArrayList<Lane>();
        Lane lane_top = new Lane("top",true, false, null, "yield");
        Lane lane_right = new Lane("right",false, false, null, null);
        Lane lane_left = new Lane("left",false, false, null, null);
        Lane lane_bottom = new Lane("bottom",false, false, null, "yield");
        Collections.addAll(lanes, lane_top, lane_right, lane_left, lane_bottom);

        List<Cyclist> cyclists = new ArrayList<Cyclist>();
        Cyclist cyclist1 = new Cyclist("Cyclist Bob ", lane_top);
        Collections.addAll(cyclists, cyclist1);

        List<Pedestrian> pedestrians = new ArrayList<Pedestrian>();
		// Pedestrian pedestrian1 = new Pedestrian("Pedestrian Bob ", lane_top);
		// Collections.addAll(pedestrians, pedestrian1);

        List<Car> cars = new ArrayList<Car>();
        Car car1 = new Car("Car 1", lane_right, "right", "right");
        Car car2 = new Car("Car 2", lane_top, null, "straight");
        Car car3 = new Car("Car 3", lane_bottom, null, "standing");
        Collections.addAll(cars, car1, car2, car3);

        checkLegality(lanes, cars, cyclists, pedestrians);

    }

    // checking if the situation is legal
	public static boolean checkLegality(List<Lane> lanes, List<Car> cars, List<Cyclist> cyclists, List<Pedestrian> pedestrians) {
		// Setup: remove all cars that are not on a lane
		cars.removeIf(car -> car.getLane() == null);

		// Setup: create help attribute to know where every car wants to go
		for (Car car : cars) {
			if (!car.findTarget(log, lanes)){
				return false;
			}
		}

		// check if driving on red traffic light
		for (Car car : cars) {
			if(car.getDirection() != "standing" && car.getLane().getTraffic_light() == "red"){
				log.error(car.getName() + " is driving over a red light");
				return false;
			}
		}
		log.info("No car is driving over a red light");

		// check if a car is blinking in the wrong direction
		for (Car car : cars) {
			if ((car.getBlinking() == null && car.getDirection() != "standing" && car.getDirection() != "straight") ||
					(car.getBlinking() != null && car.getDirection() == "straight") ||
					(car.getBlinking() != null && car.getDirection() != "standing" && car.getBlinking() != car.getDirection() )) {
				log.error(car.getName() + " is blinking incorrectly");
				return false;
			}
		}
		log.info("No car is blinking incorrectly");

		// check if cyclist is crossing stress without marked cyclist crossing
		for (Cyclist cyclist : cyclists) {
			if(!cyclist.getLane().getCyclist_crossing()){
				log.error(cyclist.getName() + " is crossing the street without a marked crossing");
				return false;
			}
		}
		log.info("No cyclist is crossing the street without a marked crossing");


		// check if pedestrian is crossing stress without marked cyclist crossing
		for (Pedestrian pedestrian : pedestrians) {
			if(!pedestrian.getLane().getPedestrian_crossing()){
				log.error(pedestrian.getName() + " is crossing the street without a marked crossing");
				return false;
			}
		}
		log.info("No pedestrian is pedestrian the street without a marked crossing");


		// check if driving towards cyclists
		for (Car car : cars) {
			Lane tl = car.getTarget_lane();
			for (Cyclist cyclist : cyclists){
				if(car.getDirection() != "standing" && tl == cyclist.getLane()){
					log.error(car.getName() + " is going to drive over a cyclist");
					return false;
				}
			}
		}
		log.info("No car is going to drive over a cyclist");

		// check if driving towards pedestrians
		for (Car car : cars) {
			Lane tl = car.getTarget_lane();
			for (Pedestrian pedestrian : pedestrians){
				if(car.getDirection() != "standing" && tl == pedestrian.getLane()){
					log.error(car.getName() + " is going to drive over a pedestrian");
					return false;
				}
			}
		}
		log.info("No car is going to drive over a pedestrian");

		// check if turning right and someone to your left who does not have to give way is driving straight
		for (Car car1 : cars) {
			if (car1.getDirection() == "right") {
				for (Car car2 : cars) {
					if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && car2.getTarget_direction() == "straight"){
						log.debug("Issue at turning part 1");
						log.error(car1.getName() + " is impeding " + car2.getName());
						return false;
					}
				}
			}
		}

		// check if turning right and someone on the opposite side who does not have to give way is driving left
		for (Car car1 : cars) {
			if (car1.getDirection() == "right") {
				for (Car car2 : cars) {
					if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && car2.getTarget_direction() == "left"){
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
			if (car1.getDirection() == "straight") {
				for (Car car2 : cars) {
					if (rightOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2){
						log.debug("Issue at turning part 3");
						log.error(car1.getName() + " is impeding " + car2.getName());
						return false;
					}
				}
			}
		}
		// check if driving straight and someone on the opposite side who does not have to give way is driving left
		for (Car car1 : cars) {
			if (car1.getDirection() == "straight") {
				for (Car car2 : cars) {
					if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && car2.getTarget_direction() == "left"){
						log.debug("Issue at turning part 4");
						log.error(car1.getName() + " is impeding " + car2.getName());
						return false;
					}
				}
			}
		}

		// check if driving straight and someone to your left who does not have to give way is driving straight or left
		for (Car car1 : cars) {
			if (car1.getDirection() == "straight") {
				for (Car car2 : cars) {
					if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (car2.getTarget_direction() == "straight" || car2.getTarget_direction() == "left")){
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
			if (car1.getDirection() == "left") {
				for (Car car2 : cars) {
					if (rightOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (car2.getTarget_direction() == "straight" || car2.getTarget_direction() == "left")){
						log.debug("Issue at turning part 6");
						log.error(car1.getName() + " is impeding " + car2.getName());
						return false;
					}
				}
			}
		}
		// check if driving left and someone on the opposite side who does not have to give way is driving straight or right
		for (Car car1 : cars) {
			if (car1.getDirection() == "left") {
				for (Car car2 : cars) {
					if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (car2.getTarget_direction() == "straight" || car2.getTarget_direction() == "right")){
						log.debug("Issue at turning part 7");
						log.error(car1.getName() + " is impeding " + car2.getName());
						return false;
					}
				}
			}
		}

		// check if driving left and someone to your left who does not have to give way is driving straight or left
		for (Car car1 : cars) {
			if (car1.getDirection() == "left") {
				for (Car car2 : cars) {
					if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (car2.getTarget_direction() == "straight" || car2.getTarget_direction() == "left")){
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