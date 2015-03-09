package ua.pp.rozkladznu.app.processor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.pp.rozkladznu.app.account.GroupAuthenticator;
import ua.pp.rozkladznu.app.account.GroupAuthenticatorHelper;
import ua.pp.rozkladznu.app.processor.dependency.ResolveDependencies;
import ua.pp.rozkladznu.app.provider.ScheduleContract;
import ua.pp.rozkladznu.app.rest.resource.Group;

import static ua.pp.rozkladznu.app.provider.ScheduleContract.Group.buildGroupUri;

/**
 * @author Vojko Vladimir
 */
public class GroupsProcessor extends Processor<Group>
        implements ResolveDependencies<ArrayList<Group>, Group> {

    public GroupsProcessor(Context context) {
        super(context);
    }

    @Override
    public void process(ArrayList<Group> groups) {
        Cursor cursor;

        for (Group group : groups) {
            cursor = mContentResolver.query(buildGroupUri(group.getId()),
                    new String[]{ScheduleContract.Group.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                mContentResolver.update(
                        buildGroupUri(group.getId()), buildValuesForUpdate(group), null, null
                );
            } else {
                mContentResolver.insert(ScheduleContract.Group.CONTENT_URI, buildValuesForInsert(group));
            }

            cursor.close();
        }
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

    @Override
    public ArrayList<Group> resolveDependencies(ArrayList<Group> groups) {
        ArrayList<Group> dependencies = new ArrayList<>();

        Cursor cursor;
        for (Group group : groups) {
            cursor = mContentResolver.query(buildGroupUri(group.getId()),
                    new String[]{ScheduleContract.Group.UPDATED}, null, null, null);

            if (cursor.moveToFirst() && cursor.getLong(0) != group.getLastUpdate() ||
                    !cursor.moveToFirst()) {
                dependencies.add(group);
            }

            cursor.close();
        }

        return dependencies;
    }

    public void clean() {
        AccountManager manager = AccountManager.get(mContext);
        GroupAuthenticatorHelper helper = new GroupAuthenticatorHelper(mContext);

        Account[] accounts = helper.getAccounts();
        String ids = "";
        String id;

        for (int i = 0; i < accounts.length; i++) {
            id = manager.getUserData(accounts[i], GroupAuthenticator.KEY_GROUP_ID);
            if (id != null) {
                if (!ids.isEmpty() && i > 0 && i < accounts.length - 1) {
                    ids += ",";
                }
                ids += id;
            }
        }

        ContentValues values = new ContentValues();
        values.put(ScheduleContract.Schedule.UPDATED, 0);
        mContentResolver.update(ScheduleContract.Group.CONTENT_URI, values,
                ScheduleContract.Group._ID + " NOT IN (" + ids + ")", null);
    }
}
