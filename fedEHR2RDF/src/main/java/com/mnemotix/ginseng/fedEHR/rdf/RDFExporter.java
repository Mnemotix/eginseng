package com.mnemotix.ginseng.fedEHR.rdf;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;


import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mnemotix.ginseng.vocabulary.Foaf;
import com.mnemotix.ginseng.vocabulary.SemEHR;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.maatg.pandora.ns.idal.Address;
import fr.maatg.pandora.ns.idal.Annotation;
import fr.maatg.pandora.ns.idal.BooleanValue;
import fr.maatg.pandora.ns.idal.ClinicalVariable;
import fr.maatg.pandora.ns.idal.ClinicalVariableRelatedClinicalVariables;
import fr.maatg.pandora.ns.idal.DateValue;
import fr.maatg.pandora.ns.idal.IntegerValue;
import fr.maatg.pandora.ns.idal.MedicalBag;
import fr.maatg.pandora.ns.idal.MedicalEvent;
import fr.maatg.pandora.ns.idal.MedicalEventType;
import fr.maatg.pandora.ns.idal.Patient;

public class RDFExporter {
	
	String baseURL = "http://e-ginseng.org/";
	//TODO relatedclinicalVariable
	//TODO address
	//TODO type
	
	public HashSet<String> typePrimitives = new HashSet<String>();
	
	public String getTripleURIValue(String subject, String property, String value){
		return "<"+subject+"> " + "<"+property+"> " + "<"+value+"> . \n" ;
	}

	public String getTripleLiteralValue(String subject, String property, String value){
		return "<"+subject+"> " + "<"+property+"> \""+value+"\" . \n" ;
	}
	
	public void writeTripleURIValue(Writer output, String subject, String property, String value) throws IOException{
		output.write(getTripleURIValue(subject, property, value));
	}

	public void writeTripleLiteralValue(Writer output, String subject, String property, String value) throws IOException{
		output.write(getTripleLiteralValue(subject, property, value));
	}
	
	public String buildLiteralValue(String label, String datatype){
		return label + "^^" +datatype;
	}
	
	public String patient2RDF(Patient patient, Writer output) throws IOException{
		String patientURL = baseURL + "patient-" + patient.getHospitalNode() + "-" + patient.getID();
		output.write(getTripleURIValue(patientURL, RDF.type.getURI(), SemEHR.PATIENT.getURI()));
		if(patient.getDateOfBirth() != null){
			output.write(getTripleURIValue(patientURL, SemEHR.BIRTH_DATE.getURI(), buildLiteralValue(patient.getDateOfBirth().toXMLFormat(), DatatypeMap.xsddate)));
		}
		output.write(getTripleLiteralValue(patientURL, Foaf.FIRST_NAME.getURI(), patient.getFirstName()));
		output.write(getTripleLiteralValue(patientURL, Foaf.LAST_NAME.getURI(), patient.getLastName()));
		/*Address patientAddress = patient.getAddress();
		if(patientAddress != null){
			output.write(getTripleURIValue(patientURL, SemEHR.ADDRESS.getURI(), address2RDF(patientAddress, output)));
		}*/
		List<MedicalBag> medicalBags = patient.getMedicalBag();
		if(medicalBags != null){
			for(MedicalBag medicalBag : medicalBags){
				output.write(getTripleURIValue(patientURL, SemEHR.HAS_MEDICAL_BAG.getURI(), medicalBag2RDF(medicalBag, output)));
			}
		}
		return patientURL;
	}
	
	public String address2RDF(Address address, Writer output) throws IOException{
		String addressURL =  baseURL + "address-" + address.getHospitalNode() + "-" + address.getID();
		writeTripleURIValue(output, addressURL, RDF.type.getURI(), SemEHR.ADDRESS_INSTANCE.getURI());
		if(address.getCity() != null){
			String cityURL =  baseURL + "city-" + address.getHospitalNode() + "-" + address.getCity().getID();
			writeTripleURIValue(output, addressURL, SemEHR.CITY.getURI(), cityURL);
			writeTripleLiteralValue(output, cityURL, RDFS.label.getURI(), address.getCity().getName());;
			writeTripleURIValue(output, cityURL, RDF.type.getURI(), SemEHR.CITY_INSTANCE.getURI());
		}
		return addressURL;
	}
	
