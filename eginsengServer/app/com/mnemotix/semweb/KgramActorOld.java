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

import play.libs.Json;

import models.LoadConf;
import models.Query;
import models.ResponseFormat;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.AskTimeoutException;

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

public class KgramActorOld extends UntypedActor {


	public static final String STOPQUERY = "STOPQUERY";
	  
	public final int MAX_WORKERS = 3; //TODO


	static boolean DQPMode = false;

	static boolean rdfsEntailment = false;
	static Graph graph = Graph.create(rdfsEntailment);	

	static Graph graphDQP = Graph.create(false); //TODO voir avec Alban
	public static Map<String, URL> dqpEndpoints = new HashMap<String, URL>();
		
	Map<String, ActorRef> initialSenders = new HashMap<String, ActorRef>();
	Map<String, Query> runningQueries = new HashMap<String, Query>();
	Queue<Query> fifoWaitingQueries = new LinkedList<Query>();
	Map<String, ActorRef> runningWorkers = new HashMap<String, ActorRef>();
	
	
	@Override
	public void onReceive(Object message) throws Exception {
		System.out.println(message.getClass().getName());
		if (message instanceof models.Query) {
			Query query = (Query) message;
			initialSenders.put(query.getId(), getSender());
			if(runningWorkers.size() < MAX_WORKERS){
				ActorRef queryWorker = getContext().system().actorOf( new Props(Worker.class), "query-"+query.getId());
				runningWorkers.put(query.getId(), queryWorker);
				runningQueries.put(query.getId(), query);
				queryWorker.tell(message, this.getSelf());
			}
			else{ 
				fifoWaitingQueries.add(query);
			}
		} 
		else if (message instanceof LoadConf){
	    	LoadConf load = (LoadConf) message;
			initialSenders.put(load.getId(), getSender());
			ActorRef loadActor = getContext().system().actorOf( new Props(Worker.class), "load-"+load.getId());
			loadActor.tell(message, this.getSelf());
		}
		else if(message instanceof Reset){
		       try {
		    	   for(String queryId : runningQueries.keySet()){
		    		   stopQuery(queryId);
		    	   }
		           graph = Graph.create(rdfsEntailment);
		           //TODO handle fifoWaitingQueries;
		    	   
		       } catch (Exception ex) {
		           ex.printStackTrace();
		       }
		       getSender().tell(status(), getSelf());
		}
		else if(message instanceof AddDataSource){
			String endpoint = ((AddDataSource)message).getEndpoint();
		       try {
			       	if(!dqpEndpoints.containsKey(endpoint)){
				        	URL endpointURL = new URL(endpoint);
					        dqpEndpoints.put(endpoint, endpointURL);
			       	}
			        System.out.println(endpoint+" added to the federation engine");
		       } catch (MalformedURLException ex) {
		           ex.printStackTrace();
		       }
		       getSender().tell(status(), getSelf());
		}
		else if(message instanceof RemoveDataSource){
			String endpoint = ((RemoveDataSource)message).getEndpoint();
			 if(dqpEndpoints.containsKey(endpoint)){
			    	dqpEndpoints.remove(endpoint);
		    }
			 getSender().tell(status(), getSelf());
		}
		else if(message instanceof Status){
		       getSender().tell(status(), getSelf());
		}
		else if(message instanceof SetDQPMode){
			DQPMode = ((SetDQPMode)message).isDqpMode();
		    getSender().tell(status(), getSelf());
		}
		else if (message instanceof Done){
			Done done = (Done)message;
			ActorRef initialSender = initialSenders.remove(done.getId());
			System.out.println(done.getId() + " ==> "+initialSender);
			initialSender.tell(done.getResult(), this.getSelf());
			runningWorkers.remove(done.getId());
			if(done.getMessage() instanceof Query){
				Query query = (Query)done.getMessage();
				runningQueries.remove(query.getId());
			}
			if(!fifoWaitingQueries.isEmpty()){
				Query nextQuery = fifoWaitingQueries.remove();
				ActorRef nextQueryActor = getContext().system().actorOf( new Props(Worker.class), "query-"+nextQuery.getId());
				runningWorkers.put(nextQuery.getId(), nextQueryActor);
				runningQueries.put(nextQuery.getId(), nextQuery);
				nextQueryActor.tell(message, this.getSelf());
			}
		}
		else if (message instanceof StopQuery){
			StopQuery stop = (StopQuery) message;
			stopQuery(stop.getQueryId());
			getSender().tell(status(), this.getSelf());
		}
		else {
			System.out.println(message);
			unhandled(message);
		}
	}
	
	public void stopQuery(String queryId){
		ActorRef initialSender = initialSenders.remove(queryId);
		System.out.println("initialSender ==> " + queryId);
		ActorRef queryActor = runningWorkers.remove(queryId);
		//TODO handle case of query in fifo
		if(queryActor!= null){
			runningQueries.remove(queryId);
			queryActor.tell(PoisonPill.getInstance(), this.getSelf());
			initialSender.tell("STOPPED", this.getSelf());
		}
	}
	
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

		ArrayNode jsonWaiting = jsonStatus.arrayNode();
		for(Query query : fifoWaitingQueries){ //TODO check that the it does not empty the fifo
			jsonWaiting.add(query.toJSON());
		}
		jsonStatus.put("waiting", jsonWaiting);		

		return new ResponseFormat("application/json", jsonStatus);
	}
	
	public static class Worker extends UntypedActor {

		@Override
		public void onReceive(Object message) {
			if (message instanceof models.Query) {
				models.Query query = (models.Query) message;
				//Thread.sleep(10000);
				try{
					ResponseFormat result = query(query);
					getSender().tell(new Done(query.getId(), query, result), getSelf());
				}catch (EngineException e) {
					getSender().tell(new Done(query.getId(), query, e.getMessage()), getSelf());
				}
			}

			else if (message instanceof LoadConf){
		    	LoadConf load = (LoadConf) message;
				if(new File(load.getRdfSourcePath()).exists()){
			    	loadDataSet(load.getRdfSourcePath(), load.getGraph());
			    	getSender().tell(new Done(load.getId(), load, String.valueOf(true)), getSelf());
				}else{
			    	getSender().tell(new Done(load.getId(), load, String.valueOf(false)), getSelf());
				}
			}
			else {
				unhandled(message);
			}
		}
		

		
		public static ResponseFormat query(Query query) throws EngineException {
			String result = null;
			Mappings map = null;
			System.out.println("isDQPMode(): "+DQPMode);
			if(DQPMode){
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
			System.out.println("nb mappings:"+map.size());
			Object formattedResult = null;
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
