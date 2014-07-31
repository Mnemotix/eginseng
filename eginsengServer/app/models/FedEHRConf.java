package models;

import play.data.validation.Constraints.Required;

public class FedEHRConf {

	@Required
	public String confFilePath; //We have a conf file by fedEHR node
	public int nbWorkers = 10; //The number of workers for crawling the nodes

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
