import org.junit.*;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */   
    @Test
    public void testEndpointWithJena() {
    	String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> select * where {?x a rdfs:Class . ?x rdfs:label ?label} limit 10";
    	String endpoint = "http://ginseng.i3s.unice.fr:9000/sparql";
         QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, queryString);

         ResultSet results = qexec.execSelect();
         assert(results.hasNext());
        
         while (results.hasNext()) {
             QuerySolution row = results.next();
             System.out.println(row.get("label"));
             System.out.println(row.toString());
         }

    }
  
    
}
