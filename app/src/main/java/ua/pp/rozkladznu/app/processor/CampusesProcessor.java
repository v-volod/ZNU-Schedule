package ua.pp.rozkladznu.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.pp.rozkladznu.app.provider.ScheduleContract;
import ua.pp.rozkladznu.app.rest.resource.Campus;

import static ua.pp.rozkladznu.app.provider.ScheduleContract.Campus.buildCampusUri;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.combineSelection;

/**
 * @author Vojko Vladimir
 */
public class CampusesProcessor extends Processor<Campus> {

    public CampusesProcessor(Context context) {
        super(context);
    }

    @Override
    public void process(ArrayList<Campus> campuses) {
        Cursor cursor;

        for (Campus campus : campuses) {
            cursor = mContentResolver.query(buildCampusUri(campus.getId()),
                    new String[]{ScheduleContract.Campus.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != campus.getLastUpdate()) {
                    mContentResolver.update(buildCampusUri(campus.getId()), buildValuesForUpdate(campus),
                            null, null);
                }
            } else {
                mContentResolver.insert(ScheduleContract.Campus.CONTENT_URI, buildValuesForInsert(campus));
            }

            cursor.close();
        }
    }

    @Override
    protected ContentValues buildValuesForInsert(Campus campus) {
        ContentValues values = buildValuesForUpdate(campus);

        values.put(ScheduleContract.Campus._ID, campus.getId());

        return values;
    }

    @Override
    protected ContentValues buildValuesForUpdate(Campus campus) {
        ContentValues values = new ContentValues();

        values.put(ScheduleContract.Campus.CAMPUS_NAME, campus.getName());
        values.put(ScheduleContract.Campus.UPDATED, campus.getLastUpdate());
        values.put(ScheduleContract.Campus.LATITUDE, campus.getLatitude());
        values.put(ScheduleContract.Campus.LONGITUDE, campus.getLongitude());

        return values;
    }

    public void clean() {
        mContentResolver.delete(ScheduleContract.Campus.CONTENT_URI,
                combineSelection(ScheduleContract.Campus._ID + " NOT IN " +
                        ScheduleContract.Audience.SELECT_DEPENDENT_CAMPUSES),
                null
        );
    }
}
