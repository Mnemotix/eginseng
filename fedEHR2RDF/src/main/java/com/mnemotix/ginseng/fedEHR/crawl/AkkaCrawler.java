package com.mnemotix.ginseng.fedEHR.crawl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.globus.gsi.CredentialException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;

import com.mnemotix.ginseng.fedEHR.ApiManager;


import fr.maatg.pandora.clients.commons.exception.ClientError;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.ns.idal.ServerError;

public class AkkaCrawler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Handle parameters
		String propertyFileName = args[0];
		final int nbOfWorkers = Integer.valueOf(args[1]);
		run(nbOfWorkers, propertyFileName);
	}
	
	
	public static void run(int nbOfWorkers, String propertyFileName){
		try{
			Properties properties = new Properties();
			FileReader fileReader = new FileReader(propertyFileName);
			properties.load(fileReader);
			String pwd = properties.getProperty("pwd");
			String fedEHRServiceURL = properties.getProperty("fedehr.service.url");
			String certFile = properties.getProperty("usercert");
			String keyFile = properties.getProperty("userkey");
			String caPathPattern = "file:"+properties.getProperty("capath.pattern.path");
			long receiveTimeout = Long.parseLong(properties.getProperty("receivetimeout")); //10 minutes
			final FedEHRConnection fedConnection = new FedEHRConnection(fedEHRServiceURL, certFile, keyFile, caPathPattern, pwd, receiveTimeout);
			System.out.println(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
			
			AkkaCrawler task = new AkkaCrawler();
//TODO count patient	final int nbtags = task.getNbTags();

			// Create an Akka system
			final ActorSystem system = ActorSystem.create(task.getClass().getSimpleName());
			
			// create the worker
			final int total = ApiManager.countAllPatients(fedConnection);
			final int limit = total / nbOfWorkers;
			
			ActorRef master = system.actorOf(Props.create(Master.class, nbOfWorkers));

			
			// start the process
			master.tell(new Go(fedConnection, nbOfWorkers, total, limit), null);

		} catch (CredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class Master extends UntypedActor {
		
		private int processed = 0;
		private int remaining;

		private final ActorRef workerRouter;
		
		public Master(int nbOfWorkers){
			remaining = nbOfWorkers;
			workerRouter = this.getContext().actorOf(Props.create(Worker.class).withRouter(new RoundRobinRouter(nbOfWorkers)), "workerRouter");
		}
		
		@Override
		public void onReceive(Object message) throws Exception {
			if (message instanceof Go) {
				Go go = (Go) message;

				for (int i = 0; i < go.getNbchuncks(); i++) {
					int offset = i * go.getLimit();
					workerRouter.tell(new Work(go.getFedConnection(), offset, go.getLimit()), getSelf());
				}

			} else if (message instanceof Done) {
				Done done = (Done) message;
				processed += done.getOffset();

				if (--this.remaining == 0) {
					this.getContext().system().shutdown();
				}
			} else {
				unhandled(message);
			}
		}
	}
	
	public static class Worker extends UntypedActor {

		@Override
		public void onReceive(Object message) throws Exception {
			if (message instanceof Work) {
				Work work = (Work) message;
				String hospitalNode = work.getFedConnection().fedEHRPortType.getLocalHospitalNodeName("");
				Writer writer = new FileWriter("src/main/resources/"+hospitalNode+"_"+work.getOffset());
				System.out.println(hospitalNode + work.getOffset());
				ApiManager.crawlPatientPage(work.getFedConnection(), writer, work.getLimit(),work.getOffset());
				writer.close();
				getSender().tell(new Done(work.getOffset()), getSelf());
			}else {
				unhandled(message);
			}

		}

	}
	
	
	/*
	 * -- MESSAGES --
	 */

	static class Go { // message to start the process

		private final int nbOfWorkers;
		private final int total;
		private final int limit;
		private final int nbchuncks;
		private final FedEHRConnection fedConnection;

		public Go(FedEHRConnection fedConnection, int nbOfWorkers, int total, int limit) {
			this.fedConnection = fedConnection;
			this.nbOfWorkers = nbOfWorkers;
			this.nbchuncks = Math.round((float) total / (float) limit);
			this.total = total;
			this.limit = limit;
		}

		public FedEHRConnection getFedConnection() {
			return fedConnection;
		}

		public int getTotal() {
			return total;
		}

		public int getLimit() {
			return limit;
		}

		public int getNbchuncks() {
			return nbchuncks;
		}

		public int getNbOfWorkers() {
			return nbOfWorkers;
		}

	} 

	static class Finished { // simple message to stop the process
	}

	static class Done {
		private final int offset;

		public Done(int offset) {
			this.offset = offset;
		}

		public int getOffset() {
			return offset;
		}
	}

	static class Work {
		private final int offset;
		private final int limit;
		private final FedEHRConnection fedConnection;

		public Work(FedEHRConnection fedConnection, int offset, int limit) {
			this.fedConnection = fedConnection;
			this.offset = offset;
			this.limit = limit;
		}

		public FedEHRConnection getFedConnection() {
			return fedConnection;
		}

		public int getLimit() {
			return limit;
		}

		public int getOffset() {
			return offset;
		}
	}
}
