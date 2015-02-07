package ua.zp.rozklad.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resource.Group;

import static ua.zp.rozklad.app.provider.ScheduleContract.Group.buildGroupUri;

/**
 * @author Vojko Vladimir
 */
public class GroupsProcessor extends Processor<Group, ArrayList<Group>> {

    public GroupsProcessor(Context context) {
        super(context);
    }

    @Override
    public ArrayList<Group> process(ArrayList<Group> groups) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor;

        ArrayList<Group> dependency = new ArrayList<>();

        for (Group group : groups) {
            cursor = resolver.query(buildGroupUri(group.getId()),
                    new String[]{ScheduleContract.Group.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != group.getLastUpdate()) {
                    resolver.update(
                            buildGroupUri(group.getId()), buildValuesForUpdate(group), null, null
                    );
                    dependency.add(group);
                }
            } else {
                resolver.insert(ScheduleContract.Group.CONTENT_URI, buildValuesForInsert(group));
                dependency.add(group);
            }

            cursor.close();
        }

        return dependency;
    }

    @Override
    protected ContentValues buildValuesForInsert(Group group) {
        ContentValues values = buildValuesForUpdate(group);

        values.put(ScheduleContract.Group._ID, group.getId());

        return values;
    }

    @Override
    protected ContentValues buildValuesForUpdate(Group group) {
        ContentValues values = new ContentValues();

        values.put(ScheduleContract.Group.DEPARTMENT_ID, group.getDepartmentId());
        values.put(ScheduleContract.Group.COURSE, group.getCourse());
        values.put(ScheduleContract.Group.GROUP_NAME, group.getName());
        values.put(ScheduleContract.Group.SUBGROUP_COUNT, group.getSubgroupCount());
        values.put(ScheduleContract.Group.UPDATED, group.getLastUpdate());

        return values;
    }
}
