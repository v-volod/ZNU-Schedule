package ua.zp.rozklad.app.model;

import ua.zp.rozklad.app.rest.resource.CurrentWeek;

import static java.lang.Math.abs;

/**
 * @author Vojko Vladimir
 */
public class Periodicity {

    private int periodicity;
    private int weekOfYear;

    public Periodicity(CurrentWeek currentWeek, int weekOfYear) {
        this.periodicity = currentWeek.getWeek();
        this.weekOfYear = weekOfYear;
    }

    public Periodicity(int periodicity, int weekOfYear) {
        this.periodicity = periodicity;
        this.weekOfYear = weekOfYear;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public int getPeriodicity(int week) {
        return toggledPeriodicity(abs(weekOfYear - week));
    }

    private int toggledPeriodicity(int times) {
        return (times % 2 == 0) ? periodicity : abs(periodicity - 3);
    }

}
