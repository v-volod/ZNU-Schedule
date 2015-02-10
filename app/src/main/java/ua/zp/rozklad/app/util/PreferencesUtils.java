package ua.zp.rozklad.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import ua.zp.rozklad.app.model.Periodicity;

/**
 * @author Vojko Vladimir
 */
public class PreferencesUtils {
    private static final String SCHEDULE_SETTINGS = "schedule_settings";

    private static final String PERIODICITY = "periodicity";
    private static final String WEEK_OF_YEAR = "week_of_year";

    private Context context;

    public PreferencesUtils(Context context) {
        this.context = context;
    }

    public Periodicity getPeriodicity() {
        SharedPreferences preferences =
                context.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE);
        return new Periodicity(
                preferences.getInt(PERIODICITY, 1),
                preferences.getInt(WEEK_OF_YEAR, CalendarUtils.getCurrentWeekOfYear())
        );
    }

    public void savePeriodicity(Periodicity periodicity) {
        context.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE)
                .edit()
                .putInt(PERIODICITY, periodicity.getPeriodicity())
                .putInt(WEEK_OF_YEAR, periodicity.getWeekOfYear())
                .apply();
    }
}
