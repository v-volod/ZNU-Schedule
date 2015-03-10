package ua.pp.rozkladznu.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.pp.rozkladznu.app.rest.RESTMethod;

/**
 * @author Vojko Vladimir
 */
public class Subject extends Resource {

    private int id;
    private String name;
    private long lastUpdate;

    public Subject(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        name = json.getString(RESTMethod.Key.NAME);
        lastUpdate = json.getLong(RESTMethod.Key.LAST_UPDATE);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }
}
