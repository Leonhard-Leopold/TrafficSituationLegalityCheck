package at.tugraz.ist.ais.is.practical;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OntData {

    List<Car> cars;
    List<Lane> lanes;
    List<Cyclist> cyclists;
    List<Pedestrian> pedestrians;

    public OntData() {
        this.cars = new ArrayList<>();
        this.lanes = new ArrayList<>();
        this.cyclists = new ArrayList<>();
        this.pedestrians = new ArrayList<>();
    }

    public List<Car> getCars() {
        return cars;
    }

    public List<Lane> getLanes() {
        return lanes;
    }

    public List<Cyclist> getCyclists() {
        return cyclists;
    }

    public List<Pedestrian> getPedestrians() {
        return pedestrians;
    }

    public void loadModelAndParseObjects(OntModel model) {
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

        ResultSet results = qexec.execSelect();

        List<Car> cars = new ArrayList<>();
        List<Lane> lanes = new ArrayList<>();
        List<Cyclist> cyclists = new ArrayList<>();
        List<Pedestrian> pedestrians = new ArrayList<>();


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
        HashMap<String, Lane> laneMap = new HashMap<>();
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

        this.cars = cars;
        this.lanes = lanes;
        this.cyclists = cyclists;
        this.pedestrians = pedestrians;
    }

}