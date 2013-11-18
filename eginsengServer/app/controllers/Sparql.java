package controllers;

import com.mnemotix.mnemokit.semweb.CoreseManager;
import play.data.Form;
import play.mvc.*;

import models.Query;
import models.Load;

public class Sparql extends Controller {
  
	static Form<Query> queryForm = Form.form(Query.class);
	static Form<Load> loadForm = Form.form(Load.class);
	static CoreseManager queryManager = new CoreseManager("resources/rdf");
	
    public static Result index() {
        return TODO;
    }
    
    public static Result load(){

    	Form<Load> filledLoad = loadForm.bindFromRequest(); 
    	if(!filledLoad.hasErrors()){
	    	Load load = filledLoad.get();
	    	queryManager.loadDataSet(load.getRdfSourcePath(), load.getGraph());
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
    		query.setQueryManager(queryManager);
    		response().setContentType("application/json, text/json, text/plain; charset=utf-8"); //adapter en fonction du format
    		return ok(query.run());
    	}
    	return ok(
    			views.html.sparql.index.render(
    					new Query(
    							"select ?type (count(*) as ?c) \n " +
    							"where {?x a ?type} \n " +
    							"group by ?type \n " +
    							"order by desc(?c)", "json", "gTable" )));
    }
    
  
}
