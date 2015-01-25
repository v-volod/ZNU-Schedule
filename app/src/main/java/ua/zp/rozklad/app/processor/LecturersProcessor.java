package ua.zp.rozklad.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resource.Lecturer;

import static ua.zp.rozklad.app.provider.ScheduleContract.Lecturer.buildLecturerUri;

/**
 * @author Vojko Vladimir
 */
public class LecturersProcessor extends Processor<Lecturer, Void> {

    public LecturersProcessor(Context context) {
        super(context);
    }

    @Override
    public Void process(ArrayList<Lecturer> lecturers) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor;

        for (Lecturer lecturer : lecturers) {
            cursor = resolver.query(buildLecturerUri(lecturer.getId()),
                    new String[]{ScheduleContract.Lecturer.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != lecturer.getLastUpdate()) {
                    resolver.update(buildLecturerUri(lecturer.getId()),
                            buildValuesForUpdate(lecturer), null, null);
                }
            } else {
                resolver.insert(ScheduleContract.Lecturer.CONTENT_URI,
                        buildValuesForInsert(lecturer));
            }

            cursor.close();
        }

        return null;
    }

    @Override
    protected ContentValues buildValuesForInsert(Lecturer lecturer) {
        ContentValues values = buildValuesForUpdate(lecturer);

        values.put(ScheduleContract.Lecturer._ID, lecturer.getId());

        return values;
    }

    @Override
    protected ContentValues buildValuesForUpdate(Lecturer lecturer) {
        ContentValues values = new ContentValues();

        values.put(ScheduleContract.Lecturer.LECTURER_NAME, lecturer.getName());
        values.put(ScheduleContract.Lecturer.UPDATED, lecturer.getLastUpdate());

        return values;
    }
}
