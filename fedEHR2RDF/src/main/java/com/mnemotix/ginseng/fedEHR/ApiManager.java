package com.mnemotix.ginseng.fedEHR;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.time.StopWatch;

import com.mnemotix.ginseng.fedEHR.rdf.RDFExporter;
import com.mnemotix.ginseng.vocabulary.SemEHR;

import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.ns.idal.Address;
import fr.maatg.pandora.ns.idal.MedicalBag;
import fr.maatg.pandora.ns.idal.MedicalBags;
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

public class ApiManager {
	
	public static void crawlHospital(FedEHRConnection fedConnection, Writer writer, int pageSize){
		try {

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			RDFExporter rdfExporter = new RDFExporter();
			
			QLimitedPatient  qLimitedPatient = ApiManager.getQLimitedPatient(fedConnection, pageSize);
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
					
					QLimitedMedicalBag qLimitedMedicalBag = ApiManager.getQLimitedMedicalBag(fedConnection, patient, 100, true);
					MedicalBags medicalBags;
					do{
						medicalBags = fedConnection.fedEHRPortType.listMedicalBags(qLimitedMedicalBag);
						for(MedicalBag medicalBag : medicalBags.getMedicalBag()){
							rdfExporter.medicalBag2RDF(medicalBag, writer);
						}
					
						qLimitedMedicalBag.setLimits(medicalBags.getNextLimits());
						
					} while(!qLimitedMedicalBag.getLimits().isFinished());
					
				}
				qLimitedPatient.setLimits(patients.getNextLimits());		
				stopWatch.split();
				System.out.println(stopWatch.getSplitTime());
			} while(!patients.getNextLimits().isFinished());
			stopWatch.stop();
			System.out.println(stopWatch.getTime());
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
		qLimitObjectByNode.setLimit(10); //10 par noeud donc 30 puisqu'on a 3 noeuds.
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); //on peut spécifier plusieurs une limit par noeud
		qLimitedPatient.setLimits(qLimitObject);
		Patients patients = fedEHRConnection.fedEHRPortType.listPatients(qLimitedPatient);
		if(patients.getPatient().size() > 0){
			Patient patientWithAddress = patients.getPatient().get(0);
			return patientWithAddress.getAddress();
		}
		return null;
	}
	
	public static QLimitedPatient getQLimitedPatient(FedEHRConnection fedConnection, int limit) throws ServerError{
		QLimitedPatient qLimitedPatient = new QLimitedPatient(); //1000 par serveur par défaut.
		QPatient qPatient = new QPatient();
		qPatient.setHospitalNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
		qLimitedPatient.setQPatient(qPatient);
		QLimitObject qLimitObject = new QLimitObject();
		//qLimitObject.setCountRequested(true);
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));  //pour tous les noeuds
		//qLimitObjectByNode.setNode("GW_DEVEL_I3S"); 
		qLimitObjectByNode.setLimit(limit); 
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); //on peut spécifier plusieurs une limit par noeud
		qLimitedPatient.setLimits(qLimitObject);
		return qLimitedPatient;
	}
	
	public static QLimitedMedicalBag getQLimitedMedicalBag(FedEHRConnection fedConnection, Patient p, int limit, boolean fillMedicalBags) throws ServerError, IOException {
		QLimitedMedicalBag qLimitedMedicalBag = new QLimitedMedicalBag(); 
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
		
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		qMedicalBag.setPatientID(String.valueOf(p.getID()));
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		QLimitObject qLimitObject = new QLimitObject();
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));  
		qLimitObjectByNode.setLimit(10); 
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); 
		qLimitedMedicalBag.setLimits(qLimitObject);
		
		return qLimitedMedicalBag;
	}

	
	
	
}
