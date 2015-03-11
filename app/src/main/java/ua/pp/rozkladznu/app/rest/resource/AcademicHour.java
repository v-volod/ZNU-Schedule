package ua.pp.rozkladznu.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.pp.rozkladznu.app.rest.RESTMethod;

import static java.lang.Integer.parseInt;

/**
 * @author Vojko Vladimir
 */
public class AcademicHour extends Resource {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;

    private int id;
    private int num;
    private long startTime;
    private long endTime;

    public AcademicHour(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        num = json.getInt(RESTMethod.Key.NUM);
        startTime = parseTime(json.getString(RESTMethod.Key.TIME_START));
        endTime = parseTime(json.getString(RESTMethod.Key.TIME_END));
    }

    private long parseTime(String timeToParse) {
        String[] time = timeToParse.split(":");
        return HOUR * parseInt(time[0]) + MINUTE * parseInt(time[1]) + SECOND * parseInt(time[2]);
    }

    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
