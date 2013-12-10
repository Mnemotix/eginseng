package controllers;

import com.mnemotix.ginseng.fedEHR.crawl.AkkaCrawler;
import play.data.Form;
import play.mvc.*;

import models.FedEHRConf;

public class ImporterFedEHR extends Controller {
  
	public static int nbOfWorkers = 8;

	static Form<FedEHRConf> fedEHRConfForm = Form.form(FedEHRConf.class);
	
    public static Result crawlNode(){
    	Form<FedEHRConf> filledConfForm = fedEHRConfForm.bindFromRequest(); 
    	if(!filledConfForm.hasErrors()){
    		FedEHRConf fedEHRConf = filledConfForm.get();
    		AkkaCrawler.run(fedEHRConf.getNbWorkers(), fedEHRConf.getConfFilePath());
        	return ok("done");
    	}
    	return ok(views.html.fedehr.crawl.render());
    }    
  

}