package za.co.discovery.assignment.bus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import za.co.discovery.assignment.orm.Planet;
import za.co.discovery.assignment.orm.Route;
import za.co.discovery.assignment.repository.PlanetRepository;
import za.co.discovery.assignment.repository.RouteRepository;

@Service
@Lazy
public class ShortestPathService implements DijkstraAlgorithm  {
	
	@Autowired
	private PlanetRepository planetRepo;


	@Autowired
	private RouteRepository routesRepo;

	private SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph =
			new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
	/**
	 * Initializing the SimpleDirectedWeightedGraph before any business methods are called
	 */
	@PostConstruct
	private void initWeightedGraph(){
		addVertex();
		addEdges();
	}

	/**
	 * Retrieving all the Planets(Vertex) from the database and initialize the Dijkstras graph
	 */
	public void addVertex(){
		for (Planet planet : planetRepo.findAll()) {
			this.graph.addVertex(planet.getPlanetID());
		}
	}

	/**
	 * Retrieving all the Routes(Edges) from the database and initialize the Dijkstras graph
	 */
	private void addEdges(){
		DefaultWeightedEdge edge = null;
		for (Route route : routesRepo.findAll()) {
			String source  = route.getSource().getPlanetID();
			String destination = route.getDest().getPlanetID();
			if(source != destination){
				edge = this.graph.addEdge(source,destination);
			}
			addWeight(edge, route.getDistance());
		}
	}

	/**
	 * Adding distance(weight) to the Route(Edge)
	 * @param edge current Route that we are adding weigh to
	 * @param weight  distance value being added to the Route
	 */
	private void addWeight(DefaultWeightedEdge edge, float weight) {
		this.graph.setEdgeWeight(edge, weight);
	}

	/**
	 * Delegating calls to Jgrapht (a  Dijkstras algorithm implementation)
	 */
	@Override
	public String findShortestPath(String source, String destination) {
		return DijkstraShortestPath.findPathBetween(this.graph, source, destination).toString();
	}

	
	/**
	 * Cleaning up after ourselves is always good
	 */
	@PreDestroy
	public void cleanup(){
		this.graph = null;
	}
}

