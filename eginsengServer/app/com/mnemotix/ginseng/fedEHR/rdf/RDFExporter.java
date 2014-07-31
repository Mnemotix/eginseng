package com.mnemotix.ginseng.fedEHR.rdf;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

import org.apache.http.client.utils.URIUtils;

import play.Logger;


import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mnemotix.ginseng.data.StringUtils;
import com.mnemotix.ginseng.vocabulary.Foaf;
import com.mnemotix.ginseng.vocabulary.SemEHR;
import com.sun.msv.util.Uri;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.maatg.pandora.clients.fedehr.exception.InvalidDataError;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRObjectFactory;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRTypeUtils;
import fr.maatg.pandora.ns.idal.Address;
import fr.maatg.pandora.ns.idal.Annotation;
import fr.maatg.pandora.ns.idal.BooleanValue;
import fr.maatg.pandora.ns.idal.ClinicalVariable;
import fr.maatg.pandora.ns.idal.ClinicalVariableType;
import fr.maatg.pandora.ns.idal.ClinicalVariableTypeRelatedClinicalVariableType;
import fr.maatg.pandora.ns.idal.DateValue;
import fr.maatg.pandora.ns.idal.IntegerValue;
import fr.maatg.pandora.ns.idal.MedicalBag;
import fr.maatg.pandora.ns.idal.MedicalEvent;
import fr.maatg.pandora.ns.idal.MedicalEventType;
import fr.maatg.pandora.ns.idal.MedicalEventTypeContainedCVT;
import fr.maatg.pandora.ns.idal.Patient;
import fr.maatg.pandora.ns.idal.ServerError;

//This class request FedEHR and generate corresponding from objects.
public class RDFExporter {
	
	String baseURL = "http://e-ginseng.org/";
	
	public HashSet<String> typePrimitives = new HashSet<String>();
	
	//Helper for building ttl triple for URI Values
	public String getTripleURIValue(String subject, String property, String value){
		return "<"+subject+"> " + "<"+property+"> " + "<"+value+"> . \n" ;
	}

	//Helper for building ttl triple for Literal Values
	public String getTripleLiteralValue(String subject, String property, String value){
		return "<"+subject+"> " + "<"+property+"> "+value+" . \n" ;
	}

	//Helper for writing ttl triple for URI Values
	public void writeTripleURIValue(Writer output, String subject, String property, String value) throws IOException{
		output.write(getTripleURIValue(subject, property, value));
	}

	//Helper for writing ttl triple for Literal Values
	public void writeTripleLiteralValue(Writer output, String subject, String property, String value) throws IOException{
		output.write(getTripleLiteralValue(subject, property, value));
	}
	
	// Build literal value with datatype if provided
	// If lang is introduce, we should add a parameter to this function
	public String buildLiteralValue(String label, String datatype){
		if(datatype == null){
			return "\""+label + "\"";
		}
		return "\""+label + "\"^^<" +datatype+">";
	}
	
	// Write patient personal and medical informations into the given writer and return it's URI
	public String patient2RDF(Patient patient, Writer output) throws IOException{
		// build URI
		String patientURL = baseURL + "patient-" + patient.getHospitalNode() + "-" + patient.getID();
		output.write(getTripleURIValue(patientURL, RDF.type.getURI(), SemEHR.PATIENT.getURI()));
		
		//personal informations
		if(patient.getDateOfBirth() != null){
			output.write(getTripleLiteralValue(patientURL, SemEHR.BIRTH_DATE.getURI(), buildLiteralValue(patient.getDateOfBirth().toXMLFormat(), DatatypeMap.xsddate)));
		}
		output.write(getTripleLiteralValue(patientURL, Foaf.FIRST_NAME.getURI(), buildLiteralValue(patient.getFirstName(), DatatypeMap.xsdstring)));
		output.write(getTripleLiteralValue(patientURL, Foaf.LAST_NAME.getURI(), buildLiteralValue(patient.getLastName(), DatatypeMap.xsdstring)));
		Address patientAddress = patient.getAddress();
		if(patientAddress != null){
			output.write(getTripleURIValue(patientURL, SemEHR.ADDRESS.getURI(), address2RDF(patientAddress, output)));
		}
		
		// api request for medical bags and iterate over it for generating RDF
		List<MedicalBag> medicalBags = patient.getMedicalBag();
		if(medicalBags != null){
			for(MedicalBag medicalBag : medicalBags){
				//link patient to the medical bag
				//medicalBag2RDF return the generated URI for the medical bag
				output.write(getTripleURIValue(patientURL, SemEHR.HAS_MEDICAL_BAG.getURI(), medicalBag2RDF(medicalBag, output)));
			}
		}
		return patientURL;
	}
	
