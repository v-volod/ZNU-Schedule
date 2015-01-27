package ua.zp.rozklad.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.processor.dependency.ResolveDependency;
import ua.zp.rozklad.app.processor.dependency.ScheduleDependency;
import ua.zp.rozklad.app.provider.ScheduleContract.Schedule;
import ua.zp.rozklad.app.rest.resource.ScheduleItem;

import static ua.zp.rozklad.app.provider.ScheduleContract.AcademicHour.buildAcademicHourUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.Audience.buildAudienceUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.Lecturer.buildLecturerUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.Schedule.buildScheduleUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.Subject.buildSubjectUri;

/**
 * @author Vojko Vladimir
 */
public class ScheduleProcessor extends Processor<ScheduleItem, ScheduleDependency>
        implements ResolveDependency<ScheduleDependency, ScheduleItem> {

    public ScheduleProcessor(Context context) {
        super(context);
    }

    @Override
    public ScheduleDependency process(ArrayList<ScheduleItem> scheduleItems) {
        ContentResolver resolver = context.getContentResolver();
        ScheduleDependency dependency = new ScheduleDependency();

        Cursor cursor;

        for (ScheduleItem scheduleItem : scheduleItems) {
            cursor = resolver
                    .query(buildScheduleUri(scheduleItem.getId()), new String[]{Schedule.UPDATED},
                            null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != scheduleItem.getLastUpdate()) {
                    resolver.update(buildScheduleUri(scheduleItem.getId()),
                            buildValuesForUpdate(scheduleItem), null, null);
                    resolveDependency(dependency, scheduleItem);
                }
            } else {
                resolver.insert(Schedule.CONTENT_URI, buildValuesForInsert(scheduleItem));
                resolveDependency(dependency, scheduleItem);
            }

            cursor.close();
        }

        return dependency;
    }

    @Override
    protected ContentValues buildValuesForInsert(ScheduleItem scheduleItem) {
        ContentValues values = buildValuesForUpdate(scheduleItem);

        values.put(Schedule._ID, scheduleItem.getId());

        return values;
    }

    @Override
    protected ContentValues buildValuesForUpdate(ScheduleItem scheduleItem) {
        ContentValues values = new ContentValues();

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
        values.put(Schedule.UPDATED, scheduleItem.getLastUpdate());

        return values;
    }

    @Override
    public void resolveDependency(ScheduleDependency dependency, ScheduleItem scheduleItem) {
        Cursor cursor;

        /**
         * Check if the Subject has been loaded.
         * */
        cursor = context.getContentResolver()
                .query(buildSubjectUri(scheduleItem.getSubjectId()), null, null, null, null);
        if (!cursor.moveToFirst()) {
            dependency.addSubject(String.valueOf(scheduleItem.getSubjectId()));
        }

        cursor.close();

        /**
         * Check if the AcademicHour has been loaded.
         * */
        cursor = context.getContentResolver()
                .query(buildAcademicHourUri(scheduleItem.getAcademicHourId()), null, null, null,
                        null);
        if (!cursor.moveToFirst()) {
            dependency.addAcademicHour(String.valueOf(scheduleItem.getAcademicHourId()));
        }

        cursor.close();

        /**
         * Check if the Lecturer has been loaded.
         * */
        cursor = context.getContentResolver()
                .query(buildLecturerUri(scheduleItem.getLecturerId()), null, null, null, null);
        if (!cursor.moveToFirst()) {
            dependency.addLecturer(String.valueOf(scheduleItem.getLecturerId()));
        }

        cursor.close();

        /**
         * Check if the Audience has been loaded.
         * */
        cursor = context.getContentResolver()
                .query(buildAudienceUri(scheduleItem.getAudienceId()), null, null, null, null);
        if (!cursor.moveToFirst()) {
            dependency.addAudience(String.valueOf(scheduleItem.getAudienceId()));
        }

        cursor.close();
    }
}
