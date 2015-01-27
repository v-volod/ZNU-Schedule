package ua.zp.rozklad.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ua.zp.rozklad.app.processor.dependency.AudienceDependency;
import ua.zp.rozklad.app.processor.dependency.ResolveDependency;
import ua.zp.rozklad.app.provider.ScheduleContract;
import ua.zp.rozklad.app.rest.resource.Audience;

import static ua.zp.rozklad.app.provider.ScheduleContract.Audience.buildAudienceUri;
import static ua.zp.rozklad.app.provider.ScheduleContract.Campus.buildCampusUri;

/**
 * @author Vojko Vladimir
 */
public class AudiencesProcessor extends Processor<Audience, AudienceDependency>
        implements ResolveDependency<AudienceDependency, Audience> {

    public AudiencesProcessor(Context context) {
        super(context);
    }

    @Override
    public AudienceDependency process(ArrayList<Audience> audiences) {
        ContentResolver resolver = context.getContentResolver();
        AudienceDependency dependency = new AudienceDependency();

        Cursor cursor;

        for (Audience audience : audiences) {
            cursor = resolver.query(buildAudienceUri(audience.getId()),
                    new String[]{ScheduleContract.Audience.UPDATED}, null, null, null);

            if (cursor.moveToFirst()) {
                if (cursor.getLong(0) != audience.getLastUpdate()) {
                    resolver.update(buildAudienceUri(audience.getId()),
                            buildValuesForUpdate(audience), null, null);

                    resolveDependency(dependency, audience);
                }
            } else {
                resolver.insert(ScheduleContract.Audience.CONTENT_URI,
                        buildValuesForInsert(audience));

                resolveDependency(dependency, audience);
            }

            cursor.close();
        }

        return dependency;
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
    public void resolveDependency(AudienceDependency audienceDependency, Audience audience) {
        Cursor cursor;

        /**
         * Check if the Campus has been loaded.
         * */
        cursor = context.getContentResolver()
                .query(buildCampusUri(audience.getCampusId()), null, null, null, null);
        if (!cursor.moveToFirst()) {
            audienceDependency.addCampus(String.valueOf(audience.getCampusId()));
        }
        cursor.close();
    }
}
