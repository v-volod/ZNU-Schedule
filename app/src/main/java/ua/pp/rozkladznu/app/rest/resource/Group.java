package ua.pp.rozkladznu.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import ua.pp.rozkladznu.app.rest.RESTMethod;

/**
 * @author Vojko Vladimir
 */
public class Group extends Resource {

    private int id;
    private int departmentId;
    private int course;
    private String name;
    private int subgroupCount;
    private long lastUpdate;

    public Group(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        departmentId = json.getInt(RESTMethod.Key.DEPARTMENT_ID);
        course = json.getInt(RESTMethod.Key.COURSE);
        name = json.getString(RESTMethod.Key.NAME);
        subgroupCount = json.getInt(RESTMethod.Key.SUBGROUP_COUNT);
        lastUpdate = json.getLong(RESTMethod.Key.LAST_UPDATE);
    }

    public int getId() {
        return id;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public int getCourse() {
        return course;
    }

    public String getName() {
        return name;
    }

    public int getSubgroupCount() {
        return subgroupCount;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String toString() {
        return "Group[id: " + id +", name: " + name + ", lastUpdate: " + lastUpdate + "]";
    }
}
