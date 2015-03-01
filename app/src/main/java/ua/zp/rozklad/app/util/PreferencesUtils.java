package ua.zp.rozklad.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import ua.zp.rozklad.app.R;
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

    public static final String KEY_AUTO_SYNC = "key_auto_sync";
    public static final String KEY_AUTO_SYNC_INTERVAL = "key_auto_sync_interval";
    public static final long HOUR_IN_SECONDS = 86400;

    private Context mContext;

    public PreferencesUtils(Context context) {
        mContext = context;
    }

    public Periodicity getPeriodicity() {
        SharedPreferences preferences =
                mContext.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE);
        return new Periodicity(
                preferences.getInt(PERIODICITY, -1),
                preferences.getInt(WEEK_OF_YEAR, -1)
        );
    }

    public void savePeriodicity(int periodicity, int weekOfYear) {
        mContext.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE)
                .edit()
                .putInt(PERIODICITY, periodicity)
                .putInt(WEEK_OF_YEAR, weekOfYear)
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

    public void removeActiveAccount() {
        mContext.getSharedPreferences(ACCOUNTS_SETTINGS, Context.MODE_PRIVATE)
                .edit()
                .remove(ACTIVE_ACCOUNT_NAME)
                .apply();
    }

    public SharedPreferences getSchedulePreferences() {
        return mContext.getSharedPreferences(SCHEDULE_SETTINGS, Context.MODE_PRIVATE);
    }

    public void setAutoSync(boolean state) {
        getSchedulePreferences().edit()
                .putBoolean(KEY_AUTO_SYNC, state)
                .apply();
    }

    public void setAutoSyncInterval(int value) {
        getSchedulePreferences().edit()
                .putInt(KEY_AUTO_SYNC_INTERVAL, value)
                .apply();
    }

    public boolean getAutoSync() {
        return getSchedulePreferences().getBoolean(KEY_AUTO_SYNC, true);
    }

    public int getAutoSyncInterval(SharedPreferences preferences) {
        return preferences.getInt(KEY_AUTO_SYNC_INTERVAL,
                mContext.getResources().getInteger(R.integer.auto_sync_interval_default)
        );
    }

    public int getAutoSyncInterval() {
        return getSchedulePreferences().getInt(KEY_AUTO_SYNC_INTERVAL,
                mContext.getResources().getInteger(R.integer.auto_sync_interval_default)
        );
    }
}
