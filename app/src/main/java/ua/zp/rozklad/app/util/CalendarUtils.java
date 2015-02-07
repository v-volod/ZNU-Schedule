package ua.zp.rozklad.app.util;

import java.util.Calendar;

/**
 * @author Vojko Vladimir
 */
public class CalendarUtils {
    public static final long SECOND_TIME_STAMP = 1000;
    public static final long MINUTE_TIME_STAMP = 60 * SECOND_TIME_STAMP;
    public static final long HOUR_TIME_STAMP = 60 * MINUTE_TIME_STAMP;
    public static final long DAY_TIME_STAMP = 24 * HOUR_TIME_STAMP;
    public static final long WEEK_TIME_STAMP = 7 * DAY_TIME_STAMP;

    public static long getCurrentWeekStartInMillis() {
        Calendar calendar = Calendar.getInstance();

        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return calendar.getTimeInMillis();
    }

    public static long addWeeks(long timeInMillis, int weeksToAdd) {
        return timeInMillis + WEEK_TIME_STAMP * weeksToAdd;
    }

    public static long addDays(long timeInMillis, int daysToAdd) {
        return timeInMillis + DAY_TIME_STAMP * daysToAdd;
    }

    public static long getCurrentDayStartInMillis() {
        return getDayStartInMillis(System.currentTimeMillis());
    }

    public static long getDayStartInMillis(long dayTimeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dayTimeInMillis);
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar.getTimeInMillis();
    }

    public static int getCurrentWeekOfYear() {
        return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }
}
