package com.mnemotix.ginseng.fedEHR.crawl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.globus.gsi.CredentialException;

import play.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;

import com.hp.hpl.jena.vocabulary.RDFS;
import com.mnemotix.ginseng.fedEHR.ApiManager;
import com.mnemotix.ginseng.fedEHR.rdf.RDFExporter;
import com.mnemotix.ginseng.vocabulary.SemEHR;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.maatg.pandora.clients.commons.exception.ClientError;
import fr.maatg.pandora.clients.fedehr.exception.InvalidDataError;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRObjectFactory;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRTypeUtils;
import fr.maatg.pandora.ns.idal.ClinicalVariableType;
import fr.maatg.pandora.ns.idal.ClinicalVariableTypeRelatedClinicalVariableType;
import fr.maatg.pandora.ns.idal.MedicalBag;
import fr.maatg.pandora.ns.idal.MedicalEvent;
import fr.maatg.pandora.ns.idal.MedicalEventTypeContainedCVT;
import fr.maatg.pandora.ns.idal.Patient;
import fr.maatg.pandora.ns.idal.Patients;
import fr.maatg.pandora.ns.idal.QLimitedPatient;
import fr.maatg.pandora.ns.idal.ServerError;

public class AkkaCrawler {

	// Define the maximum number of request line that FedEHR accept to return
	// Each request with a page size higher than this value will be limited to it
	public static final int MAX_FEDEHR_LINE = 10000;

