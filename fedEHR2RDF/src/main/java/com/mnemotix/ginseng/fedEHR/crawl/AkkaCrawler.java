package com.mnemotix.ginseng.fedEHR.crawl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.time.StopWatch;
import org.globus.gsi.CredentialException;

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
import fr.maatg.pandora.ns.idal.Address;
import fr.maatg.pandora.ns.idal.ClinicalVariableType;
import fr.maatg.pandora.ns.idal.ClinicalVariableTypeRelatedClinicalVariableType;
import fr.maatg.pandora.ns.idal.MedicalBag;
import fr.maatg.pandora.ns.idal.MedicalBags;
import fr.maatg.pandora.ns.idal.MedicalEvent;
import fr.maatg.pandora.ns.idal.MedicalEventTypeContainedCVT;
import fr.maatg.pandora.ns.idal.Patient;
import fr.maatg.pandora.ns.idal.Patients;
import fr.maatg.pandora.ns.idal.QLimitedMedicalBag;
import fr.maatg.pandora.ns.idal.QLimitedPatient;
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
			// Create an Akka system
			final ActorSystem system = ActorSystem.create(task.getClass().getSimpleName());
			
			// create the worker
			final int total = ApiManager.countAllPatients(fedConnection);
			//final int total = 10; //pour test
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
		
		private int remaining;
		Map<ClinicalVariableType, String> handledClinicalVariableTypes = new HashMap<ClinicalVariableType, String>();
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
					FedEHRConnection fedConnection = go.getFedConnection();
					QLimitedPatient  qLimitedPatient = ApiManager.getQLimitedPatient(go.getFedConnection(), go.getLimit(), offset, true, true);
					Patients patients = fedConnection.fedEHRPortType.listPatients(qLimitedPatient);
					String hospitalNode = fedConnection.fedEHRPortType.getLocalHospitalNodeName("");
					Writer writer = new FileWriter("src/main/resources/"+hospitalNode+"_"+offset+".ttl");
					workerRouter.tell(new WorkPatients(fedConnection, patients, writer), getSelf());
				}
			}
			else if (message instanceof ClinicalVariableType){
				ClinicalVariableType clinicalVariableType = (ClinicalVariableType) message;
				if(!handledClinicalVariableTypes.containsKey(clinicalVariableType)){
					System.out.println(clinicalVariableType.getName()); 
					for (ClinicalVariableTypeRelatedClinicalVariableType clinicalVariableTypeRelatedClinicalVariableType : clinicalVariableType.getRelatedClinicalVariableType()) {
						handleClinicalVariableType(clinicalVariableTypeRelatedClinicalVariableType.getClinicalVariableType());
					}
				}
			}
			else if (message instanceof Done) {
				if (--this.remaining == 0) {
					Writer cvtWriter = new FileWriter("src/main/resources/cvt.ttl");
					RDFExporter rdfExporter = new RDFExporter();
					for(ClinicalVariableType clinicalVariableType : handledClinicalVariableTypes.keySet()){
						String cvtURI = rdfExporter.buildClinicalVariableTypeURI(clinicalVariableType);
						rdfExporter.writeTripleURIValue(cvtWriter, cvtURI, RDF.TYPE, RDFS.Class.getURI());			
						rdfExporter.writeTripleURIValue(cvtWriter, cvtURI, RDFS.subClassOf.getURI(), SemEHR.CLINICAL_VARIABLE.getURI());
						rdfExporter.writeTripleURIValue(cvtWriter, cvtURI, RDFS.label.getURI(), rdfExporter.buildLiteralValue(clinicalVariableType.getName(), DatatypeMap.xsdstring));
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
			if (message instanceof Work) {
				Work work = (Work) message;
				String hospitalNode = work.getFedConnection().fedEHRPortType.getLocalHospitalNodeName("");
				Writer writer = new FileWriter("src/main/resources/"+hospitalNode+"_"+work.getOffset()+".ttl");
				System.out.println(hospitalNode + work.getOffset());
				getPatients(work.getFedConnection(), writer, work.getLimit(),work.getOffset());
				writer.close();
				getSender().tell(new Done(work.getOffset()), getSelf());
			}
			else if(message instanceof WorkPatients){
				WorkPatients workPatients = (WorkPatients) message;
				Patients patients = workPatients.getPatients();
				List<Patient> patientList = patients.getPatient();
				for(Patient patient : patientList){
					handlePatient(workPatients.getFedConnection(), workPatients.getWriter(), patient);
				}
				workPatients.getWriter().close();
				getSender().tell(new Done(message), getSelf());
			} 
			else {
				unhandled(message);
			}

		}
		
		public boolean getPatients(FedEHRConnection fedConnection, Writer writer, int pageSize, int offset){
			try {

				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				
				QLimitedPatient  qLimitedPatient = ApiManager.getQLimitedPatient(fedConnection, pageSize, offset, true, true);

				Patients patients = fedConnection.fedEHRPortType.listPatients(qLimitedPatient);
				List<Patient> patientList = patients.getPatient();
				//for()
				getMedicalBags(fedConnection, writer, patientList);
				
				stopWatch.stop();
				System.out.println(stopWatch.getTime());


				return patients.getNextLimits().isFinished();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;

		}
		
		/**
		 * @param fedConnection
		 * @param writer
		 * @param rdfExporter
		 * @param patientList
		 * @throws IOException
		 * @throws ServerError
		 */
		public void getMedicalBags(
				FedEHRConnection fedConnection,
				Writer writer, 
				List<Patient> patientList) throws IOException, ServerError {
			for(Patient patient : patientList){
				handlePatient(fedConnection, writer, patient);
			}
		}

		/**
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
			String patientUrl = rdfExporter.patient2RDF(patient, writer);
			Address patientAddress = ApiManager.getAddress(fedConnection, patient);
			if(patientAddress != null){
				rdfExporter.writeTripleURIValue(
					writer,
					patientUrl, 
					SemEHR.ADDRESS.getURI(), 
					rdfExporter.address2RDF(patientAddress, writer));
			}
			for(MedicalBag medicalBag :  patient.getMedicalBag()){
				rdfExporter.medicalBag2RDF(medicalBag, writer);
				for(MedicalEvent medicalEvent: medicalBag.getMedicalEvents()){
					try {
						FedEHRTypeUtils fedEHRTypeUtils = new FedEHRTypeUtils(new FedEHRObjectFactory(fedConnection.fedEHRPortType));
						MedicalEventTypeContainedCVT topMETCCVT = fedEHRTypeUtils.getTopElementWithChildren(medicalEvent.getMedicalEventType()).get(0);
						ClinicalVariableType topCCVT = topMETCCVT.getClinicalVariableType().getValue();
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
		private final Object message;

		public Done(Object message) {
			this.message = message;
		}

		public Object getMessage() {
			return message;
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
