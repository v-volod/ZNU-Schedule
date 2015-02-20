package ua.zp.rozklad.app.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.account.GroupAccount;
import ua.zp.rozklad.app.account.GroupAuthenticator;
import ua.zp.rozklad.app.account.GroupAuthenticatorHelper;

/**
 * Created by kkxmshu on 18.02.15.
 */
public class SyncSettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_AUTO_SYNC = "ua.zp.rozklad.app.KEY_AUTO_SYNC";
    private static final String KEY_AUTO_SYNC_INTERVAL = "ua.zp.rozklad.app.KEY_AUTO_SYNC_INTERVAL";
    private static final long DAY_SECONDS = 86400;


    private Account account;

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sync_prefs);
        final ListPreference interval = (ListPreference) getPreferenceManager()
                .findPreference(KEY_AUTO_SYNC_INTERVAL);
        interval.setSummary(interval.getEntry());

        account = null;
        GroupAuthenticatorHelper mHelper = new GroupAuthenticatorHelper(getActivity());
        if(mHelper.hasAccount()) {
            Log.d("SYNC_SETTINGS -> hasAccount", "TRUE");
            account = mHelper.getActiveAccount().getBaseAccount();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(TextUtils.equals(KEY_AUTO_SYNC, key)) {
            if(sharedPreferences.getBoolean(key, false)) {
                final int interval = Integer.parseInt(sharedPreferences.getString(
                        KEY_AUTO_SYNC_INTERVAL,
                        getString(R.string.auto_sync_interval_default)
                ));
                ContentResolver.addPeriodicSync(account, "ua.zp.rozklad.app", Bundle.EMPTY, interval);
            } else {
                ContentResolver.removePeriodicSync(account, "ua.zp.rozklad.app", new Bundle());
            }
        } else if (TextUtils.equals(KEY_AUTO_SYNC_INTERVAL, key)) {
            final ListPreference interval = (ListPreference) getPreferenceManager()
                    .findPreference(key);
            interval.setSummary(interval.getEntry());
            // TODO: Translate string
            ContentResolver.addPeriodicSync(
               account, "ua.zp.rozklad.app",
               Bundle.EMPTY, Long.parseLong(interval.getValue()) * DAY_SECONDS
            );
        }
    }
}
