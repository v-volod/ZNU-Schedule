package ua.zp.rozklad.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resource.Campus;

import static ua.zp.rozklad.app.provider.ScheduleContract.Campus.buildCampusUri;

/**
 * @author Vojko Vladimir
 */
public class CampusesProcessor extends Processor<Campus, Void> {

    public CampusesProcessor(Context context) {
        super(context);
    }

    @Override
    public Void process(ArrayList<Campus> campuses) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor;

        for (Campus campus : campuses) {
            cursor = resolver.query(buildCampusUri(campus.getId()),
                    new String[]{ScheduleContract.Campus.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != campus.getLastUpdate()) {
                    resolver.update(buildCampusUri(campus.getId()), buildValuesForUpdate(campus),
                            null, null);
                }
            } else {
                resolver.insert(ScheduleContract.Campus.CONTENT_URI, buildValuesForInsert(campus));
            }

            cursor.close();
        }

        return null;
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

        return values;
    }
}
