/* CVS $Id: $ */
package com.mnemotix.ginseng.vocabulary; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from /Users/ereteog/workspace/eginseng/fedEHR2RDF/src/main/resources/foaf.rdf 
 * @author Auto-generated by schemagen on 04 juin 2013 08:29 
 */
public class Foaf {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://xmlns.com/foaf/0.1/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Indicates an account held by this agent.</p> */
    public static final Property ACCOUNT = m_model.createProperty( "http://xmlns.com/foaf/0.1/account" );
    
    /** <p>Indicates the name (identifier) associated with this online account.</p> */
    public static final Property ACCOUNT_NAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/accountName" );
    
    /** <p>Indicates a homepage of the service provide for this online account.</p> */
    public static final Property ACCOUNT_SERVICE_HOMEPAGE = m_model.createProperty( "http://xmlns.com/foaf/0.1/accountServiceHomepage" );
    
    /** <p>The age in years of some agent.</p> */
    public static final Property AGE = m_model.createProperty( "http://xmlns.com/foaf/0.1/age" );
    
    /** <p>An AIM chat ID</p> */
    public static final Property AIM_CHAT_ID = m_model.createProperty( "http://xmlns.com/foaf/0.1/aimChatID" );
    
    /** <p>A location that something is based near, for some broadly human notion of 
     *  near.</p>
     */
    public static final Property BASED_NEAR = m_model.createProperty( "http://xmlns.com/foaf/0.1/based_near" );
    
    /** <p>The birthday of this Agent, represented in mm-dd string form, eg. '12-31'.</p> */
    public static final Property BIRTHDAY = m_model.createProperty( "http://xmlns.com/foaf/0.1/birthday" );
    
    /** <p>A current project this person works on.</p> */
    public static final Property CURRENT_PROJECT = m_model.createProperty( "http://xmlns.com/foaf/0.1/currentProject" );
    
    /** <p>A depiction of some thing.</p> */
    public static final Property DEPICTION = m_model.createProperty( "http://xmlns.com/foaf/0.1/depiction" );
    
    /** <p>A thing depicted in this representation.</p> */
    public static final Property DEPICTS = m_model.createProperty( "http://xmlns.com/foaf/0.1/depicts" );
    
    /** <p>A checksum for the DNA of some thing. Joke.</p> */
    public static final Property DNA_CHECKSUM = m_model.createProperty( "http://xmlns.com/foaf/0.1/dnaChecksum" );
    
    /** <p>The family name of some person.</p> */
    public static final Property FAMILY_NAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/familyName" );
    
    /** <p>The family name of some person.</p> */
    public static final Property FAMILY_NAME_PROP = m_model.createProperty( "http://xmlns.com/foaf/0.1/family_name" );
    
    /** <p>The first name of a person.</p> */
    public static final Property FIRST_NAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/firstName" );
    
    /** <p>The underlying or 'focal' entity associated with some SKOS-described concept.</p> */
    public static final Property FOCUS = m_model.createProperty( "http://xmlns.com/foaf/0.1/focus" );
    
    /** <p>An organization funding a project or person.</p> */
    public static final Property FUNDED_BY = m_model.createProperty( "http://xmlns.com/foaf/0.1/fundedBy" );
    
    /** <p>A textual geekcode for this person, see http://www.geekcode.com/geek.html</p> */
    public static final Property GEEKCODE = m_model.createProperty( "http://xmlns.com/foaf/0.1/geekcode" );
    
    /** <p>The gender of this Agent (typically but not necessarily 'male' or 'female').</p> */
    public static final Property GENDER = m_model.createProperty( "http://xmlns.com/foaf/0.1/gender" );
    
    /** <p>The given name of some person.</p> */
    public static final Property GIVEN_NAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/givenName" );
    
    /** <p>The given name of some person.</p> */
    public static final Property GIVENNAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/givenname" );
    
    /** <p>Indicates an account held by this agent.</p> */
    public static final Property HOLDS_ACCOUNT = m_model.createProperty( "http://xmlns.com/foaf/0.1/holdsAccount" );
    
    /** <p>A homepage for some thing.</p> */
    public static final Property HOMEPAGE = m_model.createProperty( "http://xmlns.com/foaf/0.1/homepage" );
    
    /** <p>An ICQ chat ID</p> */
    public static final Property ICQ_CHAT_ID = m_model.createProperty( "http://xmlns.com/foaf/0.1/icqChatID" );
    
    /** <p>An image that can be used to represent some thing (ie. those depictions which 
     *  are particularly representative of something, eg. one's photo on a homepage).</p>
     */
    public static final Property IMG = m_model.createProperty( "http://xmlns.com/foaf/0.1/img" );
    
    /** <p>A page about a topic of interest to this person.</p> */
    public static final Property INTEREST = m_model.createProperty( "http://xmlns.com/foaf/0.1/interest" );
    
