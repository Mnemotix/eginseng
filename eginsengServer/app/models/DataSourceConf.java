package models;

import play.data.validation.Constraints.Required;

public class DataSourceConf {

	@Required
	public String endpoint;
	
	public long startTime = System.currentTimeMillis();
	
	public DataSourceConf(){
		
	}
	
	public DataSourceConf(String endpoint){
		this.endpoint = endpoint;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}


}
