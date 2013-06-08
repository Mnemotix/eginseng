package com.mnemotix.ginseng.fedEHR;

import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.ns.idal.Address;
import fr.maatg.pandora.ns.idal.Patient;
import fr.maatg.pandora.ns.idal.Patients;
import fr.maatg.pandora.ns.idal.QAddress;
import fr.maatg.pandora.ns.idal.QCity;
import fr.maatg.pandora.ns.idal.QLimitObject;
import fr.maatg.pandora.ns.idal.QLimitObjectByNode;
import fr.maatg.pandora.ns.idal.QLimitedPatient;
import fr.maatg.pandora.ns.idal.QPatient;
import fr.maatg.pandora.ns.idal.ServerError;

public class ApiManager {


	
	public Address getAddress(FedEHRConnection fedEHRConnection, Patient patient) throws ServerError{
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
	
}
