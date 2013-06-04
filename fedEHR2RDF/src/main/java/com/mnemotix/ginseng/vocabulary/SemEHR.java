/* CVS $Id: $ */
package com.mnemotix.ginseng.vocabulary; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from /Users/ereteog/workspace/eginseng/fedEHR2RDF/src/main/resources/semEHR.owl 
 * @author Auto-generated by schemagen on 04 juin 2013 15:41 
 */
public class SemEHR {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.mnemotix.com/ontology/semEHR#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>the date where the clinical variable was acquired.</p> */
    public static final Property ACQUISITION_DATE = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#acquisitionDate" );
    
    /** <p>a postal address property</p> */
    public static final Property ADDRESS = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#address" );
    
    /** <p>an annotation describing the clinical variable, if it cannot be represented 
     *  by a value or a list of clinical variable</p>
     */
    public static final Property ANNOTATION = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#annotation" );
    
    /** <p>the birth date of a patient</p> */
    public static final Property BIRTH_DATE = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#birthDate" );
    
    /** <p>A clinical variable is the final element of the structure. It can represent 
     *  a value or a group of value, or a group of group of value and so on. There 
     *  is no restriction to the deep that FedEHR can manage, but it is recommended 
     *  not to have more than 5 levels.</p>
     */
    public static final Property HAS_CLINICAL_VARIABLE = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#hasClinicalVariable" );
    
    /** <p>a medical bag of a patient.</p> */
    public static final Property HAS_MEDICAL_BAG = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#hasMedicalBag" );
    
    /** <p>A medical bag can contain a date, a description and a list of medical events.</p> */
    public static final Property HAS_MEDICAL_EVENT = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#hasMedicalEvent" );
    
    /** <p>define a physician that proceed</p> */
    public static final Property HAS_PHYSICIAN = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#hasPhysician" );
    
    /** <p>define a clinical variable that compose the current clinical variable</p> */
    public static final Property HAS_SUB_CLINICAL_VARIABLE = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#hasSubClinicalVariable" );
    
    /** <p>the value of the clinical variable.</p> */
    public static final Property UNIT = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#unit" );
    
    /** <p>the value of the clinical variable.</p> */
    public static final Property VALUE = m_model.createProperty( "http://www.mnemotix.com/ontology/semEHR#value" );
    
    /** <p>A postal address</p> */
    public static final Resource ADDRESS_INSTANCE = m_model.createResource( "http://www.mnemotix.com/ontology/semEHR#Address" );
    
    /** <p>A clinical variable is the final element of the structure. It can represent 
     *  a value or a group of value, or a group of group of value and so on. There 
     *  is no restriction to the deep that FedEHR can manage, but it is recommended 
     *  not to have more than 5 levels. A clinical varibel must contain one and only 
     *  one of annotation or list of clinical variables or couple of value and unit.</p>
     */
    public static final Resource CLINICAL_VARIABLE = m_model.createResource( "http://www.mnemotix.com/ontology/semEHR#ClinicalVariable" );
    
    /** <p>This object composes the second level of structuring elements. These objects 
     *  are also abstract and can represent anything but subpart of what medical bag 
     *  represents. It can for example represent: Visits if medical bags represent 
     *  hospitalization or follow-up Procedures if medical bags represents visit, 
     *  follow-up or hospitalization Hospitalization if medical bags represent follow-up</p>
     */
    public static final Resource MEDICA_EVENT = m_model.createResource( "http://www.mnemotix.com/ontology/semEHR#MedicaEvent" );
    
    /** <p>The medical bag is the first grouping element of fedEHR. It is totally abstract 
     *  and can represent concepts like, visit or, hospitalization or, medical follow-up. 
     *  This element has no type, is fully abstract and is defined at creation. Even 
     *  if it can define many things it is highly recommended to always use it for 
     *  information at the same level of abstraction.</p>
     */
    public static final Resource MEDICAL_BAG = m_model.createResource( "http://www.mnemotix.com/ontology/semEHR#MedicalBag" );
    
    /** <p>A patient in an healthcare environment.</p> */
    public static final Resource PATIENT = m_model.createResource( "http://www.mnemotix.com/ontology/semEHR#Patient" );
    
    /** <p>A physician in an healthcare environment.</p> */
    public static final Resource PHYSICIAN = m_model.createResource( "http://www.mnemotix.com/ontology/semEHR#Physician" );
    
}
