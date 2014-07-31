package com.mnemotix.semweb;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.libs.Json;

import models.LoadConf;
import models.Query;
import models.ResponseFormat;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.AskTimeoutException;
import akka.routing.RoundRobinRouter;

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

import com.mnemotix.mnemokit.semweb.Format;

/*
 * This Class has 2 actors for managing a KGRAM Instance
 * Master receive task, analyze it, allocate it to Workers, 
 * manage KGRAM state, and communicate with sender
 * 
 */
public class KgramActorRoundRobin extends UntypedActor {


	public static final String STOPQUERY = "STOPQUERY";
	  
	public final int MAX_WORKERS = 16; 


	static boolean DQPMode = false;

	static boolean rdfsEntailment = false;

	//Internal graph
	static Graph graph = Graph.create(rdfsEntailment);	

	//DQP Graph
	static Graph graphDQP = Graph.create(false); //TODO voir avec Alban pour l'inférence en DQP
	//Map of endpoints for DQP Mode
	public static Map<String, URL> dqpEndpoints = new HashMap<String, URL>();
		
	//Map of initialSenders for communicating with it
	Map<String, ActorRef> initialSenders = new HashMap<String, ActorRef>();
	//Map of running queries and actors for KGRAM Status or stopping queries
	Map<String, Query> runningQueries = new HashMap<String, Query>();
	public static Map<String, ActorRef> runningWorkers = new HashMap<String, ActorRef>();
	
