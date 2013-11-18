package models;

import play.data.validation.Constraints.Required;

public class FedEHRConf {

	@Required
	public String confFilePath;

	public FedEHRConf(){
		
	}
	
	public FedEHRConf(String confFilePath){
		this.confFilePath = confFilePath;
	}
	public String getConfFilePath() {
		return confFilePath;
	}

	public void setConfFilePath(String confFilePath) {
		this.confFilePath = confFilePath;
	}
}
