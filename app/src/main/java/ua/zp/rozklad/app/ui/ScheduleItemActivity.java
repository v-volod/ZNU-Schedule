package ua.zp.rozklad.app.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.util.CalendarUtils;

import static java.lang.String.format;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Column;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;

/**
 * Created by kkxmshu on 03.02.15.
 */
public class ScheduleItemActivity extends ActionBarActivity {

    private String[] lesson_type;

    private Toolbar appBar;
    private TextView title;

    private View location;
    private View lecturer;
    private View type;
    private View time;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);

        appBar = (Toolbar) findViewById(R.id.extended_app_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackground(R.color.schedule_item_activity_statusbar);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Intent intent = getIntent();

        lesson_type = getResources().getStringArray(R.array.lesson_type);

        title = (TextView) appBar.findViewById(R.id.title);

        Long id = intent.getLongExtra("id", 0);
        Log.d("ScheduleItemActivity", "id: " + id);

        Resources res = getResources();
        Cursor cursor = getContentResolver().query(FullSchedule.CONTENT_URI
                        .buildUpon()
                        .appendPath(String.valueOf(id)).build(),
                FullSchedule.Summary.PROJECTION, null, null, null);

        String sSubj = "";
        String sLect = "";
        String sLoc = "";
        int type_index = 0;
        String sTime = "";

        if (cursor != null) {

            while (cursor.moveToNext()) {
                sSubj = cursor.getString(Column.SUBJECT_NAME);
                sLect = cursor.getString(Column.LECTURER_NAME);
                sLoc = format("Аудитория %d, %s", cursor.getInt(Column.AUDIENCE_NUMBER),
                        cursor.getString(Column.CAMPUS_NAME));
                type_index = cursor.getInt(Column.CLASS_TYPE);
                sTime = CalendarUtils.makeTime(cursor.getLong(Column.ACADEMIC_HOUR_STAT_TIME)) +
                        " - " + CalendarUtils.makeTime(cursor.getLong(Column
                        .ACADEMIC_HOUR_END_TIME));
                title.setText(sSubj);

            }

        }

        location = findViewById(R.id.location_layout);
        lecturer = findViewById(R.id.lecturer_layout);
        type = findViewById(R.id.type_layout);
        time = findViewById(R.id.time_layout);

        setTexts(location, sLoc, res.getString(R.string.location));
        ImageView thumb = (ImageView) location.findViewById(R.id.thumbnail);
        thumb.setImageResource(R.drawable.ic_place_white_18dp);
        thumb.setColorFilter(res.getColor(R.color.nav_drawer_icon_tint));

        setTexts(lecturer, sLect, res.getString(R.string.lecturer));
        thumb = (ImageView) lecturer.findViewById(R.id.thumbnail);
        thumb.setImageResource(R.drawable.ic_info_white_18dp);
        thumb.setColorFilter(res.getColor(R.color.nav_drawer_icon_tint));

        setTexts(type, lesson_type[type_index], res.getString(R.string.type));
        type.setClickable(false);
        setTexts(time, sTime, res.getString(R.string.time));
        time.setClickable(false);

    }


    public static void setTexts(View v, String t, String d) {
        TextView text = (TextView) v.findViewById(R.id.text);
        text.setText(t);

        TextView description = (TextView) v.findViewById(R.id.description);
        description.setText(d);
    }
}
