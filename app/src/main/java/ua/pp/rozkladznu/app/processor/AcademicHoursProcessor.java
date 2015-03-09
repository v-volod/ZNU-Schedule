package ua.pp.rozkladznu.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.pp.rozkladznu.app.provider.ScheduleContract;
import ua.pp.rozkladznu.app.rest.resource.AcademicHour;

import static ua.pp.rozkladznu.app.provider.ScheduleContract.AcademicHour.buildAcademicHourUri;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.combineSelection;

/**
 * @author Vojko Vladimir
 */
public class AcademicHoursProcessor extends Processor<AcademicHour> {

    public AcademicHoursProcessor(Context context) {
        super(context);
    }

    @Override
    public void process(ArrayList<AcademicHour> academicHours) {
        Cursor cursor;

        for (AcademicHour academicHour : academicHours) {
            cursor = mContentResolver.query(buildAcademicHourUri(academicHour.getId()),
                    new String[]{ScheduleContract.AcademicHour._ID}, null, null, null);

            if (!cursor.moveToFirst()) {
                mContentResolver.insert(ScheduleContract.AcademicHour.CONTENT_URI,
                        buildValuesForInsert(academicHour));
            }

            cursor.close();
        }
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

    public void clean() {
        mContentResolver.delete(ScheduleContract.AcademicHour.CONTENT_URI,
                combineSelection(ScheduleContract.AcademicHour._ID + " NOT IN " +
                                ScheduleContract.FullSchedule.SELECT_DEPENDENT_ACADEMIC_HOURS
                ),
                null
        );
    }
}
