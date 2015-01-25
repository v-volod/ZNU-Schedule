package ua.zp.rozklad.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resource.AcademicHour;

import static ua.zp.rozklad.app.provider.ScheduleContract.AcademicHour.buildAcademicHourUri;

/**
 * @author Vojko Vladimir
 */
public class AcademicHoursProcessor extends Processor<AcademicHour, Void> {

    public AcademicHoursProcessor(Context context) {
        super(context);
    }

    @Override
    public Void process(ArrayList<AcademicHour> academicHours) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor;

        for (AcademicHour academicHour : academicHours) {
            cursor = resolver.query(buildAcademicHourUri(academicHour.getId()),
                    new String[]{ScheduleContract.AcademicHour._ID}, null, null, null);

            if (!cursor.moveToFirst()) {
                resolver.insert(ScheduleContract.AcademicHour.CONTENT_URI,
                        buildValuesForInsert(academicHour));
            }

            cursor.close();
        }

        return null;
    }

    @Override
    protected ContentValues buildValuesForInsert(AcademicHour academicHour) {
        ContentValues values = buildValuesForUpdate(academicHour);

        values.put(ScheduleContract.AcademicHour._ID, academicHour.getId());

        return values;
    }

    @Override
    protected ContentValues buildValuesForUpdate(AcademicHour academicHour) {
        ContentValues values = new ContentValues();

        values.put(ScheduleContract.AcademicHour.NUM, academicHour.getNum());
        values.put(ScheduleContract.AcademicHour.START_TIME, academicHour.getStartTime());
        values.put(ScheduleContract.AcademicHour.END_TIME, academicHour.getEndTime());

        return values;
    }
}