	// generate address RDF description and return its URI
	public String address2RDF(Address address, Writer output) throws IOException{
		String addressURL =  baseURL + "address-" + address.getHospitalNode() + "-" + address.getID();
		writeTripleURIValue(output, addressURL, RDF.type.getURI(), SemEHR.ADDRESS_INSTANCE.getURI());
		writeTripleLiteralValue(output, addressURL,  SemEHR.POSTAL_CODE.getURI(), buildLiteralValue(address.getZIP(), DatatypeMap.xsdstring));
		if(address.getCity() != null){
			String cityURL =  baseURL + "city-" + address.getHospitalNode() + "-" + address.getCity().getID();
			writeTripleURIValue(output, addressURL, SemEHR.CITY.getURI(), cityURL);
			writeTripleLiteralValue(output, cityURL, RDFS.label.getURI(), buildLiteralValue(address.getCity().getName(), DatatypeMap.xsdstring));
			writeTripleURIValue(output, cityURL, RDF.type.getURI(), SemEHR.CITY_INSTANCE.getURI());
		}
		return addressURL;
	}
	
	// generate medical bag RDF description and return its URI
	public String medicalBag2RDF(MedicalBag medicalBag, Writer output) throws IOException{
		String medicalBagURL =  baseURL + "medicalBag-" + medicalBag.getHospitalNode() + "-" + medicalBag.getID();
		output.write(getTripleURIValue(medicalBagURL, RDF.type.getURI(), SemEHR.MEDICAL_BAG.getURI()));
		if (medicalBag.getMedicalBagDate() !=null){
			output.write(
				getTripleLiteralValue(
					medicalBagURL, 
					DC.date.getURI(), 
					buildLiteralValue(medicalBag.getMedicalBagDate().toXMLFormat(), DatatypeMap.xsddate)));
		}
		// API request for medical events and iterate over it to generate RDF
		List<MedicalEvent> medicalEvents = medicalBag.getMedicalEvents();
		if(medicalEvents != null){
			for(MedicalEvent medicalEvent : medicalEvents){
				//link medical bag to the medical event
				//medicalEvent2RDF return the generated URI for the medical event
				output.write(getTripleURIValue(medicalBagURL, SemEHR.HAS_MEDICAL_EVENT.getURI(), medicalEvent2RDF(medicalEvent, output)));
			}
		}
		return medicalBagURL;
	}


	// generate medical event RDF description and return its URI
	public String medicalEvent2RDF(MedicalEvent medicalEvent, Writer output) throws IOException{
		String medicalEventURL =  baseURL + "medicalEvent-" + medicalEvent.getHospitalNode() + "-" + medicalEvent.getID();
		output.write(getTripleURIValue(medicalEventURL, RDF.type.getURI(), SemEHR.MEDICAL_EVENT.getURI()));
		
		// Get the string type of medical event and instantiate it with a URI
		MedicalEventType medicalEventType = medicalEvent.getMedicalEventType();
		String medicalEventTypeURL = SemEHR.NS + "medicalEventType-"+ medicalEventType.getName().replaceAll	("\\s", "");
		output.write(getTripleURIValue(medicalEventURL, RDF.type.getURI(), medicalEventTypeURL));
		if (medicalEvent.getEventDate() !=null){
			output.write(
				getTripleLiteralValue(
					medicalEventURL, 
					DC.date.getURI(), 
					buildLiteralValue(medicalEvent.getEventDate().toXMLFormat(), DatatypeMap.xsddate)));
		}
		if(!typePrimitives.contains(medicalEventTypeURL)){
			//Write Clinical Variable type Description
			output.write(getTripleURIValue(medicalEventTypeURL, RDF.type.getURI(), OWL.Class.getURI()));
			output.write(getTripleLiteralValue(medicalEventTypeURL, RDFS.label.getURI(), buildLiteralValue(medicalEventType.getName(), DatatypeMap.xsdstring)));
			output.write(getTripleURIValue(medicalEventTypeURL, RDFS.subClassOf.getURI(), SemEHR.MEDICAL_EVENT.getURI()));
			typePrimitives.add(medicalEventTypeURL);
		}
		List<ClinicalVariable> clinicalVariables = medicalEvent.getClinicalVariable();
		if(clinicalVariables != null){
			for(ClinicalVariable clinicalVariable : clinicalVariables){
				//link medical event to the clinical variable
				//clinicalVariable2RDF return the generated URI for the clinical variable
				output.write(getTripleURIValue(medicalEventURL, SemEHR.HAS_CLINICAL_VARIABLE.getURI(), clinicalVariable2RDF(clinicalVariable, output)));
			}
		}
		return medicalEventURL;
	}
	
