import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

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
    	DateFormat mediumDateFormatEN = 
    			DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, new Locale("EN","en"));
    	System.out.print(mediumDateFormatEN.format(new Date())+": ");
    	System.out.print(request.path());
    	Map<String, String[]> queryString = request.queryString();
    	for(String queryParam : queryString.keySet()){
    		System.out.print(" "+queryParam + "="+ Arrays.asList(queryString.get(queryParam)));
    	}
    	System.out.println();
        return super.onRequest(request, actionMethod);
    }
    
}