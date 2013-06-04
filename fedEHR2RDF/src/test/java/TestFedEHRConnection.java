import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.globus.gsi.CredentialException;
import org.junit.Test;

import com.mnemotix.ginseng.fedEHR.rdf.RDFExporter;

import fr.maatg.pandora.clients.commons.exception.ClientError;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.ns.idal.Annotation;
import fr.maatg.pandora.ns.idal.BooleanValue;
import fr.maatg.pandora.ns.idal.ClinicalVariable;
import fr.maatg.pandora.ns.idal.DateValue;
import fr.maatg.pandora.ns.idal.IntegerValue;
import fr.maatg.pandora.ns.idal.MedicalBag;
import fr.maatg.pandora.ns.idal.MedicalBags;
import fr.maatg.pandora.ns.idal.MedicalEvent;
import fr.maatg.pandora.ns.idal.MedicalEvents;
import fr.maatg.pandora.ns.idal.Patient;
import fr.maatg.pandora.ns.idal.Patients;
import fr.maatg.pandora.ns.idal.QAnnotation;
import fr.maatg.pandora.ns.idal.QBooleanValue;
import fr.maatg.pandora.ns.idal.QClinicalVariable;
import fr.maatg.pandora.ns.idal.QDateValue;
import fr.maatg.pandora.ns.idal.QIntegerValue;
import fr.maatg.pandora.ns.idal.QLimitObject;
import fr.maatg.pandora.ns.idal.QLimitObjectByNode;
import fr.maatg.pandora.ns.idal.QLimitedMedicalBag;
import fr.maatg.pandora.ns.idal.QLimitedMedicalEvent;
import fr.maatg.pandora.ns.idal.QLimitedPatient;
import fr.maatg.pandora.ns.idal.QMedicalBag;
import fr.maatg.pandora.ns.idal.QMedicalEvent;
import fr.maatg.pandora.ns.idal.QMedicalEventType;
import fr.maatg.pandora.ns.idal.QPatient;
import fr.maatg.pandora.ns.idal.ServerError;

public class TestFedEHRConnection {
	
	Writer ouput;
	RDFExporter rdfExporter;
	
