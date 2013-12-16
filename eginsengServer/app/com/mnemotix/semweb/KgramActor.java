package com.mnemotix.semweb;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;

import com.mnemotix.mnemokit.semweb.Format;

public class KgramActor {

	public static final int MAX_WORKERS = 10;

	public static Graph graph;
	public static Load load;
	public static final Format defaultFormat = Format.JSON;
	
	public static void init(Boolean rdfsEntailment){
		graph = Graph.create(rdfsEntailment);	
		load = Load.create(graph);
	}
	
	public static void query(models.Query query){
		run(query);
	}
	
	public static void load(models.LoadConf load){
		run(load);
	}

	private static void run(Object message) {
		KgramActor task = new KgramActor();
		// Create an Akka system
		final ActorSystem system = ActorSystem.create(task.getClass().getSimpleName());
		
		
		ActorRef master = system.actorOf( new Props(Master.class));

		
		// start the process
		master.tell(message, null);

	}
	
	public static class Master extends UntypedActor {
		
		private final ActorRef workerRouter;
		
		public Master(){
			workerRouter = this.getContext().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(MAX_WORKERS)), "workerRouter");
		}
		
		@Override
		public void onReceive(Object message) throws Exception {
			if (message instanceof models.Query) {
				workerRouter.tell(message, this.getSelf());
			}
			else if (message instanceof models.LoadConf) {
				workerRouter.tell(message, this.getSelf());
			} else {
				unhandled(message);
			}
		}
	}
	
	public static class Worker extends UntypedActor {

		@Override
		public void onReceive(Object message) throws Exception {
			if (message instanceof models.Query) {
				models.Query query = (models.Query) message;
				query(query.getQuery(), KgramActor.graph, Format.valueOf(query.getFormat()));
			}
			else if (message instanceof models.LoadConf) {
				
				
			} else {
				unhandled(message);
			}
		}
		
		public void loadDataSet(String rdfSourcePath, String graphURI){
			if(graphURI != null) {
				load.load(rdfSourcePath, graphURI);	
			} else {
				load.load(rdfSourcePath);
			}
		}
		
		public String query(String query, Graph graph, Format format) throws EngineException {
			String result = null;
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			Object formattedResult = null;
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
	

	
}
