package controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mnemotix.mnemokit.semweb.Format;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import play.data.Form;
import play.mvc.*;

import models.Query;
import models.LoadConf;

public class Sparql extends Controller {
  
	static boolean DQPMode = false;

	static final boolean RDFS_ENTAILMENT = false;
	
	static Form<Query> queryForm = Form.form(Query.class);
	static Form<LoadConf> loadForm = Form.form(LoadConf.class);
	static Graph graph = Graph.create(RDFS_ENTAILMENT);	

	static Graph graphDQP = Graph.create(RDFS_ENTAILMENT);	
	public static Map<String, URL> dqpEndpoints = new HashMap<String, URL>();
	
    public static Result index() {
        return TODO;
    }

	public static Result status() {
		String jsonStatus = "{ \"dqpMode\":"+DQPMode + ", \"datasources\":" +dqpEndpoints.values()+"}";
		return ok(jsonStatus);
	}
    
	public static boolean isDQPMode() {
		return DQPMode;
	}

	public static Result setDQPMode(boolean dqpMode) {
		DQPMode = dqpMode;
		return status();
	}
    
    public static Result load(){
    	Form<LoadConf> filledLoad = loadForm.bindFromRequest(); 
    	if(!filledLoad.hasErrors()){
	    	LoadConf load = filledLoad.get();
	    	loadDataSet(load.getRdfSourcePath(), load.getGraph());
	    	return ok(String.valueOf(true));
    	}
    	return ok(views.html.sparql.load.render());
    }
    
    public static Result sparqlQuery(){
    	System.out.println("QUERY");
    	Form<Query> filledForm = queryForm.bindFromRequest(); 
    	if(!filledForm.hasErrors()){
    		Query query = filledForm.get();
    		if(StringUtils.isBlank(query.format)){
    			System.out.println(request().getHeader("Accept"));
    			if(request().accepts("application/sparql-results+json")){
    				query.setFormat(Format.JSON.toString());
    			} else if(request().accepts("application/sparql-results+xml")){
        			query.setFormat(Format.XML.toString());
    			} else if(request().accepts("text/turtle")){
    				query.setFormat(Format.N3.toString());
    			} else if(request().accepts("text/csv")){
    				query.setFormat(Format.CSV.toString());
    			}
    			
    		}
    		if(query.chart != null){
    	    	return ok(
    	    			views.html.sparql.index.render(
    	    					new Query(query.query, query.format, query.chart)));
    		}
    		//response().setContentType("application/json, text/json, text/plain; charset=utf-8"); 
			//adapter en fonction du format
    		try {
    			response().setHeader("Access-Control-Allow-Origin", "*");     
				return ok(query(query));
				//.as("application/sparql-results+json")
			} catch (EngineException e) {
				return internalServerError(e.getMessage());
			}
    	}
    	return ok(
    			views.html.sparql.index.render(
    					new Query(
    							"select * \n" +
    							"where {?x a ?type} \n" +
    							"limit 10", "json", "gTable" )));
    }

	
	 public static Result reset() {
       try {
           graph = Graph.create(RDFS_ENTAILMENT);
           graphDQP = Graph.create(RDFS_ENTAILMENT);	
           dqpEndpoints.clear();
           System.out.println("Reinitialized KGRAM-DQP federation engine");
   			return status();
       } catch (Exception ex) {
           ex.printStackTrace();
           return internalServerError("Exception while reseting KGRAM-DQP");
       }
	 }
	 
	 public static Result removeDataSource(String endpoint){
	    if(dqpEndpoints.containsKey(endpoint)){
	    	dqpEndpoints.remove(endpoint);
	    }
		return status();
	 }
	 
	 public static Result addDataSource(String endpoint) {
        try {
        	if(!dqpEndpoints.containsKey(endpoint)){
	        	URL endpointURL = new URL(endpoint);
		        dqpEndpoints.put(endpoint, endpointURL);
        	}
	        System.out.println(endpoint+" added to the federation engine");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
		return status();
	 }
	 
	 public static Result addDataSourceIndex(){
	    return ok(views.html.sparql.addDataSource.render());
	 }
    
	
	public static void loadDataSet(String rdfSourcePath, String graphURI){
		Load load = Load.create(graph);
		if(graphURI != null) {
			load.load(rdfSourcePath, graphURI);	
		} else {
			load.load(rdfSourcePath);
		}
	}
	
	public static String query(Query query) throws EngineException {
		String result = null;
		Mappings map = null;
		System.out.println("isDQPMode(): "+isDQPMode());
		System.out.println("query.getQuery(): "+query.getQuery());
		if(isDQPMode()){
			Provider sProv = ProviderImpl.create();
			QueryProcessDQP execDQP = QueryProcessDQP.create(graphDQP, sProv, true);
			for(URL endpoint : dqpEndpoints.values()){
				System.out.println("addRemote "+endpoint);
				execDQP.addRemote(endpoint, WSImplem.REST);
			}
			execDQP.setDebug(true);
			map = execDQP.query(query.getQuery());
		}else{ 
			QueryProcess exec = QueryProcess.create(graph);
			map = exec.query(query.getQuery());
		}
		System.out.println(map);
		Object formattedResult = null;
		try{
			Format format = Format.valueOf(query.getFormat().toUpperCase());
			if(format == Format.JSON)
				formattedResult = JSONFormat.create(map);
			if(format == Format.CSV)
				formattedResult = CSVFormat.create(map);
			if(format == Format.XML)
				formattedResult = XMLFormat.create(map);
			if(format == Format.N3)
				formattedResult = TripleFormat.create(map);			
		}catch(Exception e){
			formattedResult = ResultFormat.create(map);
		}
		result = formattedResult.toString();
		return result;
	}
	
	
	
}
