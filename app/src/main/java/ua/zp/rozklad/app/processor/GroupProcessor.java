package ua.zp.rozklad.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resources.Group;

/**
 * @author Vojko Vladimir
 */
public class GroupProcessor extends Processor<GroupProcessor> {

    public GroupProcessor(Context context) {
        super(context);
    }

    @Override
    public void insert(JSONObject response) throws JSONException {
        Group group = new Group(response);

        Uri groupUri = ScheduleContract.Group.CONTENT_URI;

        ContentValues values = new ContentValues();
        values.put(ScheduleContract.Group._ID, group.getId());
        values.put(ScheduleContract.Group.DEPARTMENT_ID, group.getDepartmentId());
        values.put(ScheduleContract.Group.GROUP_NAME, group.getName());
        values.put(ScheduleContract.SyncColumns.UPDATED, group.getLastUpdate());

        context.getContentResolver().insert(groupUri, values);
    }

    @Override
    public void update(JSONObject response) throws JSONException {
        Group group = new Group(response);

        Uri groupUri = ScheduleContract.Group.buildGroupUri(String.valueOf(group.getId()));

        ContentValues values = new ContentValues();
        values.put(ScheduleContract.Group.DEPARTMENT_ID, group.getDepartmentId());
        values.put(ScheduleContract.Group.GROUP_NAME, group.getName());

        context.getContentResolver().insert(groupUri, values);
    }

}
