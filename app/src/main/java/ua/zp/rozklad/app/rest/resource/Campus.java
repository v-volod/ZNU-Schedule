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
    private float latitude;
    private float longitude;
    private long lastUpdate;

    public Campus(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        name = json.getString(RESTMethod.Key.NAME);
        latitude = (float) json.getDouble(RESTMethod.Key.LATITUDE);
        longitude = (float) json.getDouble(RESTMethod.Key.LONGITUDE);
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

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
