package ua.zp.rozklad.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import ua.zp.rozklad.app.processor.AcademicHoursProcessor;
import ua.zp.rozklad.app.processor.AudiencesProcessor;
import ua.zp.rozklad.app.processor.CampusesProcessor;
import ua.zp.rozklad.app.processor.LecturersProcessor;
import ua.zp.rozklad.app.processor.ScheduleProcessor;
import ua.zp.rozklad.app.processor.SubjectsProcessor;
import ua.zp.rozklad.app.processor.dependency.AudienceDependency;
import ua.zp.rozklad.app.processor.dependency.ScheduleDependency;
import ua.zp.rozklad.app.rest.GetAcademicHoursMethod;
import ua.zp.rozklad.app.rest.GetAudiencesMethod;
import ua.zp.rozklad.app.rest.GetCampusesMethod;
import ua.zp.rozklad.app.rest.GetGroupsMethod;
import ua.zp.rozklad.app.rest.GetLecturersMethod;
import ua.zp.rozklad.app.rest.GetScheduleMethod;
import ua.zp.rozklad.app.rest.GetSubjectsMethod;
import ua.zp.rozklad.app.rest.MethodResponse;
import ua.zp.rozklad.app.rest.RESTMethod;
import ua.zp.rozklad.app.rest.resource.AcademicHour;
import ua.zp.rozklad.app.rest.resource.Audience;
import ua.zp.rozklad.app.rest.resource.Campus;
import ua.zp.rozklad.app.rest.resource.GlobalScheduleItem;
import ua.zp.rozklad.app.rest.resource.Group;
import ua.zp.rozklad.app.rest.resource.Lecturer;
import ua.zp.rozklad.app.rest.resource.ScheduleItem;
import ua.zp.rozklad.app.rest.resource.Subject;
import ua.zp.rozklad.app.ui.LoginActivity;

/**
 * @author Vojko Vladimir
 */
public class ScheduleSyncAdapter extends AbstractThreadedSyncAdapter {

    private ContentResolver resolver;

    public ScheduleSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        resolver = context.getContentResolver();
    }

    public ScheduleSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        resolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        AccountManager manager = AccountManager.get(getContext());
        String groupId = manager.getUserData(account, LoginActivity.KEY_GROUP_ID);
        String lastUpdate = manager.getUserData(account, LoginActivity.KEY_LAST_UPDATE);

        Log.d("ScheduleLOGS", "PerformSync: " + groupId + ", " + lastUpdate);

        GetGroupsMethod method = new GetGroupsMethod();
        method.prepare(RESTMethod.Filter.BY_ID, groupId);

        MethodResponse<ArrayList<Group>> response = method.executeBlocking();

        if (response.getResponseCode() == RESTMethod.ResponseCode.OK) {
            Group group = response.getResponse().get(0);

            if (group.getLastUpdate() > Long.parseLong(lastUpdate)) {
                performScheduleSync(group);
//                manager.setUserData(account, LoginActivity.KEY_LAST_UPDATE,
//                        String.valueOf(group.getLastUpdate()));
            }
        }

        Log.d("ScheduleLOGS", "onPerformSync finished");
    }

    private void performScheduleSync(Group group) {
        Log.d("ScheduleLOGS", "performScheduleSync for group " + group.getName());

        GetScheduleMethod method = new GetScheduleMethod();
        method.prepare(RESTMethod.Filter.BY_GROUP_ID, String.valueOf(group.getId()));

        MethodResponse<ArrayList<GlobalScheduleItem>> scheduleItemsResponse =
                method.executeBlocking();

        if (scheduleItemsResponse.getResponseCode() == RESTMethod.ResponseCode.OK) {
            ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();

            for (GlobalScheduleItem item: scheduleItemsResponse.getResponse()) {
                scheduleItems.addAll(item.getScheduleItems());
            }

            ScheduleProcessor processor = new ScheduleProcessor(getContext());
            ScheduleDependency dependency = processor.process(scheduleItems);

            Log.d("ScheduleLOGS", "ScheduleDependency " + dependency.toString());

            if (dependency.hasSubjects()) {
                performSubjectsSync(dependency.getSubjects());
            }

            if (dependency.hasAcademicHours()) {
                performAcademicHoursSync(dependency.getAcademicHours());
            }

            if (dependency.hasLecturers()) {
                performLecturersSync(dependency.getLecturers());
            }

            if (dependency.hasAudiences()) {
                performAudiencesSync(dependency.getAudiences());
            }
        }
    }

    private void performSubjectsSync(String[] subjectsIds) {
        GetSubjectsMethod method = new GetSubjectsMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, subjectsIds);

        MethodResponse<ArrayList<Subject>> response = method.executeBlocking();

        if (response.getResponseCode() == RESTMethod.ResponseCode.OK) {
            SubjectsProcessor processor = new SubjectsProcessor(getContext());

            processor.process(response.getResponse());
        }
    }

    private void performAcademicHoursSync(String[] academicHoursIds) {
        GetAcademicHoursMethod method = new GetAcademicHoursMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, academicHoursIds);

        MethodResponse<ArrayList<AcademicHour>> response = method.executeBlocking();

        if (response.getResponseCode() == RESTMethod.ResponseCode.OK) {
            AcademicHoursProcessor processor = new AcademicHoursProcessor(getContext());

            processor.process(response.getResponse());
        }
    }

    private void performLecturersSync(String[] lecturersIds) {
        GetLecturersMethod method = new GetLecturersMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, lecturersIds);

        MethodResponse<ArrayList<Lecturer>> response = method.executeBlocking();

        if (response.getResponseCode() == RESTMethod.ResponseCode.OK) {
            LecturersProcessor processor = new LecturersProcessor(getContext());

            processor.process(response.getResponse());
        }
    }

    private void performAudiencesSync(String[] audiencesIds) {
        GetAudiencesMethod method = new GetAudiencesMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, audiencesIds);

        MethodResponse<ArrayList<Audience>> response = method.executeBlocking();

        if (response.getResponseCode() == RESTMethod.ResponseCode.OK) {
            AudiencesProcessor processor = new AudiencesProcessor(getContext());

            AudienceDependency dependency =  processor.process(response.getResponse());

            if (dependency.hasCampuses()) {
                performCampusesSync(dependency.getCampuses());
            }
        }
    }

    private void performCampusesSync(String[] campusesIds) {
        GetCampusesMethod method = new GetCampusesMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, campusesIds);

        MethodResponse<ArrayList<Campus>> response = method.executeBlocking();

        if (response.getResponseCode() == RESTMethod.ResponseCode.OK) {
            CampusesProcessor processor = new CampusesProcessor(getContext());

            processor.process(response.getResponse());
        }
    }
}
