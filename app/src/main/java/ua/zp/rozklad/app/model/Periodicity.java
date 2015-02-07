package ua.zp.rozklad.app.model;

import ua.zp.rozklad.app.rest.resource.CurrentWeek;
import ua.zp.rozklad.app.util.CalendarUtils;

import static java.lang.Math.abs;

/**
 * @author Vojko Vladimir
 */
public class Periodicity {

    private int week;
    private int weekOfYear;

    public Periodicity(CurrentWeek currentWeek, int weekOfYear) {
        this.week = currentWeek.getWeek();
        this.weekOfYear = weekOfYear;
    }

    public Periodicity(int week, int weekOfYear) {
        this.week = week;
        this.weekOfYear = weekOfYear;
    }

    public int getWeek() {
        return week;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public int getPeriodicityForWeek(int weekOfYear) {
        return week + abs(this.weekOfYear - weekOfYear) % 2;
    }
}
