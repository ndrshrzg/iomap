package graph_db;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.*;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Neo4jTest {
	
	GraphDatabaseService db;

	private static enum NodeLabels implements Label
	{
		Person,
		Company,
		Project;
	}
	
	public static void main(String[] args) {
		GraphDatabaseService db = null;
		
		try{
			File dbDir = new File("db");
			File employeeFile_ = new File("./sample_data/employees.csv");
			File companyFile_ = new File("./sample_data/companies.csv");
			File projectFile_ = new File("./sample_data/projects.csv");

			System.out.println("loading employees");
			List<List<String>> employees = readEmployees(employeeFile_);
			System.out.println("loading companies");
			List<List<String>> companies = readCompanies(companyFile_);
			System.out.println("loading projects");
			List<List<String>> projects = readProjects(projectFile_);
			
			if (dbDir.exists()) FileUtils.deleteRecursively(dbDir);
			
			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbDir);
			System.out.println("creating employees");
			for (List<String> employee : employees)
			{
				try (Transaction tx = db.beginTx())
				{					
					Node newNode = db.createNode();
					newNode.addLabel(NodeLabels.Person);
					newNode.setProperty("Name", employee.get(0));
					newNode.setProperty("Company", employee.get(1));
					newNode.setProperty("Years", employee.get(2));
					
					tx.success();
				} catch (Exception e){e.printStackTrace();}
			}
			System.out.println("creating companies");
			for (List<String> company : companies)
			{
				try (Transaction tx = db.beginTx())
				{		
					Node newNode = db.createNode();
					newNode.addLabel(NodeLabels.Company);
					newNode.setProperty("Name", company.get(0));
					newNode.setProperty("Industry", company.get(1));
					
					tx.success();
				} catch (Exception e){e.printStackTrace();}
			}
			System.out.println("creating projects");
			for (List<String> project : projects)
			{
				try (Transaction tx = db.beginTx())
				{					
					Node newNode = db.createNode();
					newNode.addLabel(NodeLabels.Project);
					newNode.setProperty("Project", project.get(0));
					newNode.setProperty("Employee", project.get(1));
					newNode.setProperty("Company", project.get(2));
					
					tx.success();
				} catch (Exception e){e.printStackTrace();}
			}
			
			System.out.println("creating relationships");
			createRelationships(db);
			System.out.println("created relationships");

		}
		catch (Exception e){e.printStackTrace();}
		
		finally{if (db != null) db.shutdown();}

	}

	private static List<List<String>> readProjects(File projectFile) {
		//0 project name, 1 employee name, 2 company name
		BufferedReader br = null;
		String line = "";
		try{
			br = new BufferedReader(new FileReader(projectFile));
			List<List<String>> out = new ArrayList<>();
			while ((line = br.readLine()) != null)
			{
				String[] line_data = line.split(";");
				List<String> inner = new ArrayList<String>();
				for (String elem : line_data){
					inner.add(elem);
				}
				out.add(inner);
			}
			br.close();
			return out;
			
		} catch (Exception e){e.printStackTrace(); }
		return null;
	}

	private static List<List<String>> readEmployees(File employeeFile){
		// 0 employee name, 1 company name, 2 years
		BufferedReader br = null;
		String line = "";
		
		try{
			br = new BufferedReader(new FileReader(employeeFile));
			List<List<String>> out = new ArrayList<>();
			while ((line = br.readLine()) != null)
			{
				List<String> inner = new ArrayList<String>();
				String[] line_data = line.split(";");
				for (String elem : line_data){
					inner.add(elem);
				}
				out.add(inner);
			}
			br.close();
			return out;
			
		} catch (Exception e){e.printStackTrace(); }
		return null;
	}
	
	private static List<List<String>> readCompanies(File companyFile) {
		//0 company name, 1 industry
				BufferedReader br = null;
				String line = "";
				try{
					br = new BufferedReader(new FileReader(companyFile));
					List<List<String>> out = new ArrayList<>();
					while ((line = br.readLine()) != null)
					{
						List<String> inner = new ArrayList<String>();
						String[] line_data = line.split(";");
						for (String elem : line_data){
							inner.add(elem);
						}
						out.add(inner);
					}
					br.close();
					return out;
					
				} catch (Exception e){e.printStackTrace(); }
				return null;
	}
	
	private static void createRelationships(GraphDatabaseService db)
	{
		String queryWorksFor = "match (p:Person), (c:Company) where p.Company = c.Name create (p) - [r:WORKS_FOR] -> (c) return r";
		String queryWorksWith = "match (p1:Person), (p2:Person) where p1.Company = p2.Company and p1 <> p2 create (p1) - [r:WORKS_WITH] -> (p2) return r";
		String queryBelongsTo = "match (proj: Project), (c:Company) where proj.Company = c.Name create (proj) - [r:BELONGS_TO] -> (c) return r";
		String queryWorksOn = "match (p:Person), (proj:Project) where p.Name = proj.Employee create (p) - [r:WORKED_ON] -> (proj) return r";
		try (Transaction tx = db.beginTx()){
			Result result = db.execute(queryWorksFor);
			tx.success();
		}
		try (Transaction tx = db.beginTx()){
			Result result = db.execute(queryWorksWith);
			tx.success();
		}
		try (Transaction tx = db.beginTx()){
			Result result = db.execute(queryBelongsTo);
			tx.success();
		}
		try (Transaction tx = db.beginTx()){
			Result result = db.execute(queryWorksOn);
			tx.success();
		}
	}

}
