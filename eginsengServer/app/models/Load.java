package models;

import play.data.validation.Constraints.Required;

public class Load {

	@Required
	public String rdfSourcePath;
	@Required
	public String graph;

	public Load(){
		
	}
	
	public Load(String rdfSourcePath, String graph){
		this.rdfSourcePath = rdfSourcePath;
		this.graph = graph;
	}
	public String getRdfSourcePath() {
		return rdfSourcePath;
	}

	public void setRdfSourcePath(String rdfSourcePath) {
		this.rdfSourcePath = rdfSourcePath;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

}
