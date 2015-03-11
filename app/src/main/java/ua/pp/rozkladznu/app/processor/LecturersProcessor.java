package ua.pp.rozkladznu.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;

import ua.pp.rozkladznu.app.processor.dependency.LecturerDependency;
import ua.pp.rozkladznu.app.processor.dependency.ResolveDependencies;
import ua.pp.rozkladznu.app.provider.ScheduleContract;
import ua.pp.rozkladznu.app.rest.resource.Lecturer;

import static ua.pp.rozkladznu.app.provider.ScheduleContract.Lecturer.buildLecturerUri;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.combineSelection;

/**
 * @author Vojko Vladimir
 */
public class LecturersProcessor extends Processor<Lecturer>
        implements ResolveDependencies<LecturerDependency, Lecturer> {

    public LecturersProcessor(Context context) {
        super(context);
    }

    @Override
    public void process(ArrayList<Lecturer> lecturers) {
        Cursor cursor;

        for (Lecturer lecturer : lecturers) {
            cursor = mContentResolver.query(buildLecturerUri(lecturer.getId()),
                    new String[]{ScheduleContract.Lecturer.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != lecturer.getLastUpdate()) {
                    mContentResolver.update(buildLecturerUri(lecturer.getId()),
                            buildValuesForUpdate(lecturer), null, null);
                }
            } else {
                mContentResolver.insert(ScheduleContract.Lecturer.CONTENT_URI,
                        buildValuesForInsert(lecturer));
            }

            cursor.close();
        }
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

    @Override
    public LecturerDependency resolveDependencies(ArrayList<Lecturer> lecturers) {
        LecturerDependency dependency = new LecturerDependency();

        Cursor cursor;

        for (Lecturer lecturer : lecturers) {
            cursor = mContentResolver.query(buildLecturerUri(lecturer.getId()),
                    new String[]{ScheduleContract.Lecturer.UPDATED}, null, null, null);

            if (cursor.moveToFirst() && cursor.getLong(0) != lecturer.getLastUpdate() ||
                    !cursor.moveToFirst() &&
                            /*
                            * Skip empty lecturer (prevent downloading schedule of the empty
                            * lecturer)
                            * */
                            !TextUtils.isEmpty(lecturer.getName())) {
                dependency.addLecturer(lecturer);
            }

            cursor.close();
        }

        return dependency;
    }

    public void clean() {
        mContentResolver.delete(ScheduleContract.Lecturer.CONTENT_URI,
                combineSelection(
                        ScheduleContract.Lecturer._ID + " NOT IN " +
                                ScheduleContract.FullSchedule.SELECT_DEPENDENT_LECTURERS
                ),
                null
        );
    }
}
