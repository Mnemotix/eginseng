package controllers;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.AskTimeoutException;

import com.mnemotix.mnemokit.semweb.Format;
import com.mnemotix.semweb.KgramActor;
import play.data.Form;
import play.libs.Akka;
import play.libs.F.Function;
import play.mvc.*;

import static akka.pattern.Patterns.ask;

import models.Query;
import models.LoadConf;

public class Sparql extends Controller {

	static boolean rdfsEntailment = false;

	static final ActorSystem system = ActorSystem.create();
	static ActorRef kgramActor = system.actorOf( new Props(KgramActor.class), "kgramActor");	;
	static Form<Query> queryForm = Form.form(Query.class);
	static Form<LoadConf> loadForm = Form.form(LoadConf.class);

	private static long QUERY_TIMEOUT = 120000;
	private static long LOAD_TIMEOUT = 120000;
	private static long ADMIN_TIMEOUT = 10000;
	
    public static Result index() {
    	return TODO;
    }
    
    public static Result stopQuery(String queryId){
    	return getPromiseKgramActor(new KgramActor.StopQuery(queryId), ADMIN_TIMEOUT);
    }
    
	public static Result status() {
		return getPromiseKgramActor(new KgramActor.Status(), ADMIN_TIMEOUT);
	}

	public static Result setDQPMode(boolean dqpMode) {
		return getPromiseKgramActor(new KgramActor.SetDQPMode(dqpMode), ADMIN_TIMEOUT);
	}
    
    public static Result load(){
    	Form<LoadConf> filledLoad = loadForm.bindFromRequest(); 
    	if(!filledLoad.hasErrors()){
	    	LoadConf load = filledLoad.get();
	    	return getPromiseKgramActor(load, LOAD_TIMEOUT);
    	}
    	return ok(views.html.sparql.load.render());
    }
    
    public static Result sparqlQuery(){
    	Form<Query> filledForm = queryForm.bindFromRequest(); 
    	if(!filledForm.hasErrors()){
    		Query query = filledForm.get();
    		if(StringUtils.isBlank(query.format)){
    			System.out.println(request().getHeader("Accept"));
    			if(request().accepts("application/sparql-results+json")){
    				query.setFormat(Format.JSON.toString());
        			//response().setHeader("Content-Type", "application/sparql-results+json"); 
    			} else if(request().accepts("application/sparql-results+xml")){
        			query.setFormat(Format.XML.toString());
        			response().setHeader("Content-Type", "application/sparql-results+xml");   
    			} else if(request().accepts("text/turtle")){
    				query.setFormat(Format.N3.toString());
        			response().setHeader("Content-Type", "text/turtle");  
    			} else if(request().accepts("text/csv")){
    				query.setFormat(Format.CSV.toString());
        			response().setHeader("Content-Type", "text/csv");  
    			}
    			
    		}
    		if(query.chart != null){
    	    	return ok(
    	    			views.html.sparql.index.render(
    	    					new Query(query.query, query.format, query.chart)));
    		}

			return getPromiseKgramActor(query, QUERY_TIMEOUT );
    	}
    	return ok(
    			views.html.sparql.index.render(
    					new Query(
    							"select * \n" +
    							"where {?x a ?type} \n" +
    							"limit 10", "json", "gTable" )));
    }

	
	 public static Result reset() {
		 return getPromiseKgramActor(new KgramActor.Reset(), ADMIN_TIMEOUT);
	 }
	 
	 public static Result removeDataSource(String endpoint){
		 return getPromiseKgramActor(new KgramActor.RemoveDataSource(endpoint), ADMIN_TIMEOUT);
	 }
	 
	 public static Result addDataSource(String endpoint) {
		 return getPromiseKgramActor(new KgramActor.AddDataSource(endpoint), ADMIN_TIMEOUT);
	 }
	 
	 public static Result addDataSourceIndex(){
	    return ok(views.html.sparql.addDataSource.render());
	 }
	
	public static Result getPromiseKgramActor(Object message, long timeout){
		return async(Akka.asPromise(ask(kgramActor, message, timeout)).recover(
			    new Function<Throwable, Object>() {
					@Override
					public Object apply(Throwable t) throws Throwable {
			            if( t instanceof AskTimeoutException ) {
			                return internalServerError("Timeout");
			            }
			            else {
			                return internalServerError("Got Exception: " + t.getMessage());
			            }
					}
			    }).map(
    			new Function<Object,Result>() {
	    		        public Result apply(Object response) {
	    		          return ok(response.toString());
	    		        }
    		    }
			));
	}
	
}
