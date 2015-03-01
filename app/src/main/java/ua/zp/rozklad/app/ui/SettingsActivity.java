package ua.zp.rozklad.app.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.account.GroupAccount;
import ua.zp.rozklad.app.account.GroupAuthenticatorHelper;
import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.util.PreferencesUtils;

public class SettingsActivity extends ActionBarActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, SharedPreferences.OnSharedPreferenceChangeListener,
        MaterialDialog.ListCallback {

    private String[] intervals;
    private int[] intervalsValues;

    private CheckBox autoSyncCheckBox;
    private RelativeLayout syncInterval;
    private TextView syncIntervalText;

    private Account account;
    private PreferencesUtils preferencesUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            findViewById(R.id.app_bar_shadow).setVisibility(View.GONE);
            getSupportActionBar()
                    .setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
        }

        GroupAuthenticatorHelper mHelper = new GroupAuthenticatorHelper(this);
        GroupAccount groupAccount = mHelper.getActiveAccount();
        if (mHelper.hasAccount() && groupAccount != null) {
            account = groupAccount.getBaseAccount();
        } else {
            finish();
            return;
        }

        preferencesUtils = new PreferencesUtils(this);
        intervals = getResources().getStringArray(R.array.auto_sync_intervals);
        intervalsValues = getResources().getIntArray(R.array.auto_sync_intervals_values);

        /*
        * SetUp UI
        * */
        ((TextView) findViewById(R.id.sync).findViewById(R.id.sub_header_text))
                .setText(R.string.sync);

        View autoSync = findViewById(R.id.auto_sync);

        autoSyncCheckBox = (CheckBox) autoSync.findViewById(R.id.checkbox);
        ((TextView) autoSync.findViewById(R.id.primary_text))
                .setText(R.string.auto_sync);
        ((TextView) autoSync.findViewById(R.id.secondary_text))
                .setText(R.string.auto_sync_summary);

        syncInterval = (RelativeLayout) findViewById(R.id.auto_sync_interval);
        ((TextView) syncInterval.findViewById(R.id.primary_text))
                .setText(R.string.auto_sync_interval);
        syncIntervalText = ((TextView) syncInterval.findViewById(R.id.secondary_text));
        syncIntervalText.setText(findInterval(preferencesUtils.getAutoSyncInterval()));

        View clearCache = findViewById(R.id.clear_cache);
        ((TextView) clearCache.findViewById(R.id.primary_text))
                .setText(R.string.clear_cache);
        ((TextView) clearCache.findViewById(R.id.secondary_text))
                .setText(R.string.clear_cache_summary);

        /*
        * Prepare components
        * */
        boolean isEnabled = preferencesUtils.getAutoSync();
        autoSyncCheckBox.setChecked(isEnabled);
        setSyncIntervalEnabled(isEnabled);

        autoSync.setOnClickListener(this);
        autoSyncCheckBox.setOnCheckedChangeListener(this);
        clearCache.setOnClickListener(this);
        syncInterval.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferencesUtils.getSchedulePreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferencesUtils.getSchedulePreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auto_sync:
                autoSyncCheckBox.setChecked(!autoSyncCheckBox.isChecked());
                break;
            case R.id.clear_cache: {
                new MaterialDialog.Builder(this)
                        .title(R.string.clear_cache_question)
                        .content(R.string.clear_cache_description)
                        .positiveText(R.string.clear)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                clearCache();
                            }
                        })
                        .show();
            }
            break;
            case R.id.auto_sync_interval: {
                int position = findIntervalPosition(preferencesUtils.getAutoSyncInterval());
                new MaterialDialog.Builder(this)
                        .title(R.string.auto_sync_interval)
                        .items(R.array.auto_sync_intervals)
                        .itemsCallbackSingleChoice(position, this)
                        .negativeText(R.string.cancel)
                        .show();
            }
            break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setSyncIntervalEnabled(isChecked);
        preferencesUtils.setAutoSync(isChecked);
    }

    private void setSyncIntervalEnabled(boolean isEnabled) {
        syncInterval.setEnabled(isEnabled);
        for (int i = 0; i < syncInterval.getChildCount(); i++) {
            syncInterval.getChildAt(i).setEnabled(isEnabled);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(PreferencesUtils.KEY_AUTO_SYNC, key)) {
            if (sharedPreferences.getBoolean(key, true)) {
                final int interval = preferencesUtils.getAutoSyncInterval(sharedPreferences);
                ContentResolver.addPeriodicSync(
                        account, ScheduleContract.CONTENT_AUTHORITY, Bundle.EMPTY, interval
                );
            } else {
                ContentResolver.removePeriodicSync(
                        account, ScheduleContract.CONTENT_AUTHORITY, new Bundle()
                );
            }
        } else if (TextUtils.equals(PreferencesUtils.KEY_AUTO_SYNC_INTERVAL, key)) {
            int interval = preferencesUtils.getAutoSyncInterval(sharedPreferences);
            ContentResolver.addPeriodicSync(
                    account,
                    ScheduleContract.CONTENT_AUTHORITY,
                    Bundle.EMPTY,
                    interval * PreferencesUtils.HOUR_IN_SECONDS
            );
        }
    }

    private String findInterval(int value) {
        for (int i = 0; i < intervalsValues.length && i < intervals.length; i++) {
            if (intervalsValues[i] == value) {
                return intervals[i];
            }
        }
        return getString(R.string.not_specified);
    }

    private int findIntervalPosition(int value) {
        for (int i = 0; i < intervalsValues.length && i < intervals.length; i++) {
            if (intervalsValues[i] == value) {
                return i;
            }
        }
        return -1;
    }

    private void clearCache() {
        getContentResolver().delete(ScheduleContract.Schedule.CONTENT_URI, null, null);
        getContentResolver().delete(ScheduleContract.Subject.CONTENT_URI, null, null);
        getContentResolver().delete(ScheduleContract.Audience.CONTENT_URI, null, null);
        getContentResolver().delete(ScheduleContract.Campus.CONTENT_URI, null, null);
        getContentResolver().delete(ScheduleContract.AcademicHour.CONTENT_URI, null, null);
        getContentResolver().delete(ScheduleContract.Lecturer.CONTENT_URI, null, null);
        getContentResolver().delete(ScheduleContract.Group.CONTENT_URI, null, null);
    }

    @Override
    public void onSelection(MaterialDialog dialog, View v, int which, CharSequence charSequence) {
        int value = intervalsValues[which];
        syncIntervalText.setText(findInterval(value));
        preferencesUtils.setAutoSyncInterval(value);
    }
}