    /** <p>A document that this thing is the primary topic of.</p> */
    public static final Property IS_PRIMARY_TOPIC_OF = m_model.createProperty( "http://xmlns.com/foaf/0.1/isPrimaryTopicOf" );
    
    /** <p>A jabber ID for something.</p> */
    public static final Property JABBER_ID = m_model.createProperty( "http://xmlns.com/foaf/0.1/jabberID" );
    
    /** <p>A person known by this person (indicating some level of reciprocated interaction 
     *  between the parties).</p>
     */
    public static final Property KNOWS = m_model.createProperty( "http://xmlns.com/foaf/0.1/knows" );
    
    /** <p>The last name of a person.</p> */
    public static final Property LAST_NAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/lastName" );
    
    /** <p>A logo representing some thing.</p> */
    public static final Property LOGO = m_model.createProperty( "http://xmlns.com/foaf/0.1/logo" );
    
    /** <p>Something that was made by this agent.</p> */
    public static final Property MADE = m_model.createProperty( "http://xmlns.com/foaf/0.1/made" );
    
    /** <p>An agent that made this thing.</p> */
    public static final Property MAKER = m_model.createProperty( "http://xmlns.com/foaf/0.1/maker" );
    
    /** <p>A personal mailbox, ie. an Internet mailbox associated with exactly one owner, 
     *  the first owner of this mailbox. This is a 'static inverse functional property', 
     *  in that there is (across time and change) at most one individual that ever 
     *  has any particular value for foaf:mbox.</p>
     */
    public static final Property MBOX = m_model.createProperty( "http://xmlns.com/foaf/0.1/mbox" );
    
    /** <p>The sha1sum of the URI of an Internet mailbox associated with exactly one 
     *  owner, the first owner of the mailbox.</p>
     */
    public static final Property MBOX_SHA1SUM = m_model.createProperty( "http://xmlns.com/foaf/0.1/mbox_sha1sum" );
    
    /** <p>Indicates a member of a Group</p> */
    public static final Property MEMBER = m_model.createProperty( "http://xmlns.com/foaf/0.1/member" );
    
    /** <p>Indicates the class of individuals that are a member of a Group</p> */
    public static final Property MEMBERSHIP_CLASS = m_model.createProperty( "http://xmlns.com/foaf/0.1/membershipClass" );
    
    /** <p>An MSN chat ID</p> */
    public static final Property MSN_CHAT_ID = m_model.createProperty( "http://xmlns.com/foaf/0.1/msnChatID" );
    
    /** <p>A Myers Briggs (MBTI) personality classification.</p> */
    public static final Property MYERS_BRIGGS = m_model.createProperty( "http://xmlns.com/foaf/0.1/myersBriggs" );
    
    /** <p>A name for some thing.</p> */
    public static final Property NAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/name" );
    
    /** <p>A short informal nickname characterising an agent (includes login identifiers, 
     *  IRC and other chat nicknames).</p>
     */
    public static final Property NICK = m_model.createProperty( "http://xmlns.com/foaf/0.1/nick" );
    
    /** <p>An OpenID for an Agent.</p> */
    public static final Property OPENID = m_model.createProperty( "http://xmlns.com/foaf/0.1/openid" );
    
    /** <p>A page or document about this thing.</p> */
    public static final Property PAGE = m_model.createProperty( "http://xmlns.com/foaf/0.1/page" );
    
    /** <p>A project this person has previously worked on.</p> */
    public static final Property PAST_PROJECT = m_model.createProperty( "http://xmlns.com/foaf/0.1/pastProject" );
    
    /** <p>A phone, specified using fully qualified tel: URI scheme (refs: http://www.w3.org/Addressing/schemes.html#tel).</p> */
    public static final Property PHONE = m_model.createProperty( "http://xmlns.com/foaf/0.1/phone" );
    
    /** <p>A .plan comment, in the tradition of finger and '.plan' files.</p> */
    public static final Property PLAN = m_model.createProperty( "http://xmlns.com/foaf/0.1/plan" );
    
    /** <p>The primary topic of some page or document.</p> */
    public static final Property PRIMARY_TOPIC = m_model.createProperty( "http://xmlns.com/foaf/0.1/primaryTopic" );
    
    /** <p>A link to the publications of this person.</p> */
    public static final Property PUBLICATIONS = m_model.createProperty( "http://xmlns.com/foaf/0.1/publications" );
    
    /** <p>A homepage of a school attended by the person.</p> */
    public static final Property SCHOOL_HOMEPAGE = m_model.createProperty( "http://xmlns.com/foaf/0.1/schoolHomepage" );
    
    /** <p>A sha1sum hash, in hex.</p> */
    public static final Property SHA1 = m_model.createProperty( "http://xmlns.com/foaf/0.1/sha1" );
    
    /** <p>A Skype ID</p> */
    public static final Property SKYPE_ID = m_model.createProperty( "http://xmlns.com/foaf/0.1/skypeID" );
    
