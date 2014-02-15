package models;


import org.codehaus.jackson.node.ObjectNode;

import com.mnemotix.mnemokit.semweb.Format;

import play.data.validation.Constraints.Required;
import play.libs.Json;

public class Query {

	@Required
	public String query;

	public final long startTime = System.currentTimeMillis();;
	
	public String format;
	
	public long timeout = 120000;
	
	public String output;

	public String chart;
	
	public Object result;
	
	
	
	public Query(){
	
	}
	
	public Query(String query){
		this.query = query;
	}
	
	public Query(String query, String format){
		this.query = query;
		this.format = format;
	}
	
	public Query(String query, String format, String chart){
		this.query = query;
		this.format = Format.JSON.toString();
		this.chart = chart;
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
		this.output = format;
	}
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.format = output;
		this.output = output;
	}

	public String getChart() {
		return chart;
	}

	public void setChart(String chart) {
		this.chart = chart;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public long getStartTime() {
		return startTime;
	}
	
	public String getId() {
		return "query-"+startTime;
	}
	

	public ObjectNode toJSON(){
		ObjectNode json = Json.newObject();
		json.put("id", getId());
		json.put("startTime", new Long(startTime));
		json.put("format", format);
		json.put("query", query);
		return json;
	}
	
	public String toString(){
		return toJSON().toString();
	}
	
}