	// Define the maximum number of workers
	public static final int MAX_WORKERS = 10;
	
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
			String caPathPattern = "file:"+properties.getProperty("capattern");
			long receiveTimeout = Long.parseLong(properties.getProperty("receivetimeout")); 
			String outputDir = properties.getProperty("outputdir"); 
			run(nbOfWorkers, pwd, fedEHRServiceURL, certFile, keyFile, caPathPattern, receiveTimeout, outputDir);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param nbOfWorkers the number of workers
	 * @param pwd the password of the certificate
	 * @param fedEHRServiceURL the url of the node
	 * @param certFile path to the certificate on the server
	 * @param keyFile path to the key file on the server
	 * @param caPathPattern path to the directory with authentication chain files
	 * @param receiveTimeout timeout
	 * @param outputDir the directory in which the rdf files will be written
	 */
	private static void run(int nbOfWorkers, String pwd,
			String fedEHRServiceURL, String certFile, String keyFile,
			String caPathPattern, long receiveTimeout, String outputDir) {
		try{
			//Get the connection to fedEHR Node
			final FedEHRConnection fedConnection = new FedEHRConnection(fedEHRServiceURL, certFile, keyFile, caPathPattern, pwd, receiveTimeout);
			Logger.debug(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
			
			AkkaCrawler task = new AkkaCrawler();
			// Create an Akka system
			final ActorSystem system = ActorSystem.create(task.getClass().getSimpleName());
			
			//Count the total number of patients for splitting them between workers
			final int total = ApiManager.countAllPatients(fedConnection);
			//final int total = 100; //pour test
			final int limit = total / nbOfWorkers;
			
			ActorRef master = system.actorOf( new Props(Master.class));
			// start the process
			master.tell(new Go(fedConnection, nbOfWorkers, total, limit, outputDir), null);

		} catch (CredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class Master extends UntypedActor {
		
		private int remaining;
		Map<ClinicalVariableType, String> handledClinicalVariableTypes = new HashMap<ClinicalVariableType, String>();
		private final ActorRef workerRouter;
		String hospitalNode = null;
		Go go;
		
		public Master(){
			workerRouter = this.getContext().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(MAX_WORKERS)), "workerRouter");
		}
		
		@Override
		public void onReceive(Object message) throws Exception {
			if (message instanceof Go) {
				this.go = (Go) message;
				remaining = go.getNbchuncks();
				FedEHRConnection fedConnection = go.getFedConnection();
				hospitalNode = fedConnection.fedEHRPortType.getLocalHospitalNodeName("");
				for (int i = 0; i < go.getNbchuncks(); i++) {
					int offset = i * go.getLimit();
					int limit = (go.getLimit() < MAX_FEDEHR_LINE)? go.getLimit() : MAX_FEDEHR_LINE;
					//init the patient query
					QLimitedPatient  qLimitedPatient = ApiManager.getQLimitedPatient(fedConnection, hospitalNode, limit, offset, true, true);
					//open the writer
					Writer writer = new FileWriter(go.getOutputDir()+File.separator+hospitalNode+"_"+offset+".ttl");
					//Send patient query to the worker router
					workerRouter.tell(new WorkQLimitedPatient(fedConnection, qLimitedPatient, writer, go.getLimit(), offset), getSelf());
				}
			}
			else if (message instanceof ClinicalVariableType){
				ClinicalVariableType clinicalVariableType = (ClinicalVariableType) message;
				if(!handledClinicalVariableTypes.containsKey(clinicalVariableType)){
					//build the tree of clinical variables
					for (ClinicalVariableTypeRelatedClinicalVariableType clinicalVariableTypeRelatedClinicalVariableType : clinicalVariableType.getRelatedClinicalVariableType()) {
						handleClinicalVariableType(clinicalVariableTypeRelatedClinicalVariableType.getClinicalVariableType());
					}
				}
			}
			else if (message instanceof Done) {
				if (--this.remaining == 0) {
					//Generate the rdf for clinical variable taxonomy
					Writer cvtWriter = new FileWriter(go.getOutputDir()+File.separator+"cvt"+hospitalNode+".ttl");
					RDFExporter rdfExporter = new RDFExporter();
					for(ClinicalVariableType clinicalVariableType : handledClinicalVariableTypes.keySet()){
						String cvtURI = rdfExporter.buildClinicalVariableTypeURI(clinicalVariableType);
						rdfExporter.writeTripleURIValue(cvtWriter, cvtURI, RDF.TYPE, RDFS.Class.getURI());			
						rdfExporter.writeTripleURIValue(cvtWriter, cvtURI, RDFS.subClassOf.getURI(), SemEHR.CLINICAL_VARIABLE.getURI());
						rdfExporter.writeTripleLiteralValue(cvtWriter, cvtURI, RDFS.label.getURI(), rdfExporter.buildLiteralValue(clinicalVariableType.getName(), DatatypeMap.xsdstring));
						for (ClinicalVariableTypeRelatedClinicalVariableType clinicalVariableTypeRelatedClinicalVariableType : clinicalVariableType.getRelatedClinicalVariableType()) {
							String subCvtURI = rdfExporter.buildClinicalVariableTypeURI(clinicalVariableTypeRelatedClinicalVariableType.getClinicalVariableType());
							rdfExporter.writeTripleURIValue(cvtWriter, subCvtURI, RDFS.subClassOf.getURI(), cvtURI);
						}
					}
					cvtWriter.close();
					this.getContext().system().shutdown();
				}
			} else {
				unhandled(message);
			}
		}
		
		private void handleClinicalVariableType(ClinicalVariableType clinicalVariableType){
			if(!handledClinicalVariableTypes.containsKey(clinicalVariableType)){
				handledClinicalVariableTypes.put(clinicalVariableType, new RDFExporter().buildClinicalVariableTypeURI(clinicalVariableType));
				for (ClinicalVariableTypeRelatedClinicalVariableType clinicalVariableTypeRelatedClinicalVariableType : clinicalVariableType.getRelatedClinicalVariableType()) {
					handleClinicalVariableType(clinicalVariableTypeRelatedClinicalVariableType.getClinicalVariableType());
				}
			}
		}
	}
	
	public static class Worker extends UntypedActor {

		@Override
		public void onReceive(Object message) throws Exception {
			if(message instanceof WorkPatients){ 
				// I think that block is dead code, should be removed
				WorkPatients workPatients = (WorkPatients) message;
				Patients patients = workPatients.getPatients();
				//Get the patient list and iterate over it
				List<Patient> patientList = patients.getPatient();
				for(Patient patient : patientList){
					handlePatient(workPatients.getFedConnection(), workPatients.getWriter(), patient);
				}
				workPatients.getWriter().close();
				
				getSender().tell(new Done(message), getSelf());
			} 
			else if(message instanceof WorkQLimitedPatient){
				WorkQLimitedPatient workQLimitedPatient = (WorkQLimitedPatient) message;
				int offset = workQLimitedPatient.getOffset();
				int limit = (workQLimitedPatient.getLimit() < MAX_FEDEHR_LINE)? workQLimitedPatient.getLimit() : MAX_FEDEHR_LINE;
				QLimitedPatient  qLimitedPatient = workQLimitedPatient.getQLimitedPatient();// ApiManager.getQLimitedPatient(workQLimitedPatient.getFedConnection(), limit, offset, true, true);
				Patients patients;
				//Iterate over patient pages
				do{
					Logger.debug("worker"+workQLimitedPatient.getOffset() + " offset: "+offset+" limit: "+limit);
					// request patients
					patients = workQLimitedPatient.getFedConnection().fedEHRPortType.listPatients(qLimitedPatient);
					Logger.debug("worker"+workQLimitedPatient.getOffset() +" patients.getPatient().size(): "+patients.getPatient().size());
					//Get the patient list
					List<Patient> patientList = patients.getPatient();
					for(Patient patient : patientList){
						handlePatient(workQLimitedPatient.getFedConnection(), workQLimitedPatient.getWriter(), patient);
					}
					// update the query in order to ask for next page
					qLimitedPatient.setLimits(patients.getNextLimits());
					offset = patients.getNextLimits().getLimitObjectByNode().get(0).getOffset();
					limit = patients.getNextLimits().getLimitObjectByNode().get(0).getLimit();
				} while(!patients.getNextLimits().isFinished() 
						&& (offset < (workQLimitedPatient.getLimit() + workQLimitedPatient.getOffset())));
				workQLimitedPatient.getWriter().close();
				getSender().tell(new Done(message), getSelf());
			} 
			else {
				unhandled(message);
			}

		}
		
		/**
		 * This function crawl the medical bags and personal information of a patient
		 * @param fedConnection
		 * @param writer
		 * @param rdfExporter
		 * @param patient
		 * @throws IOException
		 * @throws ServerError
		 */
		private void handlePatient(FedEHRConnection fedConnection,
				Writer writer, Patient patient)
				throws IOException, ServerError {
			RDFExporter rdfExporter = new RDFExporter();
			

			// Write patient informations and medical information 
			// into the given writer and get it's URI
			// look to this function to understand/modify the generation of the RDF
			String patientUrl = rdfExporter.patient2RDF(patient, writer);
			
			// look for top clinical variable types and send it
			// to the master that will use it to build clinical variable taxonomy
			// iterate over the medical bags of the patient
			for(MedicalBag medicalBag :  patient.getMedicalBag()){
				//iterate of the medical event of the current medical bag
				for(MedicalEvent medicalEvent: medicalBag.getMedicalEvents()){
					try {
						FedEHRTypeUtils fedEHRTypeUtils = new FedEHRTypeUtils(new FedEHRObjectFactory(fedConnection.fedEHRPortType));
						MedicalEventTypeContainedCVT topMETCCVT = fedEHRTypeUtils.getTopElementWithChildren(medicalEvent.getMedicalEventType()).get(0);
						ClinicalVariableType topCCVT = topMETCCVT.getClinicalVariableType().getValue();

						// send top clinicical variable to master that will 
						getSender().tell(topCCVT, getSelf());
					} catch (InvalidDataError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
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
		private final String outputDir;
		private final FedEHRConnection fedConnection;

		public Go(FedEHRConnection fedConnection, int nbOfWorkers, int total, int limit, String outputDir) {
			this.fedConnection = fedConnection;
			this.nbOfWorkers = nbOfWorkers;
			this.nbchuncks = Math.round((float) total / (float) limit);
			this.total = total;
			this.limit = limit;
			this.outputDir = outputDir;
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

		public String getOutputDir() {
			return outputDir;
		}

	} 

	static class Finished { // simple message to stop the process
	}

	static class Done {
		private final Object message;

		public Done(Object message) {
			this.message = message;
		}

		public Object getMessage() {
			return message;
		}
	}



	static class WorkQLimitedPatient {
		private final QLimitedPatient qLimitedPatient;
		private final FedEHRConnection fedConnection;
		private final int limit;
		private final int offset;
		private final Writer writer;

		public WorkQLimitedPatient(FedEHRConnection fedConnection, QLimitedPatient qLimitedPatient, Writer writer, int limit, int offset) {
			this.fedConnection = fedConnection;
			this.qLimitedPatient = qLimitedPatient;
			this.writer = writer;
			this.limit = limit;
			this.offset = offset;
		}

		public QLimitedPatient getQLimitedPatient() {
			return qLimitedPatient;
		}

		public FedEHRConnection getFedConnection() {
			return fedConnection;
		}

		public Writer getWriter() {
			return writer;
		}

		public int getLimit() {
			return limit;
		}

		public int getOffset() {
			return offset;
		}
	}
	
	static class WorkPatients {
		private final Patients patients;
		private final FedEHRConnection fedConnection;
		private final Writer writer;

		public WorkPatients(FedEHRConnection fedConnection, Patients patients, Writer writer) {
			this.fedConnection = fedConnection;
			this.patients = patients;
			this.writer = writer;
		}

		public Patients getPatients() {
			return patients;
		}

		public FedEHRConnection getFedConnection() {
			return fedConnection;
		}

		public Writer getWriter() {
			return writer;
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
