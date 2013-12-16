package models;

import com.mnemotix.mnemokit.semweb.Format;

import play.data.validation.Constraints.Required;

public class Query {

	@Required
	public String query;

	public String format;
	
	public String output;

	public String chart;

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
	
}
