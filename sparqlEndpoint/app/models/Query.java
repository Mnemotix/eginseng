package models;

import com.mnemotix.mnemokit.exception.MnxException;
import com.mnemotix.mnemokit.semweb.CoreseManager;
import com.mnemotix.mnemokit.semweb.SPARQLResultFormat;
import com.mnemotix.mnemokit.semweb.api.QueryManager;

import play.data.validation.Constraints.Required;

public class Query {

	@Required
	public String query;

	public String format = SPARQLResultFormat.JSON.toString();;

	private QueryManager queryManager;

	public Query(){
	
	}
	
	public Query(String query){
		this.query = query;
	}
	
	public Query(String query, String format){
		this.query = query;
		this.format = format;
	}
	
	public String run(){
		try {
			if(format.equals(SPARQLResultFormat.CSV.toString().toLowerCase())){
				return this.queryManager.query(query, SPARQLResultFormat.CSV);				
			}
			if(format.equals(SPARQLResultFormat.XML.toString().toLowerCase())){
				return this.queryManager.query(query, SPARQLResultFormat.XML);				
			}
			return this.queryManager.query(query, SPARQLResultFormat.JSON);

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
	

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
}
