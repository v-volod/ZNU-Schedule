package ua.zp.rozklad.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.zp.rozklad.app.rest.RESTMethod;

/**
 * @author Vojko Vladimir
 */
public class Campus extends Resource {

    private int id;
    private String name;
    private long lastUpdate;

    public Campus(JSONObject json) throws JSONException {
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
