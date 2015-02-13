package ua.zp.rozklad.app.model;

import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.Calendar;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.util.CalendarUtils;

import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Column;

/**
 * @author Vojko Vladimir
 */
public class ScheduleItem {

    private long startTime;
    private long endTime;
    private String subject;
    private String info;

    public ScheduleItem(Resources res, Cursor cursor) {
        startTime = cursor.getLong(Column.ACADEMIC_HOUR_STAT_TIME);
        endTime = cursor.getInt(Column.ACADEMIC_HOUR_END_TIME);
        subject = cursor.getString(Column.SUBJECT_NAME);
        String audience = cursor.getString(Column.AUDIENCE_NUMBER);
        String campus = cursor.getString(Column.CAMPUS_NAME);
        String location = audience +
                ((TextUtils.isEmpty(campus)) ? "" :
                        (TextUtils.isEmpty(audience) ? "(" : " (") + campus + ")");
        int type = cursor.getInt(Column.CLASS_TYPE);
        info = cursor.getString(Column.LECTURER_NAME) + "\n" +
                ((TextUtils.isEmpty(location)) ? "" : location) +
                ((type == 0) ? "" :
                        ((TextUtils.isEmpty(location) ? "" : ", ") +
                                res.getStringArray(R.array.class_type)[type]));
    }

    private boolean inRange(long startAt, long endAt) {
        Calendar calendarNow = Calendar.getInstance();
        long timeNow = calendarNow.get(Calendar.AM_PM) * CalendarUtils.HALF_DAY_TIME_STAMP
                + calendarNow.get(Calendar.HOUR) * CalendarUtils.HOUR_TIME_STAMP +
                calendarNow.get(Calendar.MINUTE) * CalendarUtils.MINUTE_TIME_STAMP;
        return timeNow >= startAt && timeNow <= endAt;
    }

    public boolean isNow() {
        return inRange(startTime, endTime);
    }

    public String getSubject() {
        return subject;
    }

    public String getInfo() {
        return info;
    }

    public String getStartTime() {
        return CalendarUtils.makeTime(startTime);
    }

    public String getEndTime() {
        return CalendarUtils.makeTime(endTime);
    }
}