	ActorRef router = getContext().system().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(MAX_WORKERS)));
	
	@Override
	public void onReceive(Object message) throws Exception {
		Logger.debug(message.getClass().getName());
		if (message instanceof models.Query) {
			// Allocate worker for query execution
			Query query = (Query) message;
			initialSenders.put(query.getId(), getSender());
			runningQueries.put(query.getId(), query);
			router.tell(message, this.getSelf());
		} 
		else if (message instanceof LoadConf){
			// Allocate worker for loading a graph
	    	LoadConf load = (LoadConf) message;
			initialSenders.put(load.getId(), getSender());
			router.tell(message, this.getSelf());
		}
		
		else if(message instanceof Reset){
			//Reset the state of KGRAM
		       try {
		    	   //Stop running queries
		    	   for(String queryId : runningQueries.keySet()){
		    		   stopQuery(queryId);
		    	   }
		    	   // init a new graph
		           graph = Graph.create(rdfsEntailment);
		           //TODO handle fifoWaitingQueries;
		    	   
		       } catch (Exception ex) {
		           ex.printStackTrace();
		       }
		       //Send status
		       getSender().tell(status(), getSelf());
		}
		else if(message instanceof AddDataSource){
			// Add a new data source for DQP Mode
			String endpoint = ((AddDataSource)message).getEndpoint();
		       try {
			       	if(!dqpEndpoints.containsKey(endpoint)){
				        	URL endpointURL = new URL(endpoint);
					        dqpEndpoints.put(endpoint, endpointURL);
			       	}
			        Logger.debug(endpoint+" added to the federation engine");
		       } catch (MalformedURLException ex) {
		           ex.printStackTrace();
		       }
		       getSender().tell(status(), getSelf());
		}
		else if(message instanceof RemoveDataSource){
			// Remove a new data source for DQP Mode
			String endpoint = ((RemoveDataSource)message).getEndpoint();
			 if(dqpEndpoints.containsKey(endpoint)){
			    	dqpEndpoints.remove(endpoint);
		    }
			 getSender().tell(status(), getSelf());
		}
		else if(message instanceof Status){
			//Send the status of KGRAM
		       getSender().tell(status(), getSelf());
		}
		else if(message instanceof SetDQPMode){
			// Set mode of KGRAM (internal graph or DQP)
			DQPMode = ((SetDQPMode)message).isDqpMode();
		    getSender().tell(status(), getSelf());
		}
		else if (message instanceof Done){
			// Worker has completed a task
			// update status and tell it to sender
			Done done = (Done)message;
			// Get task sender
			ActorRef initialSender = initialSenders.remove(done.getId());
			Logger.debug(done.getId() + " ==> "+initialSender);
			// Send task result
			initialSender.tell(done.getResult(), this.getSelf());
			if(done.getMessage() instanceof Query){
				// if task is a query, remove it from map of running queries
				Query query = (Query)done.getMessage();
				runningQueries.remove(query.getId());
			}
		}
		else if (message instanceof StopQuery){
			// If stop query task, send status update
			StopQuery stop = (StopQuery) message;
			stopQuery(stop.getQueryId());
			getSender().tell(status(), this.getSelf());
		}
		else if (message instanceof AskTimeoutException){
			// if sender timeout, it does not want to wait for query result anymore
			// stop query of the sender
			for(String queryId : initialSenders.keySet()){
				ActorRef currentSender = initialSenders.get(queryId);
				if(currentSender.isTerminated()){
					stopQuery(queryId);
				}
			}
		}
		else {
			unhandled(message);
		}
	}
	
	public void stopQuery(String queryId){
		ActorRef initialSender = initialSenders.remove(queryId);
		//System.out.println("initialSender ==> " + queryId);
		ActorRef queryActor = runningWorkers.remove(queryId);
		//TODO handle case of query in fifo
		if(queryActor!= null){
			runningQueries.remove(queryId);
			queryActor.tell(PoisonPill.getInstance(), this.getSelf());
			initialSender.tell("STOPPED", this.getSelf());
		}
	}
	
	//Function that format the JSON Status of KGRAM
	public ResponseFormat status() {
		ObjectNode jsonStatus = Json.newObject();
		jsonStatus.put("dqpMode", new Boolean(DQPMode));
		
		ArrayNode jsonDatasources = jsonStatus.arrayNode();
		for(URL endpoint : dqpEndpoints.values()){
			jsonDatasources.add(endpoint.toString());
		}
		jsonStatus.put("datasources", jsonDatasources);
		

		ArrayNode jsonRunning = jsonStatus.arrayNode();
		for(Query query : runningQueries.values()){
			jsonRunning.add(query.toJSON());
		}
		jsonStatus.put("running", jsonRunning);

/*		ArrayNode jsonWaiting = jsonStatus.arrayNode();
		for(Query query : fifoWaitingQueries){ //TODO check that the it does not empty the fifo
			jsonWaiting.add(query.toJSON());
		}
		jsonStatus.put("waiting", jsonWaiting);		
*/
		return new ResponseFormat("application/json", jsonStatus);
	}
	
	public static class Worker extends UntypedActor {

		@Override
		public void onReceive(Object message) {
			if (message instanceof Query) {
				// Execute query
				Query query = (models.Query) message;
				// Add me to running workers
				KgramActorRoundRobin.runningWorkers.put(query.getId(), getSelf());
				try{
					//Execute query
					ResponseFormat result = query(query);
					// Send result
					getSender().tell(new Done(query.getId(), query, result), getSelf());
				}catch (EngineException e) {
					getSender().tell(new Done(query.getId(), query, e.getMessage()), getSelf());
				}
			}

			else if (message instanceof LoadConf){
				// Load RDF data
		    	LoadConf load = (LoadConf) message;
				if(new File(load.getRdfSourcePath()).exists()){
					//File exists, it's ok to load it
			    	loadDataSet(load.getRdfSourcePath(), load.getGraph());
			    	// Tell sender we finished it
			    	getSender().tell(new Done(load.getId(), load, String.valueOf(true)), getSelf());
				}else{
					// We can't load it
			    	getSender().tell(new Done(load.getId(), load, String.valueOf(false)), getSelf());
				}
			}
			else {
				unhandled(message);
			}
		}
		

		//Function that execute a query
		public static ResponseFormat query(Query query) throws EngineException {
			String result = null;
			Mappings map = null;
			Logger.debug("isDQPMode(): "+DQPMode);
			if(DQPMode){
				//DQP Mode
				Provider sProv = ProviderImpl.create();
				QueryProcessDQP execDQP = QueryProcessDQP.create(graphDQP, sProv, true);
				for(URL endpoint : dqpEndpoints.values()){
					// add endpoints
					System.out.println("addRemote "+endpoint);
					execDQP.addRemote(endpoint, WSImplem.REST);
				}
				execDQP.setDebug(true); //TODO check if Debug is on
				//execute query
				map = execDQP.query(query.getQuery());
			}else{ 
				// Query internal graph
				QueryProcess exec = QueryProcess.create(graph);
				map = exec.query(query.getQuery());
			}
			Logger.debug("nb mappings:"+map.size());
			Object formattedResult = null;
			
			// Format results
			String contentType = "application/rdf+xml";
			try{
				Format format = Format.valueOf(query.getFormat().toUpperCase());
				if(format == Format.JSON){
					formattedResult = JSONFormat.create(map);
					contentType = "application/sparql-results+json";
				}
				if(format == Format.CSV){
					formattedResult = CSVFormat.create(map);	
					contentType = "text/csv";
				}
				if(format == Format.XML){
					contentType = "application/sparql-results+xml";
					formattedResult = XMLFormat.create(map);	
				}if(format == Format.N3){
					contentType = "text/turtle";
					formattedResult = TripleFormat.create(map);		
				}	
			}catch(Exception e){
				formattedResult = ResultFormat.create(map);
			}
			result = formattedResult.toString();
			return new ResponseFormat(contentType, result);
		}
		
		// Load rdf data set into internal graph
		public static void loadDataSet(String rdfSourcePath, String graphURI){
			Load load = Load.create(graph);
			if(graphURI != null) {
				load.load(rdfSourcePath, graphURI);	
			} else {
				load.load(rdfSourcePath);
			}
		}

	}
	
	public static class Done{
		String id;
		Object message;
		Object result;

		public Done(String id, Object message, Object result) {
			this.id = id;
			this.message = message;
			this.result = result;
		}
		
		public String getId() {
			return id;
		}

		public Object getResult() {
			return result;
		}

		public Object getMessage() {
			return message;
		}	
	}
	
	public static class StopQuery{
		String queryId;

		public StopQuery(String queryId) {
			this.queryId = queryId;
		}

		public String getQueryId() {
			return queryId;
		}
	}

	public static class Reset{
	}

	public static class Status{
	}


	public static class SetDQPMode{
		boolean dqpMode;
		
		public SetDQPMode(boolean dqpMode){
			this.dqpMode = dqpMode;
		}

		public boolean isDqpMode() {
			return dqpMode;
		}
	}

	public static class AddDataSource{
		String endpoint;
		
		public AddDataSource(String endpoint){
			this.endpoint = endpoint;
		}

		public String getEndpoint() {
			return endpoint;
		}
	}

	public static class RemoveDataSource{
		String endpoint;
		
		public RemoveDataSource(String endpoint){
			this.endpoint = endpoint;
		}

		public String getEndpoint() {
			return endpoint;
		}
	}
}
