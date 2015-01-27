package ua.zp.rozklad.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.zp.rozklad.app.rest.RESTMethod;

/**
 * @author Vojko Vladimir
 */
public class Audience extends Resource {

    private int id;
    private int campusId;
    private String number;
    private long lastUpdate;

    public Audience(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        campusId = json.getInt(RESTMethod.Key.CAMPUS_ID);
        number = json.getString(RESTMethod.Key.AUDIENCE);
        lastUpdate = json.getLong(RESTMethod.Key.LAST_UPDATE);
    }

    public int getId() {
        return id;
    }

    public int getCampusId() {
        return campusId;
    }

    public String getNumber() {
        return number;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }
}
