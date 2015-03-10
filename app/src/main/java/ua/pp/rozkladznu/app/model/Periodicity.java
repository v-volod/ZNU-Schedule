package ua.pp.rozkladznu.app.model;

import static java.lang.Math.abs;

/**
 * @author Vojko Vladimir
 */
public class Periodicity {

    private int periodicity;
    private int weekOfYear;

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
        if (!isValid()) {
            return 0;
        }
        return toggledPeriodicity(abs(weekOfYear - week));
    }

    private int toggledPeriodicity(int times) {
        return (times % 2 == 0) ? periodicity : abs(periodicity - 3);
    }

    public boolean isValid() {
        return periodicity != -1 && weekOfYear != -1;
    }
}
