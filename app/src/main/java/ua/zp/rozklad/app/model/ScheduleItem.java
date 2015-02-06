package ua.zp.rozklad.app.model;

import android.database.Cursor;

import java.util.Calendar;

import static java.lang.String.format;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.SUMMARY.COLUMN;

/**
 * @author Vojko Vladimir
 */
public class ScheduleItem {
    private static final String INFO_FORMAT_FULL = "%s\n%d (%s)";
    private static final String INFO_FORMAT_SHORT = "%s\n%s";

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int HALF_OF_DAY = 12 * HOUR;

    private long startTime;
    private long endTime;
    private String subject;
    private String info;

    public ScheduleItem(Cursor cursor) {
        int audienceNumber = cursor.getInt(COLUMN.AUDIENCE_NUMBER);
        startTime = cursor.getLong(COLUMN.ACADEMIC_HOUR_STAT_TIME);
        endTime = cursor.getInt(COLUMN.ACADEMIC_HOUR_END_TIME);
        subject = cursor.getString(COLUMN.SUBJECT_NAME);
        info = (audienceNumber > 0) ?
                format(INFO_FORMAT_FULL, cursor.getString(COLUMN.LECTURER_NAME), audienceNumber,
                        cursor.getString(COLUMN.CAMPUS_NAME)) :
                format(INFO_FORMAT_SHORT, cursor.getString(COLUMN.LECTURER_NAME),
                        cursor.getInt(COLUMN.CAMPUS_NAME));
    }

    private String makeTime(long timeToMake) {
        return makeTime(timeToMake, ":");
    }

    private String makeTime(long timeToMake, String divider) {
        int hours = (int) (timeToMake / HOUR);
        int minutes = (int) (timeToMake % HOUR / MINUTE);
        return format("%02d%s%02d", hours, divider, minutes);
    }

    private boolean inRange(long startAt, long endAt) {
        Calendar calendarNow = Calendar.getInstance();
        long timeNow = calendarNow.get(Calendar.AM_PM) * HALF_OF_DAY
                + calendarNow.get(Calendar.HOUR) * HOUR +
                calendarNow.get(Calendar.MINUTE) * MINUTE;
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
        return makeTime(startTime);
    }

    public String getEndTime() {
        return makeTime(endTime);
    }
}
