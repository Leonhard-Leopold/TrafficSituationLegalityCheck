package at.tugraz.ist.ais.is.practical;

import org.apache.jena.base.Sys;
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
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String file_nr = "1";
		try {
			System.out.println("\nWelcome to the Traffic Situation Legality Checker!\n" +
					"There are X different predefined ontologies you can test.\n" +
					"Which one do you want to use (Enter the number): ");
			file_nr = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		model.read("src/main/java/at/tugraz/ist/ais/is/practical/owl/crossroads_example"+file_nr+".owl");

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

		log.info("Query execution is created for the example query");
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		log.info("Obtains the result set");
		ResultSet results = qexec.execSelect();


		List<Car> cars = new ArrayList<Car>();
		List<Lane> lanes = new ArrayList<Lane>();
		List<Cyclist> cyclists = new ArrayList<Cyclist>();
		List<Pedestrian> pedestrians = new ArrayList<Pedestrian>();



		Map<String, Map<String, String>> map =new HashMap<>();

		log.info("Iterates over the result set");
		while (results.hasNext()) {
			QuerySolution sol = results.nextSolution();
			String individual = sol.getResource("individual").toString().split(("#"))[1];
			String property = sol.getResource("property").toString().split(("#"))[1];
			String relatedObject = sol.getResource("relatedObject").toString().split(("#"))[1];

			if (map.containsKey(individual)){
				map.get(individual).put(property,relatedObject);
			}
			else{
				map.put(individual,new HashMap(){{put(property,relatedObject);}});
			}
		}

		// First create the lanes, since they are needed for the position of other elements
		HashMap<String, Lane> laneMap = new HashMap<String, Lane>();
		for ( String key : map.keySet() ) {
			Map<String, String> attributes = map.get(key);
			String type = attributes.get("type");
			if(type.equals("lanes")){
				Lane l = new Lane(attributes.get("position"), false, false, null, null);
				lanes.add(l);
				laneMap.put(key, l);
			}
		}

		for ( String key : map.keySet() ) {
			Map<String, String> attributes = map.get(key);
			String type = attributes.get("type");

			switch(type){
				case "cars":
					cars.add(new Car(key, laneMap.get(attributes.get("on_lane")),
							attributes.get("blinking"), attributes.get("direction")));
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
			}
		}

		checkLegality(lanes, cars, cyclists, pedestrians);

		// TODO Theory questions:
		//  1. if one lane has a yield sign and the other a stop sign, are the equal, or not
		//  (if not --> add code to right of way function)


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
			if(!Objects.equals(car.getDirection(), "standing") && Objects.equals(car.getLane().getTraffic_light(), "red")){
				log.error(car.getName() + " is driving over a red light");
				return false;
			}
		}
		log.info("No car is driving over a red light");

		// check if a car is blinking in the wrong direction
		for (Car car : cars) {
			if ((car.getBlinking() == null && !Objects.equals(car.getDirection(), "standing") && !Objects.equals(car.getDirection(), "straight")) ||
					(car.getBlinking() != null && Objects.equals(car.getDirection(), "straight")) ||
					(car.getBlinking() != null && !Objects.equals(car.getDirection(), "standing") && !Objects.equals(car.getBlinking(), car.getDirection()))) {
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
				if(!Objects.equals(car.getDirection(), "standing") && tl == cyclist.getLane()){
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
				if(!Objects.equals(car.getDirection(), "standing") && tl == pedestrian.getLane()){
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
					if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && Objects.equals(car2.getTarget_direction(), "straight")){
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
					if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && Objects.equals(car2.getTarget_direction(), "left")){
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
			if (Objects.equals(car1.getDirection(), "straight")) {
				for (Car car2 : cars) {
					if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && Objects.equals(car2.getTarget_direction(), "left")){
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
					if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "left"))){
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
					if (rightOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "left"))){
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
					if (otherSideOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "right"))){
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
					if (leftOf(car2.getLane(), car1.getLane()) && rightOfWay(car1, car2) == car2 && (Objects.equals(car2.getTarget_direction(), "straight") || Objects.equals(car2.getTarget_direction(), "left"))){
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