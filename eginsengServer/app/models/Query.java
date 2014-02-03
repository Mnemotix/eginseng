package models;

import com.mnemotix.mnemokit.semweb.Format;
import com.sun.org.apache.xml.internal.security.Init;

import play.data.validation.Constraints.Required;

public class Query {

	@Required
	public String query;

	public final long startTime = System.currentTimeMillis();;
	
	public String format;
	
	public String output;

	public String chart;
	
	public String result;
	
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

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public long getStartTime() {
		return startTime;
	}
	
	public String getId() {
		return "query-"+startTime;
	}
	
	public String toString(){
		return "{ " +
				"\"id\": \""+getId()+"\", " +
				"\"startTime\": \""+startTime+"\", " +
				"\"format\": \""+format+"\", " +
				"\"query\": \""+query+"\"" +
				"}";
	}
	
}
