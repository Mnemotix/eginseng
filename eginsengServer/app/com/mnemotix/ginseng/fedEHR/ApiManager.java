package com.mnemotix.ginseng.fedEHR;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;

import play.Logger;

import com.mnemotix.ginseng.fedEHR.rdf.RDFExporter;
import com.mnemotix.ginseng.vocabulary.SemEHR;

import fr.maatg.pandora.clients.fedehr.exception.InvalidDataError;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.ns.idal.Address;
import fr.maatg.pandora.ns.idal.MedicalBag;
import fr.maatg.pandora.ns.idal.MedicalBags;
import fr.maatg.pandora.ns.idal.MedicalEvent;
import fr.maatg.pandora.ns.idal.Patient;
import fr.maatg.pandora.ns.idal.Patients;
import fr.maatg.pandora.ns.idal.QAddress;
import fr.maatg.pandora.ns.idal.QCity;
import fr.maatg.pandora.ns.idal.QClinicalVariable;
import fr.maatg.pandora.ns.idal.QLimitObject;
import fr.maatg.pandora.ns.idal.QLimitObjectByNode;
import fr.maatg.pandora.ns.idal.QLimitedMedicalBag;
import fr.maatg.pandora.ns.idal.QLimitedPatient;
import fr.maatg.pandora.ns.idal.QMedicalBag;
import fr.maatg.pandora.ns.idal.QMedicalEvent;
import fr.maatg.pandora.ns.idal.QMedicalEventType;
import fr.maatg.pandora.ns.idal.QPatient;
import fr.maatg.pandora.ns.idal.ServerError;


//Initial class that was used for centralized crawl of a FedEHR node 
public class ApiManager {
	
