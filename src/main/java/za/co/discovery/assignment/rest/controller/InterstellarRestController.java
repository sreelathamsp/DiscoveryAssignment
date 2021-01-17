package za.co.discovery.assignment.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import za.co.discovery.assignment.bus.DijkstraAlgorithm;

@RestController
public class InterstellarRestController {
	
	@Autowired
	@Lazy
	private DijkstraAlgorithm shortestPathService;
	
	/**
	 * Determine Shortest path between source and destination
	 * @Usage http://localhost:8081/shortestpath/A/Z
	 */
	@RequestMapping(method = RequestMethod.GET, value="/shortestpath/{source}/{destination}")
	@ResponseBody
	public String getShortestPath(@PathVariable("source") String source, @PathVariable("destination") String destination){
		return shortestPathService.findShortestPath(source, destination);
	}
}
