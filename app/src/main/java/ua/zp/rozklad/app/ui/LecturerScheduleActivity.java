package ua.zp.rozklad.app.ui;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.provider.ScheduleContract.Lecturer;

import static ua.zp.rozklad.app.provider.ScheduleContract.Lecturer.buildLecturerUri;

public class LecturerScheduleActivity extends BaseActivity
        implements ScheduleOfWeekFragment.OnPeriodicityChangeListener,
        ScheduleFragment.OnScheduleItemClickListener {

    public static final String ARG_LECTURER_ID = "LECTURER_ID";

    private static final int[] PERIODICITY_SUBTITLE_RES_ID = {
            0,
            R.string.numerator,
            R.string.denominator
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_schedule);

        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long lecturerId = getIntent().getLongExtra(ARG_LECTURER_ID, -1);

        Cursor cursor = getContentResolver()
                .query(buildLecturerUri(lecturerId), new String[]{Lecturer.LECTURER_NAME},
                        null, null, null);

        if (cursor.moveToFirst()) {
            getSupportActionBar().setTitle(cursor.getString(0));
        } else {
            finish();
        }

        cursor.close();

        Fragment fragment = ScheduleOfWeekFragment.newInstance(lecturerId);
        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPeriodicityChanged(int periodicity) {
        getSupportActionBar().setSubtitle(PERIODICITY_SUBTITLE_RES_ID[periodicity]);
    }

    @Override
    public void onScheduleItemClicked(long scheduleItemId) {
        Intent intent = new Intent(this, ScheduleItemActivity.class);
        intent.putExtra(ScheduleItemActivity.ARG_SCHEDULE_ITEM_ID, scheduleItemId);
        startActivity(intent);
    }
}