	public String clinicalVariable2RDF(ClinicalVariable clinicalVariable, Writer output) throws IOException{
		String clinicalVariableURL =  baseURL + "clinicalVariable-" + clinicalVariable.getHospitalNode() + "-" + clinicalVariable.getID();
		output.write(getTripleURIValue(clinicalVariableURL, RDF.type.getURI(), SemEHR.CLINICAL_VARIABLE.getURI()));
		// Get the string type of clinical variable and instantiate it with a URI
		String clinicalVariableTypeURL = buildClinicalVariableTypeURI(clinicalVariable.getTypeName());
		output.write(getTripleURIValue(clinicalVariableURL, RDF.type.getURI(), clinicalVariableTypeURL));
		
		if(clinicalVariable.getAcquisitionDate() != null){
			output.write(
				getTripleLiteralValue(
					clinicalVariableURL, 
					SemEHR.ACQUISITION_DATE.getURI(), 
					buildLiteralValue(clinicalVariable.getAcquisitionDate().toXMLFormat(), DatatypeMap.xsddate)));
		}
		// We have different type of clinical variable value
		// We need to test each one
		try{
			// is it a text annotation?
			Annotation annotation = (Annotation) clinicalVariable;
			if(annotation.getValue() != null){
				output.write(
						getTripleLiteralValue(
							clinicalVariableURL, 
							SemEHR.VALUE.getURI(), 
							buildLiteralValue(annotation.getValue(), DatatypeMap.xsdstring)));
			}
		}catch(ClassCastException classCastException){
			//OK This is not an annotation
		}
		try{
			// is it an integer value?
			IntegerValue integerValue = (IntegerValue) clinicalVariable;
			if(integerValue.getValue() != null){
				output.write(
						getTripleLiteralValue(
							clinicalVariableURL, 
							SemEHR.VALUE.getURI(), 
							buildLiteralValue(String.valueOf(integerValue.getValue().longValue()), DatatypeMap.xsdlong)));
			}
		}catch(ClassCastException classCastException){
			//OK This is not an integer value
		}
		try{
			// is it an boolean value?
			BooleanValue booleanValue = (BooleanValue) clinicalVariable;
			if(booleanValue.isValue() != null){
				output.write(
					getTripleLiteralValue(
						clinicalVariableURL, 
						SemEHR.VALUE.getURI(), 
						buildLiteralValue(String.valueOf(booleanValue.isValue()), DatatypeMap.xsdboolean)));
			}
		}catch(ClassCastException classCastException){
			//OK This is not an boolean value
		}
		try{
			// is it an date value?
			DateValue dateValue = (DateValue) clinicalVariable;
			if(dateValue.getValue() != null){
				output.write(
					getTripleLiteralValue(
						clinicalVariableURL, 
						SemEHR.VALUE.getURI(), 
						buildLiteralValue(dateValue.getValue().toXMLFormat(), DatatypeMap.xsddate)));
			}
		}catch(ClassCastException classCastException){
			//OK This is not an boolean value
		}
		
		return clinicalVariableURL;
	}

	
	//The two following functions show an example for navigating into clinical variable tree in FedEHR
	public void navigateCVTFromMedicalEvent(FedEHRConnection fedEHRConnection, MedicalEvent medicalEvent) throws InvalidDataError, ServerError {
		FedEHRTypeUtils fedEHRTypeUtils = new FedEHRTypeUtils(new FedEHRObjectFactory(fedEHRConnection.fedEHRPortType));
		MedicalEventTypeContainedCVT topMETCCVT = fedEHRTypeUtils.getTopElementWithChildren(medicalEvent.getMedicalEventType()).get(0);
		ClinicalVariableType topCCVT = topMETCCVT.getClinicalVariableType().getValue();
		navigateCVT(0,topCCVT);
	}
		
	private void navigateCVT(int level, ClinicalVariableType clinicalVariableType) {
		String indent ="";
		for (int i=0;i<level;i++)
			indent+="-";
		Logger.debug(indent+clinicalVariableType.getName()); 
		for (ClinicalVariableTypeRelatedClinicalVariableType clinicalVariableTypeRelatedClinicalVariableType : clinicalVariableType.getRelatedClinicalVariableType()) {
			navigateCVT(level+1,clinicalVariableTypeRelatedClinicalVariableType.getClinicalVariableType());
		}
	}
	
	
	public String buildClinicalVariableURL(ClinicalVariable clinicalVariable){
		return baseURL + "clinicalVariable-" + clinicalVariable.getHospitalNode() + "-" + clinicalVariable.getID();
	}

	public String buildClinicalVariableTypeURI(String cvtName){
		return SemEHR.NS + "ClinicalVariableType-"+ Uri.escapeDisallowedChars(StringUtils.normalize(cvtName).replaceAll("\\s", ""));
	}

	public String buildClinicalVariableTypeURI(ClinicalVariableType clinicalVariableType) {
		return buildClinicalVariableTypeURI(clinicalVariableType.getName());
	}
	
	public String normalizeString(String s){
		return s;
	}
		
}
