package controllers;

import com.mnemotix.mnemokit.semweb.CoreseManager;
import com.mnemotix.mnemokit.semweb.api.QueryManager;

import play.*;
import play.data.Form;
import play.mvc.*;

import models.Query;

import views.html.*;

public class Application extends Controller {
  
	static Form<Query> queryForm = Form.form(Query.class);
	//static QueryManager queryManager = new CoreseManager("resources/sample_dbpedia_person.rdf");
	
    public static Result index() {
        return TODO;
    }
    
    public static Result sparqlQuery(){
    	Form<Query> filledForm = queryForm.bindFromRequest(); 
    	if(!filledForm.hasErrors()){
    		Query query = filledForm.get();
    		QueryManager queryManager = new CoreseManager("resources/sample_dbpedia_person.rdf");
    		query.setQueryManager(queryManager);
    		response().setContentType("application/json, text/json, text/plain; charset=utf-8"); //adapter en fonction du format
    		return ok(query.run());
    	}
    	return ok(views.html.index.render(new Query("select * where {?x a ?Concept} limit 10")));
    }
  
}
