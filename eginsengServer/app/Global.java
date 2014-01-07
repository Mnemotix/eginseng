import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.util.CharArrayMap.EntrySet;

import play.Application;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {}

    @Override
    public void onStop(Application app) {}

    @SuppressWarnings("rawtypes")
    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
    	System.out.print(request.path());
    	Map<String, String[]> queryString = request.queryString();
    	for(String queryParam : queryString.keySet()){
    		System.out.print(" "+queryParam + "="+ Arrays.asList(queryString.get(queryParam)));
    	}
    	System.out.println();
        return super.onRequest(request, actionMethod);
    }
    
}