	public String medicalBag2RDF(MedicalBag medicalBag, Writer output) throws IOException{
		String medicalBagURL =  baseURL + "medicalBag-" + medicalBag.getHospitalNode() + "-" + medicalBag.getID();
		output.write(getTripleURIValue(medicalBagURL, RDF.type.getURI(), SemEHR.MEDICAL_BAG.getURI()));
		if (medicalBag.getMedicalBagDate() !=null){
			output.write(
				getTripleLiteralValue(
					medicalBagURL, 
					DC.date.getURI(), 
					buildLiteralValue(medicalBag.getMedicalBagDate().toXMLFormat(), DatatypeMap.xsddateTime)));
		}
		List<MedicalEvent> medicalEvents = medicalBag.getMedicalEvents();
		if(medicalEvents != null){
			for(MedicalEvent medicalEvent : medicalEvents){
				output.write(getTripleURIValue(medicalBagURL, SemEHR.HAS_MEDICAL_EVENT.getURI(), medicalEvent2RDF(medicalEvent, output)));
			}
		}
		//TODO write triples to ouput
		return medicalBagURL;
	}

	
	public String medicalEvent2RDF(MedicalEvent medicalEvent, Writer output) throws IOException{
		String medicalEventURL =  baseURL + "medicalEvent-" + medicalEvent.getHospitalNode() + "-" + medicalEvent.getID();
		output.write(getTripleURIValue(medicalEventURL, RDF.type.getURI(), SemEHR.MEDICAL_EVENT.getURI()));
		MedicalEventType medicalEventType = medicalEvent.getMedicalEventType();
		String medicalEventTypeURL = SemEHR.NS + "medicalEventType-"+medicalEventType.getID() +"-"+ medicalEventType.getName().replaceAll	("\\s", "");
		output.write(getTripleURIValue(medicalEventURL, RDF.type.getURI(), medicalEventTypeURL));
		if (medicalEvent.getEventDate() !=null){
			output.write(
				getTripleLiteralValue(
					medicalEventURL, 
					DC.date.getURI(), 
					buildLiteralValue(medicalEvent.getEventDate().toXMLFormat(), DatatypeMap.xsddateTime)));
		}
		if(!typePrimitives.contains(medicalEventTypeURL)){
			//Write Clinical Variable type Description
			output.write(getTripleURIValue(medicalEventTypeURL, RDF.type.getURI(), OWL.Class.getURI()));
			output.write(getTripleLiteralValue(medicalEventTypeURL, RDFS.label.getURI(), medicalEventType.getName()));
			output.write(getTripleURIValue(medicalEventTypeURL, RDFS.subClassOf.getURI(), SemEHR.MEDICAL_EVENT.getURI()));
			typePrimitives.add(medicalEventTypeURL);
		}
		List<ClinicalVariable> clinicalVariables = medicalEvent.getClinicalVariable();
		if(clinicalVariables != null){
			for(ClinicalVariable clinicalVariable : clinicalVariables){
				output.write(getTripleURIValue(medicalEventURL, SemEHR.HAS_CLINICAL_VARIABLE.getURI(), clinicalVariable2RDF(clinicalVariable, output)));
			}
		}
		return medicalEventURL;
	}
	
	public String clinicalVariable2RDF(ClinicalVariable clinicalVariable, Writer output) throws IOException{
		String clinicalVariableURL =  baseURL + "clinicalVariable-" + clinicalVariable.getHospitalNode() + "-" + clinicalVariable.getID();
		output.write(getTripleURIValue(clinicalVariableURL, RDF.type.getURI(), SemEHR.CLINICAL_VARIABLE.getURI()));
		String clinicalVariableTypeURL = SemEHR.NS + "ClinicalVariableType-"+clinicalVariable.getMedicalEventTypeID() +"-"+ clinicalVariable.getTypeName().replaceAll	("\\s", "");
		output.write(getTripleURIValue(clinicalVariableURL, RDF.type.getURI(), clinicalVariableTypeURL));
		if(!typePrimitives.contains(clinicalVariableTypeURL)){
			//Write Clinical Variable type Description
			output.write(getTripleURIValue(clinicalVariableTypeURL, RDF.type.getURI(), OWL.Class.getURI()));
			output.write(getTripleLiteralValue(clinicalVariableTypeURL, RDFS.label.getURI(), clinicalVariable.getTypeName()));
			output.write(getTripleURIValue(clinicalVariableTypeURL, RDFS.subClassOf.getURI(), SemEHR.CLINICAL_VARIABLE.getURI()));
			typePrimitives.add(clinicalVariableTypeURL);
		}
		
		clinicalVariable.getClinicalVariableTypeID();
		if(clinicalVariable.getAcquisitionDate() != null){
			output.write(
				getTripleLiteralValue(
					clinicalVariableURL, 
					SemEHR.ACQUISITION_DATE.getURI(), 
					buildLiteralValue(clinicalVariable.getAcquisitionDate().toXMLFormat(), DatatypeMap.xsddate)));
		}
		try{
			Annotation annotation = (Annotation) clinicalVariable;
			if(annotation.getValue() != null){
				output.write(
						getTripleLiteralValue(
							clinicalVariableURL, 
							SemEHR.ANNOTATION.getURI(), 
							annotation.getValue()));
			}
		}catch(ClassCastException classCastException){
			//OK This is not an annotation
		}
		try{
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
			DateValue dateValue = (DateValue) clinicalVariable;
			if(dateValue.getValue() != null){
				output.write(
					getTripleLiteralValue(
						clinicalVariableURL, 
						SemEHR.VALUE.getURI(), 
						buildLiteralValue(dateValue.getValue().toXMLFormat(), DatatypeMap.xsddateTime)));
			}
		}catch(ClassCastException classCastException){
			//OK This is not an boolean value
		}
		//TODO get related clinical variable and write rdf into output
		List<ClinicalVariableRelatedClinicalVariables> relatedClinicalVariables = clinicalVariable.getRelatedClinicalVariables();
		for(ClinicalVariableRelatedClinicalVariables clinicalVariableRelatedClinicalVariables : relatedClinicalVariables){
		}
		return clinicalVariableURL;
	}
	
	
	public String buildClinicalVariableURL(ClinicalVariable clinicalVariable){
		return baseURL + "clinicalVariable-" + clinicalVariable.getHospitalNode() + "-" + clinicalVariable.getID();
	}
}
