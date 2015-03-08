package ua.zp.rozklad.app.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.util.CalendarUtils;

import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary.Column;
import static ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.buildScheduleItemUri;

public class ScheduleItemActivity extends BaseActivity implements View.OnClickListener {

    public static final String ARG_SCHEDULE_ITEM_ID = "SCHEDULE_ITEM_ID";

    private long lecturerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);

        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            findViewById(R.id.app_bar_shadow).setVisibility(View.GONE);
            getSupportActionBar()
                    .setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
        }

        Intent intent = getIntent();

        String[] classTypes = getResources().getStringArray(R.array.class_type);

        TextView title = (TextView) appBar.findViewById(R.id.title);

        long scheduleItemId = intent.getLongExtra(ARG_SCHEDULE_ITEM_ID, -1);

        Cursor cursor = getContentResolver()
                .query(buildScheduleItemUri(scheduleItemId),
                        FullSchedule.Summary.PROJECTION, null, null, null);

        if (cursor.moveToFirst()) {
            title.setText(cursor.getString(Column.SUBJECT_NAME));
            String lecturerName = cursor.getString(Column.LECTURER_NAME);
            String classTypeText = classTypes[cursor.getInt(Column.CLASS_TYPE)];
            String timeText =
                    CalendarUtils.makeTime(cursor.getLong(Column.ACADEMIC_HOUR_STAT_TIME)) +
                            " - " + CalendarUtils.makeTime(cursor.getLong(Column.ACADEMIC_HOUR_END_TIME));
            String audience = cursor.getString(Column.AUDIENCE_NUMBER);
            final String campus = cursor.getString(Column.CAMPUS_NAME);
            String locationText =
                    (TextUtils.isEmpty(audience) ? "" :
                            getString(R.string.audience) + " " + audience + ", ") + campus;
            final float latitude = cursor.getFloat(Column.CAMPUS_LATITUDE);
            final float longitude = cursor.getFloat(Column.CAMPUS_LONGITUDE);

            lecturerId = cursor.getLong(Column.SCHEDULE_LECTURER_ID);

            View lecturer = findViewById(R.id.lecturer);
            View classType = findViewById(R.id.class_type);
            View location = findViewById(R.id.location);
            View time = findViewById(R.id.time);

            ((ImageView) lecturer.findViewById(R.id.icon))
                    .setImageResource(R.drawable.ic_person_white_24dp);
            ((ImageView) classType.findViewById(R.id.icon))
                    .setImageResource(R.drawable.ic_class_white_24dp);
            ((ImageView) location.findViewById(R.id.icon))
                    .setImageResource(R.drawable.ic_map_white_24dp);
            ((ImageView) time.findViewById(R.id.icon))
                    .setImageResource(R.drawable.ic_query_builder_white_24dp);

            MapFragment mMapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);

            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

            if (latitude == -1.0f && longitude == -1.0f || status != ConnectionResult.SUCCESS) {
                getFragmentManager()
                        .beginTransaction()
                        .hide(mMapFragment)
                        .commit();
            } else {
                mMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap map) {
                        LatLng campusLatLng = new LatLng(latitude, longitude);
                        MarkerOptions mMarkerOptions = new MarkerOptions()
                                .title(campus)
                                .position(campusLatLng);

                        map.setMyLocationEnabled(true);
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(campusLatLng, 16));
                        map.addMarker(mMarkerOptions).showInfoWindow();
                    }
                });
            }

            setUpItem(lecturer, lecturerName, getString(R.string.lecturer), !lecturerName.isEmpty());
            setUpItem(classType, classTypeText, getString(R.string.type), false);
            setUpItem(location, locationText, getString(R.string.location), false);
            setUpItem(time, timeText, getString(R.string.time), false);
        } else {
            finish();
        }

        cursor.close();
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


    public void setUpItem(View view, String primary, String secondary, boolean isClickable) {
        if (TextUtils.isEmpty(primary)) {
            primary = getString(R.string.not_specified);
        }
        if (isClickable) {
            view.setOnClickListener(this);
        }
        ((TextView) view.findViewById(R.id.primary_text)).setText(primary);
        ((TextView) view.findViewById(R.id.secondary_text)).setText(secondary);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lecturer:
                Intent intent = new Intent(this, LecturerScheduleActivity.class);
                intent.putExtra(LecturerScheduleActivity.ARG_LECTURER_ID, lecturerId);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onAccountDeleted() {
        MainActivity.startClearTask(this);
    }

    @Override
    protected void onAccountChanged() {
        MainActivity.startClearTask(this);
    }
}
