package ua.zp.rozklad.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resource.Subject;

import static ua.zp.rozklad.app.provider.ScheduleContract.Subject.buildSubjectUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;

/**
 * @author Vojko Vladimir
 */
public class SubjectsProcessor extends Processor<Subject> {

    public SubjectsProcessor(Context context) {
        super(context);
    }

    @Override
    public void process(ArrayList<Subject> subjects) {
        Cursor cursor;

        for (Subject subject : subjects) {
            cursor = mContentResolver.query(buildSubjectUri(subject.getId()),
                    new String[]{ScheduleContract.Subject.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != subject.getLastUpdate()) {
                    mContentResolver.update(buildSubjectUri(subject.getId()),
                            buildValuesForUpdate(subject), null, null);
                }
            } else {
                mContentResolver.insert(ScheduleContract.Subject.CONTENT_URI,
                        buildValuesForInsert(subject));
            }

            cursor.close();
        }
    }

    @Override
    protected ContentValues buildValuesForInsert(Subject subject) {
        ContentValues values = buildValuesForUpdate(subject);

        values.put(ScheduleContract.Subject._ID, subject.getId());

        return values;
    }

    @Override
    protected ContentValues buildValuesForUpdate(Subject subject) {
        ContentValues values = new ContentValues();

        values.put(ScheduleContract.Subject.SUBJECT_NAME, subject.getName());
        values.put(ScheduleContract.Subject.UPDATED, subject.getLastUpdate());

        return values;
    }

    public void clean() {
        mContentResolver.delete(ScheduleContract.Subject.CONTENT_URI,
                combineSelection(ScheduleContract.Subject._ID + " NOT IN " +
                                ScheduleContract.FullSchedule.SELECT_DEPENDENT_SUBJECTS
                ),
                null
        );
    }
}
