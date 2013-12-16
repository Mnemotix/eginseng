package controllers;

import play.mvc.*;

public class Application extends Controller {
  	
    public static Result index() {
        return TODO;
    }
	
    public static Result untrail(String path) {
    	   return redirect("/" + path);
	}
	
}
