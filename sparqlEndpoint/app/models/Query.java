package models;

import com.mnemotix.mnemokit.exception.MnxException;
import com.mnemotix.mnemokit.semweb.CoreseManager;
import com.mnemotix.mnemokit.semweb.api.QueryManager;

import play.data.validation.Constraints.Required;

public class Query {

	@Required
	public String query;
	public String format;
	private QueryManager queryManager;

	public Query(){
	
	}
	
	public Query(String query){
		this.query = query;
	}
	
	public String run(){
		try {
			return this.queryManager.query(query);
		} catch (MnxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public QueryManager getQueryManager() {
		return queryManager;
	}

	public void setQueryManager(QueryManager queryManager) {
		this.queryManager = queryManager;
	}
}
