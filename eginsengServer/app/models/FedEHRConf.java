package models;

import play.data.validation.Constraints.Required;

public class FedEHRConf {

	@Required
	public String confFilePath;
	public int nbWorkers = 10;

	public FedEHRConf(){
		
	}
	
	public FedEHRConf(String confFilePath, int nbWorkers){
		this.confFilePath = confFilePath;
		this.nbWorkers = nbWorkers;
	}
	public String getConfFilePath() {
		return confFilePath;
	}

	public void setConfFilePath(String confFilePath) {
		this.confFilePath = confFilePath;
	}

	public int getNbWorkers() {
		return nbWorkers;
	}

	public void setNbWorkers(int nbWorkers) {
		this.nbWorkers = nbWorkers;
	}
}
