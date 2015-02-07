package ua.zp.rozklad.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.zp.rozklad.app.rest.RESTMethod;

/**
 * @author Vojko Vladimir
 */
public class CurrentWeek extends Resource {

    private String week;

    public CurrentWeek(JSONObject json) throws JSONException {
        week = json.getString(RESTMethod.Key.WEEK);
    }

    public int getWeek() {
        return Integer.parseInt(week);
    }
}
