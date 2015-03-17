package ua.pp.rozkladznu.app.util;

import java.util.Calendar;

import static java.lang.String.format;
import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.THURSDAY;
import static java.util.Calendar.TUESDAY;
import static java.util.Calendar.WEDNESDAY;
import static java.util.Calendar.getInstance;

/**
 * @author Vojko Vladimir
 */
public class CalendarUtils {
    public static final long SECOND_TIME_STAMP = 1000;
    public static final long MINUTE_TIME_STAMP = 60 * SECOND_TIME_STAMP;
    public static final long HOUR_TIME_STAMP = 60 * MINUTE_TIME_STAMP;
    public static final long HALF_DAY_TIME_STAMP = 12 * HOUR_TIME_STAMP;
    public static final long DAY_TIME_STAMP = 24 * HOUR_TIME_STAMP;
    public static final long WEEK_TIME_STAMP = 7 * DAY_TIME_STAMP;

    public static long addDays(long timeInMillis, int daysToAdd) {
        return timeInMillis + DAY_TIME_STAMP * daysToAdd;
    }

    public static long getCurrentDayStartMillis() {
        return getDayStartMillis(System.currentTimeMillis());
    }

    public static long getDayStartMillis(long dayTimeInMillis) {
        Calendar calendar = getInstance();
        calendar.setTimeInMillis(dayTimeInMillis);
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.setFirstDayOfWeek(MONDAY);
        return calendar.getTimeInMillis();
    }

    public static int getCurrentWeekOfYear() {
        return getInstance().get(Calendar.WEEK_OF_YEAR);
    }

    public static int getCurrentDayOfWeek() {
        int day = 0;

        switch (getInstance().get(Calendar.DAY_OF_WEEK)) {
            case MONDAY:
                day = 0;
                break;
            case TUESDAY:
                day = 1;
                break;
            case WEDNESDAY:
                day = 2;
                break;
            case THURSDAY:
                day = 3;
                break;
            case FRIDAY:
                day = 4;
                break;
            case SUNDAY:
                day = 5;
                break;
            case SATURDAY:
                day = 6;
                break;
        }

        return day;
    }

    public static long getStartOfWeekMillis(int weekOfCurrentYear) {
        return getStartOfWeekMillis(getInstance().get(Calendar.YEAR), weekOfCurrentYear);
    }

    public static long getStartOfWeekMillis(int year, int weekOfYear) {
        Calendar calendar = getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        calendar.setFirstDayOfWeek(MONDAY);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentTimeOfDayMillis() {
        return System.currentTimeMillis() - getDayStartMillis(System.currentTimeMillis());
    }

    public static String makeTime(long timeToMake) {
        return makeTime(timeToMake, ":");
    }

    public static String makeTime(long timeToMake, String divider) {
        int hours = (int) (timeToMake / HOUR_TIME_STAMP);
        int minutes = (int) (timeToMake % HOUR_TIME_STAMP / MINUTE_TIME_STAMP);
        return format("%02d%s%02d", hours, divider, minutes);
    }


}
