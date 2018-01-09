package iomap.backend;
/**
 * @author andreas herzog
 * @since 09.01.2018
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.servlet.SparkApplication;

import java.net.URLDecoder;

import static spark.Spark.get;

public class IomapRoutes implements SparkApplication {

	private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private IomapService service;
	private GraphBuilder graph;

	public IomapRoutes(IomapService service) {
		this.service = service;
	}

	// calls the methods in MovieService
	public void init() {
		/*
		get("/", (req, res) -> {
			return gson.toJson(service.shortestPathPerson(
					URLDecoder.decode(req.queryParams("shortest_path_origin")), 
					URLDecoder.decode(req.queryParams("shortest_path_destination")), 
					req.queryParams("limit") != null ? Integer.valueOf(req.queryParams("limit")) : 100)
					);
		});
		*/
		get("/person/:name", (req, res) -> gson.toJson(service.findPerson(URLDecoder.decode(req.params("name")))));
		get("/company/:name", (req, res) -> gson.toJson(service.findCompany(URLDecoder.decode(req.params("name")))));
		
		get("/search", (req, res) -> gson.toJson(service.search(req.queryParams("q"))));
		
		get("/worksForGraph/:name", (req, res) -> {
			int limitWorksFor = req.queryParams("limit") != null ? Integer.valueOf(req.queryParams("limit")) : 100;
			return gson.toJson(service.worksForGraph(limitWorksFor,
					URLDecoder.decode(req.params("name"))
					));
			});
		get("/worksWithGraph/:name", (req, res) -> {
			int limitWorksWith = req.queryParams("limit") != null ? Integer.valueOf(req.queryParams("limit")) : 100;
			return gson.toJson(service.worksWithGraph(limitWorksWith, 
					URLDecoder.decode(req.params("name"))
					));
		});
		get("/worksOnGraph", (req, res) -> {
			int limitWorksWith = req.queryParams("limit") != null ? Integer.valueOf(req.queryParams("limit")) : 100;
			return gson.toJson(service.worksOnGraph(limitWorksWith));
		});
		get("/shortestPathPerson", (req, res) -> {
			return gson.toJson(service.shortestPathPerson(
					URLDecoder.decode(req.queryParams("p1")), 
					URLDecoder.decode(req.queryParams("p2")), 
					req.queryParams("limit") != null ? Integer.valueOf(req.queryParams("limit")) : 100)
					);
		});
		
		
		
	}
}
