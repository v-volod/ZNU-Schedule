package ua.zp.rozklad.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import java.util.ArrayList;

import ua.zp.rozklad.app.App;
import ua.zp.rozklad.app.account.GroupAuthenticator;
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
import ua.zp.rozklad.app.util.PreferencesUtils;

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
        PreferencesUtils mPrefUtils = App.getInstance().getPreferencesUtils();
        AccountManager manager = AccountManager.get(getContext());
        String groupId = manager.getUserData(account, GroupAuthenticator.KEY_GROUP_ID);

        GetCurrentWeekMethod currentWeekMethod = new GetCurrentWeekMethod();
        currentWeekMethod.prepare(RESTMethod.Filter.NONE);

        MethodResponse<CurrentWeek> currentWeekMethodResponse = currentWeekMethod.executeBlocking();
        if (!canProcess(currentWeekMethodResponse, syncResult) &&
                !mPrefUtils.getPeriodicity().isValid()) {
            return;
        } else {
            CurrentWeek currentWeek = currentWeekMethodResponse.getResponse();
            mPrefUtils.savePeriodicity(currentWeek.getWeek(), CalendarUtils.getCurrentWeekOfYear());
        }

        GetGroupsMethod method = new GetGroupsMethod();
        method.prepare(RESTMethod.Filter.BY_ID, groupId);

        MethodResponse<ArrayList<Group>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            ArrayList<Group> groups = response.getResponse();

            GroupsProcessor processor = new GroupsProcessor(getContext());
            processor.process(groups);

            for (Group group : groups) {
                performGroupSync(syncResult, group);
                /*
                * Download Schedule of all dependent lecturers of all groups
                * */
                GetScheduleMethod getLecturersSchedule = new GetScheduleMethod();
                getLecturersSchedule.prepare(RESTMethod.Filter.LECTURERS_SCHEDULE_BY_GROUP, String.valueOf(group.getId()));

                MethodResponse<ArrayList<GlobalScheduleItem>> scheduleItemsResponse =
                        getLecturersSchedule.executeBlocking();

                if (canProcess(scheduleItemsResponse, syncResult)) {
                    processScheduleData(syncResult, scheduleItemsResponse.getResponse());
                }
            }
        }
    }

    private void performGroupSync(SyncResult syncResult, Group group) {
        GetScheduleMethod method = new GetScheduleMethod();
        method.prepare(RESTMethod.Filter.BY_GROUP_ID, String.valueOf(group.getId()));

        MethodResponse<ArrayList<GlobalScheduleItem>> scheduleItemsResponse =
                method.executeBlocking();

        if (canProcess(scheduleItemsResponse, syncResult)) {
            processScheduleData(syncResult, scheduleItemsResponse.getResponse());
        }
    }

    private void processScheduleData(SyncResult syncResult, ArrayList<GlobalScheduleItem> response) {
        ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();

        for (GlobalScheduleItem item : response) {
            scheduleItems.addAll(item.getScheduleItems());
        }

        ScheduleProcessor processor = new ScheduleProcessor(getContext());
        ScheduleDependency dependency = processor.process(scheduleItems);

        if (dependency.hasSubjects()) {
            performSubjectsSync(syncResult, dependency.getSubjects());
        }

        if (dependency.hasAcademicHours()) {
            performAcademicHoursSync(syncResult, dependency.getAcademicHours());
        }

        if (dependency.hasLecturers()) {
            performLecturersSync(syncResult, dependency.getLecturers());
        }

        if (dependency.hasAudiences()) {
            performAudiencesSync(syncResult, dependency.getAudiences());
        }
    }

    private void performSubjectsSync(SyncResult syncResult, String[] subjectsIds) {
        GetSubjectsMethod method = new GetSubjectsMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, subjectsIds);

        MethodResponse<ArrayList<Subject>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            SubjectsProcessor processor = new SubjectsProcessor(getContext());

            processor.process(response.getResponse());
        }
    }

    private void performAcademicHoursSync(SyncResult syncResult, String[] academicHoursIds) {
        GetAcademicHoursMethod method = new GetAcademicHoursMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, academicHoursIds);

        MethodResponse<ArrayList<AcademicHour>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            AcademicHoursProcessor processor = new AcademicHoursProcessor(getContext());

            processor.process(response.getResponse());
        }
    }

    private void performLecturersSync(SyncResult syncResult, String[] lecturersIds) {
        GetLecturersMethod method = new GetLecturersMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, lecturersIds);

        MethodResponse<ArrayList<Lecturer>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            LecturersProcessor processor = new LecturersProcessor(getContext());

            processor.process(response.getResponse());
        }
    }

    private void performAudiencesSync(SyncResult syncResult, String[] audiencesIds) {
        GetAudiencesMethod method = new GetAudiencesMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, audiencesIds);

        MethodResponse<ArrayList<Audience>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            AudiencesProcessor processor = new AudiencesProcessor(getContext());

            AudienceDependency dependency = processor.process(response.getResponse());

            if (dependency.hasCampuses()) {
                performCampusesSync(syncResult, dependency.getCampuses());
            }
        }
    }

    private void performCampusesSync(SyncResult syncResult, String[] campusesIds) {
        GetCampusesMethod method = new GetCampusesMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, campusesIds);

        MethodResponse<ArrayList<Campus>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            CampusesProcessor processor = new CampusesProcessor(getContext());

            processor.process(response.getResponse());
        }
    }

    private boolean canProcess(MethodResponse methodResponse, SyncResult syncResult) {
        if (methodResponse.getResponseCode() == RESTMethod.ResponseCode.OK) {
            return true;
        }
        syncResult.stats.numIoExceptions++;
        return false;
    }
}