    /** <p>A string expressing what the user is happy for the general public (normally) 
     *  to know about their current activity.</p>
     */
    public static final Property STATUS = m_model.createProperty( "http://xmlns.com/foaf/0.1/status" );
    
    /** <p>The surname of some person.</p> */
    public static final Property SURNAME = m_model.createProperty( "http://xmlns.com/foaf/0.1/surname" );
    
    /** <p>A theme.</p> */
    public static final Property THEME = m_model.createProperty( "http://xmlns.com/foaf/0.1/theme" );
    
    /** <p>A derived thumbnail image.</p> */
    public static final Property THUMBNAIL = m_model.createProperty( "http://xmlns.com/foaf/0.1/thumbnail" );
    
    /** <p>A tipjar document for this agent, describing means for payment and reward.</p> */
    public static final Property TIPJAR = m_model.createProperty( "http://xmlns.com/foaf/0.1/tipjar" );
    
    /** <p>Title (Mr, Mrs, Ms, Dr. etc)</p> */
    public static final Property TITLE = m_model.createProperty( "http://xmlns.com/foaf/0.1/title" );
    
    /** <p>A topic of some page or document.</p> */
    public static final Property TOPIC = m_model.createProperty( "http://xmlns.com/foaf/0.1/topic" );
    
    /** <p>A thing of interest to this person.</p> */
    public static final Property TOPIC_INTEREST = m_model.createProperty( "http://xmlns.com/foaf/0.1/topic_interest" );
    
    /** <p>A weblog of some thing (whether person, group, company etc.).</p> */
    public static final Property WEBLOG = m_model.createProperty( "http://xmlns.com/foaf/0.1/weblog" );
    
    /** <p>A work info homepage of some person; a page about their work for some organization.</p> */
    public static final Property WORK_INFO_HOMEPAGE = m_model.createProperty( "http://xmlns.com/foaf/0.1/workInfoHomepage" );
    
    /** <p>A workplace homepage of some person; the homepage of an organization they 
     *  work for.</p>
     */
    public static final Property WORKPLACE_HOMEPAGE = m_model.createProperty( "http://xmlns.com/foaf/0.1/workplaceHomepage" );
    
    /** <p>A Yahoo chat ID</p> */
    public static final Property YAHOO_CHAT_ID = m_model.createProperty( "http://xmlns.com/foaf/0.1/yahooChatID" );
    
    /** <p>An agent (eg. person, group, software or physical artifact).</p> */
    public static final Resource AGENT = m_model.createResource( "http://xmlns.com/foaf/0.1/Agent" );
    
    /** <p>A document.</p> */
    public static final Resource DOCUMENT = m_model.createResource( "http://xmlns.com/foaf/0.1/Document" );
    
    /** <p>A class of Agents.</p> */
    public static final Resource GROUP = m_model.createResource( "http://xmlns.com/foaf/0.1/Group" );
    
    /** <p>An image.</p> */
    public static final Resource IMAGE = m_model.createResource( "http://xmlns.com/foaf/0.1/Image" );
    
    /** <p>A foaf:LabelProperty is any RDF property with texual values that serve as 
     *  labels.</p>
     */
    public static final Resource LABEL_PROPERTY = m_model.createResource( "http://xmlns.com/foaf/0.1/LabelProperty" );
    
    /** <p>An online account.</p> */
    public static final Resource ONLINE_ACCOUNT = m_model.createResource( "http://xmlns.com/foaf/0.1/OnlineAccount" );
    
    /** <p>An online chat account.</p> */
    public static final Resource ONLINE_CHAT_ACCOUNT = m_model.createResource( "http://xmlns.com/foaf/0.1/OnlineChatAccount" );
    
    /** <p>An online e-commerce account.</p> */
    public static final Resource ONLINE_ECOMMERCE_ACCOUNT = m_model.createResource( "http://xmlns.com/foaf/0.1/OnlineEcommerceAccount" );
    
    /** <p>An online gaming account.</p> */
    public static final Resource ONLINE_GAMING_ACCOUNT = m_model.createResource( "http://xmlns.com/foaf/0.1/OnlineGamingAccount" );
    
    /** <p>An organization.</p> */
    public static final Resource ORGANIZATION = m_model.createResource( "http://xmlns.com/foaf/0.1/Organization" );
    
    /** <p>A person.</p> */
    public static final Resource PERSON = m_model.createResource( "http://xmlns.com/foaf/0.1/Person" );
    
    /** <p>A personal profile RDF document.</p> */
    public static final Resource PERSONAL_PROFILE_DOCUMENT = m_model.createResource( "http://xmlns.com/foaf/0.1/PersonalProfileDocument" );
    
    /** <p>A project (a collective endeavour of some kind).</p> */
    public static final Resource PROJECT = m_model.createResource( "http://xmlns.com/foaf/0.1/Project" );
    
}
