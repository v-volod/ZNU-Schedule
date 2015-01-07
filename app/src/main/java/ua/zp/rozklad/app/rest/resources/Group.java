package ua.zp.rozklad.app.rest.resources;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Vojko Vladimir
 */
public class Group implements Resources {

    private int id;
    private int departmentId;
    private String name;
    private long lastUpdate;

    public Group(JSONObject groupJson) throws JSONException {
        id = groupJson.getInt("id");
        // TODO: Fix departament_id -> department_id in API
        departmentId = groupJson.getInt("departament_id");
        name = groupJson.getString("name");
        // TODO: Fetch last update time from api.
        lastUpdate = System.currentTimeMillis();
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
