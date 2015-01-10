package ua.zp.rozklad.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.zp.rozklad.app.rest.RESTMethod;

/**
 * @author Vojko Vladimir
 */
public class Group extends Resource {

    private int id;
    private int departmentId;
    private String name;
    private long lastUpdate;

    public Group(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        departmentId = json.getInt(RESTMethod.Key.DEPARTMENT_ID);
        name = json.getString(RESTMethod.Key.NAME);
        lastUpdate = json.getLong(RESTMethod.Key.LAST_UPDATE);
    }

    public int getId() {
        return id;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

}
