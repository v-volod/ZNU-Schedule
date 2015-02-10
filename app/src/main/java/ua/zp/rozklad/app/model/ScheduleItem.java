package ua.zp.rozklad.app.model;

import android.database.Cursor;

import java.util.Calendar;

import ua.zp.rozklad.app.util.CalendarUtils;

import static java.lang.String.format;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Column;

/**
 * @author Vojko Vladimir
 */
public class ScheduleItem {
    private static final String INFO_FORMAT_FULL = "%s\n%d (%s)";
    private static final String INFO_FORMAT_SHORT = "%s\n%s";

    private long startTime;
    private long endTime;
    private String subject;
    private String info;

    public ScheduleItem(Cursor cursor) {
        int audienceNumber = cursor.getInt(Column.AUDIENCE_NUMBER);
        startTime = cursor.getLong(Column.ACADEMIC_HOUR_STAT_TIME);
        endTime = cursor.getInt(Column.ACADEMIC_HOUR_END_TIME);
        subject = cursor.getString(Column.SUBJECT_NAME);
        info = (audienceNumber > 0) ?
                format(INFO_FORMAT_FULL, cursor.getString(Column.LECTURER_NAME), audienceNumber,
                        cursor.getString(Column.CAMPUS_NAME)) :
                format(INFO_FORMAT_SHORT, cursor.getString(Column.LECTURER_NAME),
                        cursor.getInt(Column.CAMPUS_NAME));
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
