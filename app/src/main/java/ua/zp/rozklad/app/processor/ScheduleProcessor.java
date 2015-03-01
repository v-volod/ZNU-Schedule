package ua.zp.rozklad.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.ArrayList;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.processor.dependency.ResolveDependencies;
import ua.zp.rozklad.app.processor.dependency.ScheduleDependency;
import ua.zp.rozklad.app.provider.ScheduleContract.Schedule;
import ua.zp.rozklad.app.rest.resource.ScheduleItem;

import static ua.zp.rozklad.app.provider.ScheduleContract.AcademicHour.buildAcademicHourUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.Group.buildGroupUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.Schedule.Summary.Selection;
import static ua.zp.rozklad.app.provider.ScheduleContract.Schedule.buildScheduleUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.combine;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineArgs;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineProjection;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;

/**
 * @author Vojko Vladimir
 */
public class ScheduleProcessor extends Processor<ScheduleItem>
        implements ResolveDependencies<ScheduleDependency, ScheduleItem> {

    public ScheduleProcessor(Context context) {
        super(context);
    }

    @Override
    public void process(ArrayList<ScheduleItem> scheduleItems) {
        Cursor cursor;

        for (ScheduleItem scheduleItem : scheduleItems) {
            cursor = mContentResolver
                    .query(Schedule.CONTENT_URI,
                            combineProjection(Schedule.UPDATED, Schedule.SCHEDULE_ID),
                            combineSelection(Selection.SCHEDULE_ID, Selection.GROUP_ID),
                            combineArgs(scheduleItem.getId(), scheduleItem.getGroupId()), null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != scheduleItem.getLastUpdate()) {
                    mContentResolver.update(buildScheduleUri(scheduleItem.getId()),
                            buildValuesForUpdate(scheduleItem), null, null);
                    App.LOG_D("updated " + scheduleItem + " -> " + cursor.getLong(0));
                }
            } else {
                mContentResolver.insert(Schedule.CONTENT_URI, buildValuesForInsert(scheduleItem));
                App.LOG_D("inserted " + scheduleItem);
            }

            cursor.close();
        }
    }

    @Override
    protected ContentValues buildValuesForInsert(ScheduleItem scheduleItem) {
        return buildValuesForUpdate(scheduleItem);
    }

    @Override
    protected ContentValues buildValuesForUpdate(ScheduleItem scheduleItem) {
        ContentValues values = new ContentValues();

        values.put(Schedule.SCHEDULE_ID, scheduleItem.getId());
        values.put(Schedule.GROUP_ID, scheduleItem.getGroupId());
        values.put(Schedule.SUBGROUP, scheduleItem.getSubgroup());
        values.put(Schedule.SUBJECT_ID, scheduleItem.getSubjectId());
        values.put(Schedule.DAY_OF_WEEK, scheduleItem.getDayOfWeek());
        values.put(Schedule.ACADEMIC_HOUR_ID, scheduleItem.getAcademicHourId());
        values.put(Schedule.LECTURER_ID, scheduleItem.getLecturerId());
        values.put(Schedule.AUDIENCE_ID, scheduleItem.getAudienceId());
        values.put(Schedule.PERIODICITY, scheduleItem.getPeriodicity());
        values.put(Schedule.START_DATE, scheduleItem.getStartDate());
        values.put(Schedule.END_DATE, scheduleItem.getEndDate());
        values.put(Schedule.CLASS_TYPE, scheduleItem.getClassType());
        values.put(Schedule.FREE_TRAJECTORY, scheduleItem.getFreeTrajectory());
        values.put(Schedule.UPDATED, scheduleItem.getLastUpdate());

        return values;
    }

    @Override
    public ScheduleDependency resolveDependencies(ArrayList<ScheduleItem> scheduleItems) {
        ScheduleDependency dependency = new ScheduleDependency();

        Cursor cursor;
        for (ScheduleItem scheduleItem : scheduleItems) {
            cursor = mContentResolver
                    .query(Schedule.CONTENT_URI,
                            combineProjection(Schedule.UPDATED, Schedule.SCHEDULE_ID),
                            combineSelection(Selection.SCHEDULE_ID, Selection.GROUP_ID),
                            combineArgs(scheduleItem.getId(), scheduleItem.getGroupId()), null);

            if (cursor.moveToFirst() && cursor.getLong(0) != scheduleItem.getLastUpdate() ||
                    !cursor.moveToFirst()) {
                dependency.addSubject(String.valueOf(scheduleItem.getSubjectId()));
                if (!hasAcademicHour(scheduleItem.getAcademicHourId())) {
                    dependency.addAcademicHour(String.valueOf(scheduleItem.getAcademicHourId()));
                }
                dependency.addLecturer(String.valueOf(scheduleItem.getLecturerId()));
                dependency.addAudience(String.valueOf(scheduleItem.getAudienceId()));
            }

            if (!hasGroup(scheduleItem.getGroupId())) {
                dependency.addGroup(String.valueOf(scheduleItem.getGroupId()));
            }

            cursor.close();
        }

        return dependency;
    }

    private boolean hasGroup(long id) {
        Cursor cursor = mContentResolver
                .query(buildGroupUri(id), null, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }

    private boolean hasAcademicHour(long id) {
        Cursor cursor = mContentResolver
                .query(buildAcademicHourUri(id), null, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }

    public void cleanGroupSchedule(ArrayList<ScheduleItem> scheduleItems) {
        if (scheduleItems.size() > 0) {
            mContentResolver.delete(Schedule.CONTENT_URI,
                    combineSelection(Schedule.GROUP_ID + " IN " + generateGroupIds(scheduleItems),
                            Schedule.SCHEDULE_ID + " NOT IN " + generateScheduleIds(scheduleItems)
                    ),
                    null
            );
        }
    }

    public void deleteGroupSchedule(long groupId) {
        mContentResolver.delete(Schedule.CONTENT_URI, Schedule.GROUP_ID + " = " + groupId, null);
    }

    public void cleanLecturerSchedule(ArrayList<ScheduleItem> scheduleItems) {
        if (scheduleItems.size() > 0) {
            mContentResolver.delete(Schedule.CONTENT_URI,
                    combineSelection(
                            Schedule.LECTURER_ID + "=" + scheduleItems.get(0).getLecturerId(),
                            Schedule.SCHEDULE_ID + " NOT IN " + generateScheduleIds(scheduleItems)
                    ),
                    null
            );
        }
    }

    private String generateScheduleIds(ArrayList<ScheduleItem> scheduleItems) {
        String ids = "(";
        for (int i = 0; i < scheduleItems.size(); i++) {
            ids += scheduleItems.get(i).getId();
            if (i < scheduleItems.size() - 1) {
                ids += ",";
            }
        }
        return ids + ")";
    }

    private String generateGroupIds(ArrayList<ScheduleItem> scheduleItems) {
        String ids = "(";
        for (int i = 0; i < scheduleItems.size(); i++) {
            ids += scheduleItems.get(i).getGroupId();
            if (i < scheduleItems.size() - 1) {
                ids += ",";
            }
        }
        return ids + ")";
    }
}
