package controllers;

import com.mnemotix.semweb.Format;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import play.data.Form;
import play.mvc.*;

import models.Query;
import models.LoadConf;

public class Sparql extends Controller {
  
	static final boolean RDFSEntailment = true;
	
	static Form<Query> queryForm = Form.form(Query.class);
	static Form<LoadConf> loadForm = Form.form(LoadConf.class);

	static Graph graph = Graph.create(RDFSEntailment);	
	
    public static Result index() {
        return TODO;
    }
    
    public static Result load(){

    	Form<LoadConf> filledLoad = loadForm.bindFromRequest(); 
    	if(!filledLoad.hasErrors()){
	    	LoadConf load = filledLoad.get();
	    	loadDataSet(load.getRdfSourcePath(), load.getGraph());
	    	return ok("done");
    	}
    	return ok(views.html.sparql.load.render());
    }
    
    public static Result sparqlQuery(){
    	Form<Query> filledForm = queryForm.bindFromRequest(); 
    	if(!filledForm.hasErrors()){
    		Query query = filledForm.get();
    		if(query.chart != null){
    	    	return ok(
    	    			views.html.sparql.index.render(
    	    					new Query(query.query, query.format, query.chart)));
    		}
    		//response().setContentType("application/json, text/json, text/plain; charset=utf-8"); 
			//adapter en fonction du format
    		try {
				return ok(query(query, graph));
			} catch (EngineException e) {
				return internalServerError(e.getMessage());
			}
    	}
    	return ok(
    			views.html.sparql.index.render(
    					new Query(
    							"select ?type (count(*) as ?c) \n " +
    							"where {?x a ?type} \n " +
    							"group by ?type \n " +
    							"order by desc(?c)", "json", "gTable" )));
    }
    
	
	public static void loadDataSet(String rdfSourcePath, String graphURI){
		Load load = Load.create(graph);
		if(graphURI != null) {
			load.load(rdfSourcePath, graphURI);	
		} else {
			load.load(rdfSourcePath);
		}
	}
	
	public static String query(Query query, Graph graph) throws EngineException {
		String result = null;
		QueryProcess exec = QueryProcess.create(graph);
		Mappings map = exec.query(query.getQuery());
		Object formattedResult = null;
		Format format = Format.valueOf(query.getFormat().toUpperCase());
		if(format == Format.JSON)
			formattedResult = JSONFormat.create(map);
		if(format == Format.CSV)
			formattedResult = CSVFormat.create(map);
		if(format == Format.XML)
			formattedResult = XMLFormat.create(map);
		if(format == Format.RDF_XML)
			formattedResult = RDFFormat.create(map);
		if(formattedResult != null){
			result = formattedResult.toString();
		}else { 
			formattedResult = JSONFormat.create(map);
		}
		return result;
	}

}
