package ua.pp.rozkladznu.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.pp.rozkladznu.app.rest.RESTMethod;

/**
 * @author Vojko Vladimir
 */
public class CurrentWeek extends Resource {

    private int week;
    private int weekOfYear;

    public CurrentWeek(JSONObject json) throws JSONException {
        week = json.getInt(RESTMethod.Key.TIMETABLE_WEEK);
        weekOfYear = json.getInt(RESTMethod.Key.WEEK_OF_YEAR);
    }

    public int getWeek() {
        return week;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }
}
