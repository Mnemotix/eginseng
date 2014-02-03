import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.junit.*;

import com.sun.msv.util.Uri;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;


import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

	@Test
	public void encodeURI(){
		System.out.println(Uri.escapeDisallowedChars("grossesse-amniocentese-prelevementpourl\'amniocentese"));
	}
	
    @Test 
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }
    
    @Test
	public void testDQP(){
		Graph tmpgraph = Graph.create(false);

		Provider tmpsProv = ProviderImpl.create();

		QueryProcessDQP tmpexecDQP = QueryProcessDQP.create(tmpgraph, tmpsProv, true);

		try {

		tmpexecDQP.addRemote(new URL("http://localhost:9000/sparql"), WSImplem.REST);

		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException");
			e.printStackTrace();
		}

		try {
			System.out.println(tmpexecDQP.query("select * where {?x ?p ?type} limit 10"));
		} catch (EngineException e) {
			System.out.println("EngineException");
			e.printStackTrace();
		}
    }
    
/*    
    @Test
    public void renderTemplate() {
        Content html = views.html.index.render("Your new application is ready.");
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("Your new application is ready.");
    }
  */
   
}
