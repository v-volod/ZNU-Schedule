package ua.zp.rozklad.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.processor.dependency.AudienceDependency;
import ua.zp.rozklad.app.processor.dependency.ResolveDependencies;
import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resource.Audience;

import static ua.zp.rozklad.app.provider.ScheduleContract.Audience.buildAudienceUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;

/**
 * @author Vojko Vladimir
 */
public class AudiencesProcessor extends Processor<Audience>
        implements ResolveDependencies<AudienceDependency, Audience> {

    public AudiencesProcessor(Context context) {
        super(context);
    }

    @Override
    public void process(ArrayList<Audience> audiences) {
        Cursor cursor;

        for (Audience audience : audiences) {
            cursor = mContentResolver.query(buildAudienceUri(audience.getId()),
                    new String[]{ScheduleContract.Audience.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != audience.getLastUpdate()) {
                    mContentResolver.update(buildAudienceUri(audience.getId()),
                            buildValuesForUpdate(audience), null, null);
                }
            } else {
                mContentResolver.insert(ScheduleContract.Audience.CONTENT_URI,
                        buildValuesForInsert(audience));
            }

            cursor.close();
        }
    }

    @Override
    protected ContentValues buildValuesForInsert(Audience audience) {
        ContentValues values = buildValuesForUpdate(audience);

        values.put(ScheduleContract.Audience._ID, audience.getId());

        return values;
    }

    @Override
    protected ContentValues buildValuesForUpdate(Audience audience) {
        ContentValues values = new ContentValues();

        values.put(ScheduleContract.Audience.CAMPUS_ID, audience.getCampusId());
        values.put(ScheduleContract.Audience.AUDIENCE_NUMBER, audience.getNumber());
        values.put(ScheduleContract.Audience.UPDATED, audience.getLastUpdate());

        return values;
    }

    @Override
    public AudienceDependency resolveDependencies(ArrayList<Audience> audiences) {
        AudienceDependency dependency = new AudienceDependency();
        Cursor cursor;

        for (Audience audience : audiences) {
            cursor = mContentResolver.query(buildAudienceUri(audience.getId()),
                    new String[]{ScheduleContract.Audience.UPDATED}, null, null, null);

            if (cursor.moveToFirst() && cursor.getLong(0) != audience.getLastUpdate() ||
                    !cursor.moveToFirst()) {
                dependency.addAudience(audience);
            }

            cursor.close();
        }

        return dependency;
    }

    public void clean() {
        mContentResolver.delete(ScheduleContract.Audience.CONTENT_URI,
                combineSelection(ScheduleContract.Audience._ID + " NOT IN " +
                                ScheduleContract.FullSchedule.SELECT_DEPENDENT_AUDIENCES
                ),
                null
        );
    }
}