	//Function that count all patients available through a given fedEHR Connection
	public static int countAllPatients(FedEHRConnection fedConnection){
		int count = 0;

		try {
			//init the query
			//hospitalNode is set to null in order to get all the patients from all hospitals
			//last parameter of getQLimitedPatient specify that's a count request
			QLimitedPatient qLimitedPatient = ApiManager.getQLimitedPatient(fedConnection, null, 1, 0, true, true);

			//call the service with listPatients
			Patients patients = fedConnection.fedEHRPortType.listPatients(qLimitedPatient);
			for(QLimitObjectByNode qLimitObjectByNode : patients.getNextLimits().getLimitObjectByNode()){
				count += qLimitObjectByNode.getTotalResults();
			}
			Logger.debug("total: " + count);

		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	/*
	//Function that starts the crawl of the patients with 
	//there medical bags that are available through a FedEHR Connection
	public static boolean crawlPatients(FedEHRConnection fedConnection, Writer writer, int pageSize, int offset){
		try {

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			RDFExporter rdfExporter = new RDFExporter();
			
			QLimitedPatient  qLimitedPatient = ApiManager.getQLimitedPatient(fedConnection, null, pageSize, offset, true, true);

			Patients patients = fedConnection.fedEHRPortType.listPatients(qLimitedPatient);
			List<Patient> patientList = patients.getPatient();
			getMedicalBags(fedConnection, writer, rdfExporter, patientList);

			qLimitedPatient.setLimits(patients.getNextLimits());		
			stopWatch.stop();
			Logger.debug("duration: "+stopWatch.getTime());


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
	*/

	/**
	 * Function that crawl all the medical bags of a given list of patient.
	 * Corresponding RDF descriptions are written in the writer
	 * @param fedConnection
	 * @param writer
	 * @param rdfExporter
	 * @param patientList
	 * @throws IOException
	 * @throws ServerError
	 */
	public static void getMedicalBags(
			FedEHRConnection fedConnection,
			Writer writer, 
			RDFExporter rdfExporter, 
			List<Patient> patientList) throws IOException, ServerError {
		for(Patient patient : patientList){
			String patientUrl = rdfExporter.patient2RDF(patient, writer);
			Address patientAddress = ApiManager.getAddress(fedConnection, patient);
			if(patientAddress != null){
				rdfExporter.writeTripleURIValue(
					writer,
					patientUrl, 
					SemEHR.ADDRESS.getURI(), 
					rdfExporter.address2RDF(patientAddress, writer));
			}
			
			QLimitedMedicalBag qLimitedMedicalBag = ApiManager.getQLimitedMedicalBag(fedConnection, patient, true);
			MedicalBags medicalBags;
			do{
				medicalBags = fedConnection.fedEHRPortType.listMedicalBags(qLimitedMedicalBag);
				for(MedicalBag medicalBag : medicalBags.getMedicalBag()){
					rdfExporter.medicalBag2RDF(medicalBag, writer);
					for(MedicalEvent medicalEvent: medicalBag.getMedicalEvents()){
						try {
							rdfExporter.navigateCVTFromMedicalEvent(fedConnection, medicalEvent);
						} catch (InvalidDataError e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				qLimitedMedicalBag.setLimits(medicalBags.getNextLimits());
			} while(!qLimitedMedicalBag.getLimits().isFinished());
		}
	}
	
	public static void crawlAllPatients(FedEHRConnection fedConnection, Writer writer, int pageSize){
		try {

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			RDFExporter rdfExporter = new RDFExporter();
			
			QLimitedPatient  qLimitedPatient = ApiManager.getQLimitedPatient(fedConnection, null, pageSize, true);
			Patients patients;
			do{
				patients = fedConnection.fedEHRPortType.listPatients(qLimitedPatient);
				for(Patient patient : patients.getPatient()){
					String patientUrl = rdfExporter.patient2RDF(patient, writer);
					Address patientAddress = ApiManager.getAddress(fedConnection, patient);
					if(patientAddress != null){
						rdfExporter.writeTripleURIValue(
							writer,
							patientUrl, 
							SemEHR.ADDRESS.getURI(), 
							rdfExporter.address2RDF(patientAddress, writer));
					}
					
					QLimitedMedicalBag qLimitedMedicalBag = ApiManager.getQLimitedMedicalBag(fedConnection, patient, true);
					MedicalBags medicalBags;
					do{
						medicalBags = fedConnection.fedEHRPortType.listMedicalBags(qLimitedMedicalBag);
						for(MedicalBag medicalBag : medicalBags.getMedicalBag()){
							/*for(MedicalEvent medicalEvent : medicalBag.getMedicalEvents()){
								rdfExporter.navigateCVTFromMedicalEvent(fedConnection, medicalEvent);
							}*/
							rdfExporter.medicalBag2RDF(medicalBag, writer);
						}
					
						qLimitedMedicalBag.setLimits(medicalBags.getNextLimits());
						
					} while(!qLimitedMedicalBag.getLimits().isFinished());
					
				}
				qLimitedPatient.setLimits(patients.getNextLimits());		
				stopWatch.split();
			} while(!patients.getNextLimits().isFinished());
			stopWatch.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static Address getAddress(FedEHRConnection fedEHRConnection, Patient patient) throws ServerError{
		QLimitedPatient qLimitedPatient = new QLimitedPatient();
		QPatient qPatient = new QPatient();
		qPatient.setID(String.valueOf(patient.getID()));
		QAddress qAddress = new QAddress();
		QCity qCity = new QCity();
		qAddress.setQCity(qCity);
		qPatient.setQAddress(qAddress);
		qLimitedPatient.setQPatient(qPatient);
		QLimitObject qLimitObject = new QLimitObject();
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(patient.getHospitalNode());  //pour tous les noeuds
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); //on peut spécifier une limit par noeud
		qLimitedPatient.setLimits(qLimitObject);
		Patients patients = fedEHRConnection.fedEHRPortType.listPatients(qLimitedPatient);
		if(patients.getPatient().size() > 0){
			Patient patientWithAddress = patients.getPatient().get(0);
			return patientWithAddress.getAddress();
		}
		return null;
	}

	public static QLimitedPatient getQLimitedPatient(FedEHRConnection fedConnection, String hospitalNode, int limit, boolean fillMediacalBag) throws ServerError{
		return getQLimitedPatient(fedConnection, hospitalNode, limit, 0, false, fillMediacalBag);
	}
	
	public static QLimitedPatient getQLimitedPatient(FedEHRConnection fedConnection, String hospitalNode, int limit, int offset, boolean fillMedicalBags, boolean countRequested) throws ServerError{
		QLimitedPatient qLimitedPatient = new QLimitedPatient(); 
		QPatient qPatient = new QPatient();
		if(hospitalNode != null){
			qPatient.setHospitalNode(hospitalNode);
		}else {
			qPatient.setHospitalNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
		}
		QMedicalBag qMedicalBag = getQMedicalBag(fedConnection, fillMedicalBags);
		qPatient.setQMedicalBag(qMedicalBag);
		qLimitedPatient.setQPatient(qPatient);
		QLimitObject qLimitObject = new QLimitObject();
		qLimitObject.setCountRequested(countRequested);
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName("")); 
		qLimitObjectByNode.setLimit(limit); 
		qLimitObjectByNode.setOffset(offset); 
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); //on peut spécifier plusieurs une limit par noeud
		qLimitedPatient.setLimits(qLimitObject);
		return qLimitedPatient;
	}
	
	public static QLimitedMedicalBag getQLimitedMedicalBag(FedEHRConnection fedConnection, Patient p, boolean fillMedicalBags) throws ServerError, IOException {
		QLimitedMedicalBag qLimitedMedicalBag = new QLimitedMedicalBag(); 
		QMedicalBag qMedicalBag = getQMedicalBag(fedConnection, fillMedicalBags);
		qMedicalBag.setPatientID(String.valueOf(p.getID()));
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		QLimitObject qLimitObject = new QLimitObject();
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));  
		qLimitObjectByNode.setLimit(10); 
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); 
		qLimitedMedicalBag.setLimits(qLimitObject);
		
		return qLimitedMedicalBag;
	}
	
	public static QLimitedMedicalBag getQLimitedMedicalBag(FedEHRConnection fedConnection, boolean fillMedicalBags) throws ServerError, IOException {
		QLimitedMedicalBag qLimitedMedicalBag = new QLimitedMedicalBag(); 
		QMedicalBag qMedicalBag = getQMedicalBag(fedConnection, fillMedicalBags);
		
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		QLimitObject qLimitObject = new QLimitObject();
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));  
		qLimitObjectByNode.setLimit(10); 
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); 
		qLimitedMedicalBag.setLimits(qLimitObject);
		
		return qLimitedMedicalBag;
	}

	/**
	 * @param fedConnection
	 * @param fillMedicalBags
	 * @return
	 * @throws ServerError
	 */
	private static QMedicalBag getQMedicalBag(FedEHRConnection fedConnection, boolean fillMedicalBags) throws ServerError {
		QMedicalBag qMedicalBag = new QMedicalBag();
		qMedicalBag.setHospitalNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
		
		if(fillMedicalBags){
			QMedicalEvent qMedicalEvent = new QMedicalEvent();
			QMedicalEventType qMedicalEventType = new QMedicalEventType();
			qMedicalEvent.setQMedicalEventType(qMedicalEventType);
			QClinicalVariable qClinicalVariable = new QClinicalVariable();
			qMedicalEvent.setQClinicalVariable(qClinicalVariable);
			qMedicalBag.setQMedicalEvent(qMedicalEvent);
		}
		return qMedicalBag;
	}
	
	
}
