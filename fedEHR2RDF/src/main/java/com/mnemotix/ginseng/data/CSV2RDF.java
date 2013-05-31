package com.mnemotix.ginseng.data;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import com.csvreader.CsvReader;

public class CSV2RDF {

	public void cvs2rdf(
			Reader in, 
			Writer out, 
			String separator, 
			int columnId, 
			Map<Integer, String> columnPropertyMap, 
			boolean useHeaderAsProperty, 
			boolean hasHeader, 
			String baseURI){
		CsvReader csvReader = new CsvReader(in);
	}
	
}
