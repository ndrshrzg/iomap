package iomap.backend;

import example.movies.executor.CypherExecutor;
import example.movies.executor.BoltCypherExecutor;

import org.neo4j.driver.v1.types.Path;
import org.neo4j.helpers.collection.Iterators;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * @author andreas herzog
 * @since 09.01.2018
 */
public class IomapService {

    private final CypherExecutor cypher;

    public IomapService(String uri) {
        cypher = createCypherExecutor(uri);
    }
    private CypherExecutor createCypherExecutor(String uri) {
        try {
            String auth = new URL(uri.replace("bolt","http")).getUserInfo();
            if (auth != null) {
                String[] parts = auth.split(":");
                return new BoltCypherExecutor(uri,parts[0],parts[1]);
            }
            return new BoltCypherExecutor(uri);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Neo4j-ServerURL " + uri);
        }
    }
   
/// SEARCH FOR NODES
    
    @SuppressWarnings("rawtypes")
	public Map findPerson(String name) {
        if (name==null) return Collections.emptyMap();
        
        return Iterators.singleOrNull(cypher.query(
                "match (p:Person {Name:{name}})" + 
                "optional match (p:Person {Name:{name}}) <-[:WORKS_WITH]-(p2:Person) return p.Name as name, "
                + "collect({name:p.Name, company:p.Company, years:p.Years}) as details, "
                + "collect({name:p2.Name}) as coworkers",
                map("name", name)));
    }
    
    @SuppressWarnings("rawtypes")
    public Map findCompany(String name) {
    	if (name == null) return Collections.emptyMap();
    	
    	return Iterators.singleOrNull(cypher.query(
    			"match (c:Company {Name:{name}})"
    			+ "optional match (c)<-[WORKS_FOR]-(p:Person)"
    			+ "return c.Name as name, collect({name: p.Name, years: p.Years}) as employees limit 1",
    			map("name", name)));			
    }
    
    public Iterable<Map<String,Object>> search(String query) {
    	if (query==null || query.trim().isEmpty()) return Collections.emptyList();
    	return Iterators.asCollection(cypher.query(
    			"MATCH (c:Company) WHERE toLower(c.Name) CONTAINS {part}"
    			+ "optional match (c)<-[WORKS_FOR]-(p:Person)"
    			+ " RETURN c as company, collect ({name: p.Name, years: p.Years}) as employees limit 1",
    					map("part", query.toLowerCase())));
    }
    
    public Map<String, Object> shortestPathPerson(String p1, String p2, int limit){
    	Iterator<Map<String, Object>> result = cypher.queryPath(
    			"match (p1:Person {Name: {p1}}), (p2:Person {Name: {p2}}), p = shortestPath((p1)-[*..10]-(p2)) return p",
    			map("p1", p1, "p2", p2));

    	Map<String, Object> res = GraphBuilder.shortestPathGraph(result, limit);
    	
    	return res;
    	
    }
    
/// GRAPHS
    
    public Map<String, Object> worksForGraph(int limit, String name){
    	Iterator<Map<String,Object>> queryResult = cypher.query(
                "MATCH (c:Company {Name: {name}})<-[:WORKS_FOR]-(p:Person) " +
                " RETURN c.Name as company, collect(p.Name) as employees " +
                " LIMIT {limit}", map("limit",limit, "name", name));
    	Map<String, Object> res = GraphBuilder.worksForGraph(queryResult, limit);
    	return res;
    }
    
    public Map<String, Object> worksWithGraph(int limit, String name){
    	Iterator<Map<String, Object>> queryResult = cypher.query(
    			"match (p1:Person {Name: {name}})<-[:WORKS_WITH]-(p2:Person)"
    			+ "return p1.Name as emp1, collect(p2.Name) as coworkers "
    			+ "limit{limit}", map("limit", limit, "name", name));
    	Map<String, Object> res = GraphBuilder.worksWithGraph(queryResult, limit);
    	return res;
    }
    
    public Map<String, Object> worksOnGraph(int limit) {
        Iterator<Map<String,Object>> result = cypher.query(
                "match (t:Technology)<-[:WORKED_ON]-(p:Person) "
                + "return t.Project as technology, t.Company as owner, collect(p.Name) as employees "
                + "limit {limit}", map("limit",limit));
        Map<String, Object> res = GraphBuilder.worksOnGraph(result, limit);
        return res;
    }
    
}
