package iomap.backend;

import static org.neo4j.helpers.collection.MapUtil.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Path.Segment;
import org.neo4j.driver.v1.types.Relationship;

public class GraphBuilder {
	
	@SuppressWarnings("unchecked")
    public static Map<String, Object> worksForGraph(Iterator<Map<String, Object>> data,int limit) {
        List nodes = new ArrayList();
        List rels= new ArrayList();
        int i = 0;
        while (data.hasNext()) {
            Map<String, Object> row = data.next();
            nodes.add(map("title",row.get("company"),"label","company"));
            int target=i;
            i++;
            for (Object name : (Collection) row.get("employees")) {
                Map<String, Object> employee = map("title", name,"label","person");
                int source = nodes.indexOf(employee);
                if (source == -1) {
                    nodes.add(employee);
                    source = i++;
                }
                rels.add(map("source",source,"target",target));
            }
        }
        return map("nodes", nodes, "links", rels);
    }
	
	@SuppressWarnings("unchecked")
    public static Map<String, Object> worksWithGraph(Iterator<Map<String, Object>> data,int limit) {
        List nodes = new ArrayList();
        List rels= new ArrayList();
        int i = 0;
    	while (data.hasNext()) {
    		Map<String,Object> row = data.next();
    		nodes.add(map("title", row.get("emp1"), "label", "person"));
    		int target = i;
    		i++;
    		for (Object name : (Collection) row.get("coworkers")) {
    			Map<String, Object> coworker = map("title", name, "label", "person");
    			int source = nodes.indexOf(coworker);
    			if (source == -1) {
    				nodes.add(coworker);
    				source = i++;
    			}
    			rels.add(map("source", source, "target", target));
    		}
    	}
    	return map("nodes", nodes, "links", rels);
    }
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> worksOnGraph(Iterator<Map<String, Object>> data, int limit){
		List nodes = new ArrayList();
		List rels= new ArrayList();
		int i=0;
		while (data.hasNext()) {
			Map<String, Object> row = data.next();
			nodes.add(map("title",row.get("technology"),"label","technology"));
			nodes.add(map("title",row.get("owner"), "label", "company"));
			int target=i;
			i++;
			int owner = i;
			rels.add(map("source", owner, "target", target));
			i++;
			for (Object name : (Collection) row.get("employees")) {
				Map<String, Object> employee = map("title", name,"label","person");
				int source = nodes.indexOf(employee);
				if (source == -1) {
					nodes.add(employee);
					source = i++;
				}
				rels.add(map("source",source,"target",target));
			}
		}
		return map("nodes", nodes, "links", rels);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> shortestPathGraph(Iterator<Map<String, Object>> data, int limit) {
		List nodes = new ArrayList();
		List rels = new ArrayList();
		Node previous = null;
		int i = 0;
		while (data.hasNext()) {
			Map<String, Object> row = data.next();
			Path res = (Path) row.get("p");
			for (Segment s : res) {
				Node start = s.start();
				Node end = s.end();
				//Relationship rel = s.relationship();
				//String rel_type = rel.type();  //label for relation ship type?
				String start_label = start.labels().iterator().next().toLowerCase();
				String end_label = end.labels().iterator().next().toLowerCase();
				if (i == 0) {
					nodes.add(map("title", getTitleForHTML(start), "label", start_label));
					nodes.add(map("title", getTitleForHTML(end), "label", end_label));
					rels.add(map("source", 0, "target", 1));
				}
				if (i > 0) {
					if(previous.id() != end.id()) {
						nodes.add(map("title", getTitleForHTML(end), "label", end_label));										
					}
					else {
						nodes.add(map("title", getTitleForHTML(start), "label", start_label));						
					}
					rels.add(map("source", i, "target", (i+1)));						
				}
				i++;
				previous = end;
			}		
		}
		return map("nodes", nodes, "links", rels);
	}
	
	private static Object getTitleForHTML(Node n) {
		String label = n.labels().iterator().next().toLowerCase();
		switch (label) {
		case "person":
			return n.get("Name").toString();
		case "company":
			return n.get("Name").toString();
		case "technology":
			return n.get("Project").toString();
		}
		
		return null;
	}
}