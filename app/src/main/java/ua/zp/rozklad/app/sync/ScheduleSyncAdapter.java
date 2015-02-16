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

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.account.GroupAuthenticator;
import ua.zp.rozklad.app.model.Periodicity;
import ua.zp.rozklad.app.processor.AcademicHoursProcessor;
import ua.zp.rozklad.app.processor.AudiencesProcessor;
import ua.zp.rozklad.app.processor.CampusesProcessor;
import ua.zp.rozklad.app.processor.GroupsProcessor;
import ua.zp.rozklad.app.processor.LecturersProcessor;
import ua.zp.rozklad.app.processor.ScheduleProcessor;
import ua.zp.rozklad.app.processor.SubjectsProcessor;
import ua.zp.rozklad.app.processor.dependency.AudienceDependency;
import ua.zp.rozklad.app.processor.dependency.ScheduleDependency;
import ua.zp.rozklad.app.rest.GetAcademicHoursMethod;
import ua.zp.rozklad.app.rest.GetAudiencesMethod;
import ua.zp.rozklad.app.rest.GetCampusesMethod;
import ua.zp.rozklad.app.rest.GetCurrentWeekMethod;
import ua.zp.rozklad.app.rest.GetGroupsMethod;
import ua.zp.rozklad.app.rest.GetLecturersMethod;
import ua.zp.rozklad.app.rest.GetScheduleMethod;
import ua.zp.rozklad.app.rest.GetSubjectsMethod;
import ua.zp.rozklad.app.rest.MethodResponse;
import ua.zp.rozklad.app.rest.RESTMethod;
import ua.zp.rozklad.app.rest.resource.AcademicHour;
import ua.zp.rozklad.app.rest.resource.Audience;
import ua.zp.rozklad.app.rest.resource.Campus;
import ua.zp.rozklad.app.rest.resource.CurrentWeek;
import ua.zp.rozklad.app.rest.resource.GlobalScheduleItem;
import ua.zp.rozklad.app.rest.resource.Group;
import ua.zp.rozklad.app.rest.resource.Lecturer;
import ua.zp.rozklad.app.rest.resource.ScheduleItem;
import ua.zp.rozklad.app.rest.resource.Subject;
import ua.zp.rozklad.app.util.CalendarUtils;

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
        String groupId = manager.getUserData(account, GroupAuthenticator.KEY_GROUP_ID);

        Log.d("ScheduleLOGS", "PerformSync for account: " + account + ", group_id: " + groupId);

        GetCurrentWeekMethod currentWeekMethod = new GetCurrentWeekMethod();
        currentWeekMethod.prepare(RESTMethod.Filter.NONE);

        MethodResponse<CurrentWeek> currentWeekMethodResponse = currentWeekMethod.executeBlocking();
        if (currentWeekMethodResponse.getResponseCode() != RESTMethod.ResponseCode.OK) {
            onErrorOccurred();
            return;
        }

        CurrentWeek currentWeek = currentWeekMethodResponse.getResponse();
        App.getInstance().getPreferencesUtils().savePeriodicity(
                new Periodicity(currentWeek, CalendarUtils.getCurrentWeekOfYear())
        );

        GetGroupsMethod method = new GetGroupsMethod();
        method.prepare(RESTMethod.Filter.BY_ID, groupId);

        MethodResponse<ArrayList<Group>> response = method.executeBlocking();

        if (response.getResponseCode() == RESTMethod.ResponseCode.OK) {
            ArrayList<Group> groups = response.getResponse();

            GroupsProcessor processor = new GroupsProcessor(getContext());

            ArrayList<Group> dependency = new ArrayList<>();
            processor.resolveDependency(groups, dependency);

            for (Group group : dependency) {
                performGroupSync(group);
            }

            for (Group group: groups) {
                /*
                * Download Schedule of all dependent lecturers of all groups
                * */
                Log.d("ScheduleLOGS", "Sync Lecturers Schedule");
                GetScheduleMethod getLecturersSchedule = new GetScheduleMethod();
                getLecturersSchedule.prepare(RESTMethod.Filter.LECTURERS_SCHEDULE_BY_GROUP, String.valueOf(group.getId()));

                MethodResponse<ArrayList<GlobalScheduleItem>> scheduleItemsResponse =
                        getLecturersSchedule.executeBlocking();

                if (scheduleItemsResponse.getResponseCode() == RESTMethod.ResponseCode.OK) {
                    processScheduleData(scheduleItemsResponse.getResponse());
                }
            }

            processor.process(groups);
        }

        Log.d("ScheduleLOGS", "onPerformSync finished");
    }

    private void performGroupSync(Group group) {
        Log.d("ScheduleLOGS", "performGroupSync " + group.getName());

        GetScheduleMethod method = new GetScheduleMethod();
        method.prepare(RESTMethod.Filter.BY_GROUP_ID, String.valueOf(group.getId()));

        MethodResponse<ArrayList<GlobalScheduleItem>> scheduleItemsResponse =
                method.executeBlocking();

        if (scheduleItemsResponse.getResponseCode() == RESTMethod.ResponseCode.OK) {
            processScheduleData(scheduleItemsResponse.getResponse());
        }
    }

    private void processScheduleData(ArrayList<GlobalScheduleItem> response) {
        ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();

        for (GlobalScheduleItem item : response) {
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

            AudienceDependency dependency = processor.process(response.getResponse());

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

    private void onErrorOccurred() {
        Log.d("ScheduleLOGS", "onPerformSync finished with error");
        // TODO: Notify about errors.
    }
}
