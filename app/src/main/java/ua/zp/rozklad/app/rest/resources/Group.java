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

    public Group(JSONObject groupJson) throws JSONException {
        id = groupJson.getInt("id");
        departmentId = groupJson.getInt("department_id");
        name = groupJson.getString("name");
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
}