	@Test
	public void test(){
		//String fedEHRServiceURL = "https://ginseng.unice.fr:8443/pandora-gateway-idal-fedehr/fedehr";
		String fedEHRServiceURL = "https://dev.grid.creatis.insa-lyon.fr:8443/pandora-gateway-idal-fedehr/fedehr";
		String certFile = "src/test/resources/usercert.pem";
		String keyFile = "src/test/resources/userkey.pem";
		long receiveTimeout = 10*60*1000L; //10 minutes
		

		try {

			Properties properties = new Properties();
			FileReader fileReader = new FileReader("src/test/resources/fedEHRdev.properties");
			properties.load(fileReader);
			String pwd = properties.getProperty("pwd");
			String caPathPattern = "file:"+properties.getProperty("capath.pattern.path");
			FedEHRConnection fedConnection = new FedEHRConnection(fedEHRServiceURL, certFile, keyFile, caPathPattern, pwd, receiveTimeout);
			//System.out.println(fedConnection.fedEHRPortType.getHospitalNodeList("").getHospitalNode());
			

			this.ouput = new FileWriter("src/test/resources/patient.rdf");
			this.rdfExporter = new RDFExporter();
			
			getPatients(fedConnection);

			this.ouput.close();
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
	
	private void getAddress(FedEHRConnection fedConnection, Patient patient){
		//QAdress ==> QCity ==> QCountry
	}
	
	private void getPatients(FedEHRConnection fedConnection) throws ServerError, IOException {
		QLimitedPatient qLimitedPatient = new QLimitedPatient(); //1000 par serveur par défaut.
		QPatient qPatient = new QPatient();
		qPatient.setHospitalNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
		
		//Si tous les patients ont des adresses, on peut les demander ici.
		//QAddress ==> QCity ==> QCountry
		
		QMedicalBag qMedicalBag = new QMedicalBag();
		qPatient.setQMedicalBag(qMedicalBag);
		qLimitedPatient.setQPatient(qPatient);
		QLimitObject qLimitObject = new QLimitObject();
		//qLimitObject.setCountRequested(true);
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));  //pour tous les noeuds
		//qLimitObjectByNode.setNode("GW_DEVEL_I3S"); 
		qLimitObjectByNode.setLimit(100); //10 par noeud donc 30 puisqu'on a 3 noeuds.
		//qLimitObjectByNode.setOffset(10);
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); //on peut spécifier plusieurs une limit par noeud
		qLimitedPatient.setLimits(qLimitObject);
		do{
			Patients patients = fedConnection.fedEHRPortType.listPatients(qLimitedPatient);
			Patient p = patients.getPatient().get(0);
			//QPatient qPatient = new QPatient(); qPatient.setID(String.valueOf(p.getID())); //poss
			System.out.println(p.getFirstName() + " " + p.getLastName());
			this.rdfExporter.patient2RDF(p, this.ouput);
			getMedicalBag(fedConnection, p);
			qLimitObject = patients.getNextLimits();
			qLimitedPatient.setLimits(qLimitObject);		
			
		} while(false && !qLimitObject.isFinished());
	}

	private void getMedicalBag(FedEHRConnection fedConnection, Patient p) throws ServerError, IOException {
		QLimitedMedicalBag qLimitedMedicalBag = new QLimitedMedicalBag(); //1000 par serveur par défaut.

		QMedicalEvent qMedicalEvent = new QMedicalEvent();
		QMedicalEventType qMedicalEventType = new QMedicalEventType();
		qMedicalEvent.setQMedicalEventType(qMedicalEventType);

		QClinicalVariable qClinicalVariable = new QClinicalVariable();
		qClinicalVariable.setValueNotAvailable(true);
		qMedicalEvent.setQClinicalVariable(qClinicalVariable);
		
		QMedicalBag qMedicalBag = new QMedicalBag();
		qMedicalBag.setQMedicalEvent(qMedicalEvent);
		qMedicalBag.setHospitalNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
		
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		qMedicalBag.setPatientID(String.valueOf(p.getID()));
		qLimitedMedicalBag.setQMedicalBag(qMedicalBag);
		QLimitObject qLimitObject = new QLimitObject();
		//qLimitObject.setCountRequested(true);
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));  //pour tous les noeuds
		//qLimitObjectByNode.setNode("GW_DEVEL_I3S"); 
		qLimitObjectByNode.setLimit(10); //10 par noeud donc 30 puisqu'on a 3 noeuds.
		//qLimitObjectByNode.setOffset(10);
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); //on peut spécifier plusieurs une limit par noeud
		qLimitedMedicalBag.setLimits(qLimitObject);
		do{
			
			MedicalBags medicalBags = fedConnection.fedEHRPortType.listMedicalBags(qLimitedMedicalBag);
			for(MedicalBag medicalBag : medicalBags.getMedicalBag()){
				System.out.println("medical Bag: " +
						medicalBag.getMedicalBagNo() + ": "+
						medicalBag.getHospitalNode() + ": " +
						medicalBag.getDescription());
				this.rdfExporter.medicalBag2RDF(medicalBag, this.ouput);
				for(MedicalEvent medicalEvent : medicalBag.getMedicalEvents()){
					System.out.println("medical Event: " +
							medicalEvent.getID() + ": "+
							medicalEvent.getMedicalEventType().getName()+ ": " +
							medicalEvent.getEventDate());
					/*for(ClinicalVariable clinicalVariable : medicalEvent.getClinicalVariable()){
						System.out.println("clinical Variable: " +
								clinicalVariable.getID() + ": " +
								clinicalVariable.getClinicalVariableTypeID() + ": "+ //id unique par noeud 
								clinicalVariable.getTypeName() + ": " +
								clinicalVariable.getAcquisitionDate() + ": " +
								clinicalVariable.getRelatedClinicalVariables()
								);
					}*/

					getClinicalVariables(fedConnection, medicalEvent);
				}
			}
			//QPatient qPatient = new QPatient(); qPatient.setID(String.valueOf(p.getID())); //poss
		
			qLimitObject = medicalBags.getNextLimits();
			qLimitedMedicalBag.setLimits(qLimitObject);
			
		} while(!qLimitObject.isFinished());
	}
	
	
	public void getClinicalVariables(FedEHRConnection fedConnection, MedicalEvent medicalEvent) throws ServerError{
		QLimitedMedicalEvent qLimitedMedicalEvent = new QLimitedMedicalEvent();
		
		QMedicalEvent qMedicalEvent = new QMedicalEvent();
		qMedicalEvent.setID(String.valueOf(medicalEvent.getID()));
		//qMedicalEvent.setHospitalNode(medicalEvent.getHospitalNode());
		QClinicalVariable qClinicalVariable = new QClinicalVariable();
		qClinicalVariable.setValueNotAvailable(true);
		qMedicalEvent.setQClinicalVariable(qClinicalVariable);
		qLimitedMedicalEvent.setQMedicalEvent(qMedicalEvent);
		

		QLimitObject qLimitObject = new QLimitObject();
		//qLimitObject.setCountRequested(true);
		QLimitObjectByNode qLimitObjectByNode = new QLimitObjectByNode();
		qLimitObjectByNode.setNode(medicalEvent.getHospitalNode());  //pour tous les noeuds
		//qLimitObjectByNode.setNode("GW_DEVEL_I3S"); 
		qLimitObjectByNode.setLimit(10); //10 par noeud donc 30 puisqu'on a 3 noeuds.
		//qLimitObjectByNode.setOffset(10);
		qLimitObject.getLimitObjectByNode().add(qLimitObjectByNode); //on peut spécifier plusieurs une limit par noeud
		qLimitedMedicalEvent.setLimits(qLimitObject);
		

		
		MedicalEvents medicalEvents = fedConnection.fedEHRPortType.listMedicalEvents(qLimitedMedicalEvent);
		if(medicalEvents.getMedicalEvent().size() > 0){
			for(ClinicalVariable clinicalVariable : medicalEvent.getClinicalVariable()){
				System.out.println("clinical Variable: " +
						clinicalVariable.getID() + ": " +
						clinicalVariable.getClinicalVariableTypeID() + ": "+ //id unique par noeud 
						clinicalVariable.getTypeName() + ": " +
						clinicalVariable.getAcquisitionDate() + ": " +
						clinicalVariable.getRelatedClinicalVariables()
						);
			}
		}

		QAnnotation qAnnotation = new QAnnotation(); //QAnnotation extend QClinicalVariable
		//qAnnotation.setValueNotAvailable(true);
		qMedicalEvent.setQClinicalVariable(qAnnotation);
		
		medicalEvents = fedConnection.fedEHRPortType.listMedicalEvents(qLimitedMedicalEvent);
		if(medicalEvents.getMedicalEvent().size() > 0){
			for(ClinicalVariable clinicalVariable : medicalEvent.getClinicalVariable()){
				Annotation annotation = (Annotation) clinicalVariable;
				System.out.println("Annotation: " +
						annotation.getID() + ": " +
						annotation.getClinicalVariableTypeID() + ": "+ //id unique par noeud 
						annotation.getTypeName() + ": " +
						annotation.getAcquisitionDate() + ": " +
						annotation.getValue() + ": " +
						annotation.getRelatedClinicalVariables()
						);
			}
		}
		

		

		QDateValue qDateValue = new QDateValue(); //QAnnotation extend QClinicalVariable
		qDateValue.setValueNotAvailable(false);
		qMedicalEvent.setQClinicalVariable(qDateValue);
		medicalEvents = fedConnection.fedEHRPortType.listMedicalEvents(qLimitedMedicalEvent);
		if(medicalEvents.getMedicalEvent().size() > 0){
			MedicalEvent medicalEvent3 = medicalEvents.getMedicalEvent().get(0);
			
			for(ClinicalVariable clinicalVariable : medicalEvent3.getClinicalVariable()){
				DateValue dateValue = (DateValue) clinicalVariable;
				System.out.println("DateValue: " +
						dateValue.getID() + ": " +
						dateValue.getClinicalVariableTypeID() + ": "+ //id unique par noeud 
						dateValue.getTypeName() + ": " +
						dateValue.getAcquisitionDate() + ": " +
						dateValue.getValue() + ": " +
						dateValue.getRelatedClinicalVariables()
						);
			}
		}
		
		QIntegerValue qIntegerValue = new QIntegerValue(); //QAnnotation extend QClinicalVariable
		qIntegerValue.setValueNotAvailable(false);
		qMedicalEvent.setQClinicalVariable(qIntegerValue);
		medicalEvents = fedConnection.fedEHRPortType.listMedicalEvents(qLimitedMedicalEvent);
		if(medicalEvents.getMedicalEvent().size() > 0){
			MedicalEvent medicalEvent3 = medicalEvents.getMedicalEvent().get(0);
			
			for(ClinicalVariable clinicalVariable : medicalEvent3.getClinicalVariable()){
				IntegerValue integerValue = (IntegerValue) clinicalVariable;
				System.out.println("IntegerValue: " +
						integerValue.getID() + ": " +
						integerValue.getClinicalVariableTypeID() + ": "+ //id unique par noeud 
						integerValue.getTypeName() + ": " +
						integerValue.getAcquisitionDate() + ": " +
						integerValue.getValue() + ": " +
						integerValue.getRelatedClinicalVariables()
						);
			}
		}
		


		QBooleanValue qBooleanValue = new QBooleanValue(); //QAnnotation extend QClinicalVariable
		qBooleanValue.setValueNotAvailable(false);
		qMedicalEvent.setQClinicalVariable(qBooleanValue);
		medicalEvents = fedConnection.fedEHRPortType.listMedicalEvents(qLimitedMedicalEvent);
		if(medicalEvents.getMedicalEvent().size() > 0){
			MedicalEvent medicalEvent3 = medicalEvents.getMedicalEvent().get(0);
			
			for(ClinicalVariable clinicalVariable : medicalEvent3.getClinicalVariable()){
				BooleanValue booleanValue = (BooleanValue) clinicalVariable;
				System.out.println("BooleanValue: " +
						booleanValue.getID() + ": " +
						booleanValue.getClinicalVariableTypeID() + ": "+ //id unique par noeud 
						booleanValue.getTypeName() + ": " +
						booleanValue.getAcquisitionDate() + ": " +
						booleanValue.isValue() + ": " +
						booleanValue.getRelatedClinicalVariables()
						);
			}
		}
		
	}
}
