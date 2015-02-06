package ua.zp.rozklad.app.util;

import java.util.Calendar;

/**
 * @author Vojko Vladimir
 */
public class CalendarUtils {

    public static long getCurrentWeekInMillis() {
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        calendar.add(Calendar.WEEK_OF_YEAR, weeksToAdd);
        return calendar.getTimeInMillis();
    }
}
