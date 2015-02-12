package ua.zp.rozklad.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import ua.zp.rozklad.app.model.Periodicity;

/**
 * @author Vojko Vladimir
 */
public class PreferencesUtils {
    private static final String ACCOUNTS_SETTINGS = "accounts_settings";
    private static final String SCHEDULE_SETTINGS = "schedule_settings";

    private static final String PERIODICITY = "periodicity";
    private static final String WEEK_OF_YEAR = "week_of_year";
    private static final String ACTIVE_ACCOUNT_NAME = "active_account_name";

    private Context mContext;

    public PreferencesUtils(Context context) {
        mContext = context;
    }

    public Periodicity getPeriodicity() {
        SharedPreferences preferences =
                mContext.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE);
        return new Periodicity(
                preferences.getInt(PERIODICITY, 1),
                preferences.getInt(WEEK_OF_YEAR, CalendarUtils.getCurrentWeekOfYear())
        );
    }

    public void savePeriodicity(Periodicity periodicity) {
        mContext.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE)
                .edit()
                .putInt(PERIODICITY, periodicity.getPeriodicity())
                .putInt(WEEK_OF_YEAR, periodicity.getWeekOfYear())
                .apply();
    }

    public String getActiveAccount() {
        SharedPreferences preferences =
                mContext.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE);

        return preferences.getString(ACTIVE_ACCOUNT_NAME, "");
    }

    public void saveActiveAccount(String accountName) {
        mContext.getSharedPreferences(ACCOUNTS_SETTINGS, Context.MODE_PRIVATE)
                .edit()
                .putString(ACTIVE_ACCOUNT_NAME, accountName)
                .apply();
    }
}
