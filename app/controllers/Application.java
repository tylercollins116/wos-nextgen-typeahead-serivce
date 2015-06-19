package controllers;

import models.Suggester;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {

    public static Result index(String q) {
        return ok(index.render(q));
    }
    
    
    public static Result suggest(String query) {
        return  ok(Json.toJson(Suggester.lookup(query, 10)));
    }

    
    
    
}
