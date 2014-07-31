package controllers;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.AskTimeoutException;

import com.mnemotix.mnemokit.semweb.Format;
import com.mnemotix.semweb.KgramActorRoundRobin;

import play.Logger;
import play.data.Form;
import play.libs.Akka;
import play.libs.F.Function;
import play.mvc.*;

import static akka.pattern.Patterns.ask;

import models.DataSourceConf;
import models.Query;
import models.LoadConf;
import models.ResponseFormat;

public class Sparql extends Controller {

	static boolean rdfsEntailment = false;

	static final ActorSystem system = ActorSystem.create();
	static ActorRef kgramActor = system.actorOf( new Props(KgramActorRoundRobin.class), "kgramActor");	
	static Form<Query> queryForm = Form.form(Query.class);
	static Form<LoadConf> loadForm = Form.form(LoadConf.class);
	static Form<DataSourceConf> datasourceForm = Form.form(DataSourceConf.class);

	private static long LOAD_TIMEOUT = 120000;
	private static long ADMIN_TIMEOUT = 10000;
	
    public static Result index() {
    	return ok(
		views.html.sparql.index.render(
				new Query(
						"select * \n" +
						"where {?x a ?type} \n" +
						"limit 10", "json", "gTable" )));
    }
    
    public static Result stopQuery(String queryId){
    	return getPromiseKgramActor(new KgramActorRoundRobin.StopQuery(queryId), ADMIN_TIMEOUT);
    }
    
	public static Result status() {
		return getPromiseKgramActor(new KgramActorRoundRobin.Status(), ADMIN_TIMEOUT);
	}

	public static Result setDQPMode(boolean dqpMode) {
		return getPromiseKgramActor(new KgramActorRoundRobin.SetDQPMode(dqpMode), ADMIN_TIMEOUT);
	}
    public static Result admin(){
    	return ok(views.html.sparql.admin.render());
    }
	
    public static Result load(){
    	Form<LoadConf> filledLoad = loadForm.bindFromRequest(); 
    	if(!filledLoad.hasErrors()){
	    	LoadConf load = filledLoad.get();
	    	return getPromiseKgramActor(load, LOAD_TIMEOUT);
    	}
    	return status();
    }
    
    // Controller for the SPARQL Protocol
    public static Result sparqlQuery(){
    	Form<Query> filledForm = queryForm.bindFromRequest(); 
    	if(!filledForm.hasErrors()){
    		Query query = filledForm.get();
    		
    		if(StringUtils.isBlank(query.format)){
        		//'format' or 'output' parameter are not specified
    			//Define response format with content negotiation
    			Logger.debug(request().getHeader("Accept"));
    			if(request().accepts("application/sparql-results+json")){
	        		response().setContentType("application/json");
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
    		Logger.debug("query.getTimeout(): " + query.getTimeout());
			return getPromiseKgramActor(query, query.getTimeout());
    	}
    	return ok(
    			views.html.sparql.index.render(
    					new Query(
    							"select * \n" +
    							"where {?x a ?type} \n" +
    							"limit 10", "json", "gTable" )));
    }

	
	 public static Result reset() {
		 return getPromiseKgramActor(new KgramActorRoundRobin.Reset(), ADMIN_TIMEOUT);
	 }
	 
	 public static Result removeDataSource(){
	    	Form<DataSourceConf> dataSourceConf = datasourceForm.bindFromRequest(); 
	    	if(!dataSourceConf.hasErrors()){
	    		return getPromiseKgramActor(new KgramActorRoundRobin.RemoveDataSource(dataSourceConf.get().getEndpoint()), ADMIN_TIMEOUT);
	    	}
	    	return status();
	 }
	 
	 public static Result addDataSource() {
    	Form<DataSourceConf> dataSourceConf = datasourceForm.bindFromRequest(); 
    	if(!dataSourceConf.hasErrors()){
    		return getPromiseKgramActor(new KgramActorRoundRobin.AddDataSource(dataSourceConf.get().getEndpoint()), ADMIN_TIMEOUT);
    	}
    	return status();
	 }
	
	public static Result getPromiseKgramActor(Object message, long timeout){
		return async(Akka.asPromise(ask(kgramActor, message, timeout)).recover(
			    new Function<Throwable, Object>() {
					@Override
					public Object apply(Throwable t) throws Throwable {
						System.out.println(t.getMessage());
			            if( t instanceof AskTimeoutException) {
			            	kgramActor.tell(t, null);
			                return "Timeout";
			            }
			            else {
			            	return "Got Exception: " + t.getMessage();
			            }
					}
			    }).map(
    			new Function<Object,Result>() {
	    		        public Result apply(Object response) {
	    		        	if(response instanceof ResponseFormat){
	    		        		ResponseFormat responseFormat = (ResponseFormat) response;
	    		        		response().setContentType(responseFormat.getContentType());
	    		        		return ok(responseFormat.getContent().toString());
	    		        	}
	    		        	return ok(response.toString());
	    		        }
    		    }
			));
	}
	
}
