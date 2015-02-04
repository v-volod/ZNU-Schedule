package ua.zp.rozklad.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.rest.resource.Resource;

/**
 * Created by kkxmshu on 03.02.15.
 */
public class ScheduleItemActivity extends ActionBarActivity {

    private Toolbar appBar;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);

        appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackground(R.color.schedule_item_activity_statusbar);

        getSupportActionBar().setTitle("Фiлософiя");

        Resource res = getResources();

        View location = findViewById(R.id.location_layout);
        setTexts(location, "Аудитория 123, 6 корпус", res.getString(R.string.location));
        ImageView thumb = (ImageView) location.findViewById(R.id.thumbnail);
        thumb.setImageResource(R.drawable.ic_place_white_18dp);
        thumb.setColorFilter(res.getColor(R.color.nav_drawer_icon_tint));
        location.setClickable(true);

        View lecturer = findViewById(R.id.lecturer_layout);
        setTexts(lecturer, "Чопоров С. В.", res.getString(R.string.lecturer));
        thumb = (ImageView) lecturer.findViewById(R.id.thumbnail);
        thumb.setImageResource(R.drawable.ic_info_white_18dp);
        thumb.setColorFilter(res.getColor(R.color.nav_drawer_icon_tint));
        lecturer.setClickable(true);

        View type = findViewById(R.id.type_layout);
        setTexts(type, "Лекция", res.getString(R.string.type));

        View time = findViewById(R.id.time_layout);
        setTexts(time, "08:00 - 09:20", res.getString(R.string.time));
    }


    public static void setTexts(View v, String t, String d) {
        TextView text = (TextView) v.findViewById(R.id.text);
        text.setText(t);

        TextView description = (TextView) v.findViewById(R.id.description);
        description.setText(d);
    }
}
