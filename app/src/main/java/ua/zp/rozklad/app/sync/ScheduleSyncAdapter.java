package ua.zp.rozklad.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

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
import ua.zp.rozklad.app.processor.dependency.LecturerDependency;
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
import ua.zp.rozklad.app.util.PreferencesUtils;

import static ua.zp.rozklad.app.provider.ScheduleContract.Schedule;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineArgs;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineProjection;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;
import static ua.zp.rozklad.app.provider.ScheduleContract.groupBySelection;

/**
 * @author Vojko Vladimir
 */
public class ScheduleSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ACTION_SYNC_STATUS = App.TAG + ".ACTION_SYNC_STATUS";
    public static final String EXTRA_SYNC_STATUS = "SYNC_STATUS";
    public interface SyncStatus {
        int START = 0;
        int FINISHED = 1;
        int ABORTED_WITH_ERROR = 2;
    }
    private static final Intent SYNC_STATUS_BROADCAST = new Intent(ACTION_SYNC_STATUS);

    private GroupsProcessor groupsProcessor;
    private ScheduleProcessor scheduleProcessor;
    private LecturersProcessor lecturersProcessor;
    private SubjectsProcessor subjectsProcessor;
    private AudiencesProcessor audiencesProcessor;
    private CampusesProcessor campusesProcessor;
    private AcademicHoursProcessor academicHoursProcessor;

    public ScheduleSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        boolean isSyncRequestManual = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);

        if (isSyncRequestManual) {
            App.getInstance().setManualSyncActive(true);
            SYNC_STATUS_BROADCAST.putExtra(EXTRA_SYNC_STATUS, SyncStatus.START);
            getContext().sendBroadcast(SYNC_STATUS_BROADCAST);
        }

        PreferencesUtils mPrefUtils = App.getInstance().getPreferencesUtils();
        AccountManager manager = AccountManager.get(getContext());
        String groupId = manager.getUserData(account, GroupAuthenticator.KEY_GROUP_ID);

        GetCurrentWeekMethod currentWeekMethod = new GetCurrentWeekMethod();
        currentWeekMethod.prepare(RESTMethod.Filter.NONE);

        MethodResponse<CurrentWeek> currentWeekMethodResponse = currentWeekMethod.executeBlocking();
        if (canProcess(currentWeekMethodResponse, syncResult)) {
            CurrentWeek currentWeek = currentWeekMethodResponse.getResponse();
            mPrefUtils.savePeriodicity(currentWeek.getWeek(), currentWeek.getWeekOfYear());
        } else if (!mPrefUtils.getPeriodicity().isValid()) {
            if (isSyncRequestManual) {
                App.getInstance().setManualSyncActive(false);
                SYNC_STATUS_BROADCAST.putExtra(EXTRA_SYNC_STATUS, SyncStatus.ABORTED_WITH_ERROR);
                getContext().sendBroadcast(SYNC_STATUS_BROADCAST);
            }
            return;
        }

        groupsProcessor = new GroupsProcessor(getContext());
        scheduleProcessor = new ScheduleProcessor(getContext());
        lecturersProcessor = new LecturersProcessor(getContext());
        subjectsProcessor = new SubjectsProcessor(getContext());
        audiencesProcessor = new AudiencesProcessor(getContext());
        campusesProcessor = new CampusesProcessor(getContext());
        academicHoursProcessor = new AcademicHoursProcessor(getContext());

        groupsProcessor.clean();

        GetGroupsMethod getGroups = new GetGroupsMethod();
        getGroups.prepare(RESTMethod.Filter.BY_ID, groupId);

        MethodResponse<ArrayList<Group>> groupsResponse = getGroups.executeBlocking();

        if (canProcess(groupsResponse, syncResult) && groupsResponse.getResponse().size() == 1) {
            ArrayList<Group> groups = groupsProcessor
                    .resolveDependencies(groupsResponse.getResponse());

            if (groups.size() == 1) {
                if (performGroupScheduleSync(syncResult, groups.get(0))) {
                    groupsProcessor.process(groupsResponse.getResponse());
                }
            } else {
                Cursor cursor = getContext().getContentResolver()
                        .query(Schedule.CONTENT_URI,
                                combineProjection(Schedule.LECTURER_ID),
                                combineSelection(Schedule.GROUP_ID + "=?") +
                                        groupBySelection(Schedule.LECTURER_ID),
                                combineArgs(groupId),
                                null);

                if (cursor.moveToFirst()) {
                    String[] lecturersIds = new String[cursor.getCount()];

                    do {
                        lecturersIds[cursor.getPosition()] = cursor.getString(0);
                    } while (cursor.moveToNext());

                    GetLecturersMethod getLecturers = new GetLecturersMethod();
                    getLecturers.prepare(RESTMethod.Filter.BY_ID_IN, lecturersIds);

                    MethodResponse<ArrayList<Lecturer>> lecturersResponse =
                            getLecturers.executeBlocking();

                    if (canProcess(lecturersResponse, syncResult)) {
                        performLecturersSync(syncResult, lecturersResponse.getResponse(), true);
                    }
                }

                cursor.close();
            }
        }

        lecturersProcessor.clean();
        subjectsProcessor.clean();
        audiencesProcessor.clean();
        campusesProcessor.clean();
        academicHoursProcessor.clean();

        if (isSyncRequestManual) {
            App.getInstance().setManualSyncActive(false);
            SYNC_STATUS_BROADCAST.putExtra(EXTRA_SYNC_STATUS, SyncStatus.FINISHED);
            getContext().sendBroadcast(SYNC_STATUS_BROADCAST);
        }
    }

    private boolean performGroupScheduleSync(SyncResult syncResult, Group group) {
        GetScheduleMethod method = new GetScheduleMethod();
        method.prepare(RESTMethod.Filter.BY_GROUP_ID, String.valueOf(group.getId()));

        MethodResponse<ArrayList<GlobalScheduleItem>> scheduleItemsResponse =
                method.executeBlocking();

        if (canProcess(scheduleItemsResponse, syncResult)) {
            ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();

            for (GlobalScheduleItem item : scheduleItemsResponse.getResponse()) {
                scheduleItems.addAll(item.getScheduleItems());
            }

            return performScheduleSync(syncResult, scheduleItems, true);
        }

        return false;
    }

    private boolean performScheduleSync(SyncResult syncResult, ArrayList<ScheduleItem> scheduleItems,
                                        boolean syncLecturerSchedule) {
        boolean audiencesSynced = true;
        boolean subjectsSynced = true;
        boolean academicHoursSynced = true;
        boolean lecturersSynced = true;

        if (syncLecturerSchedule) {
            scheduleProcessor.cleanGroupSchedule(scheduleItems);
        } else {
            scheduleProcessor.cleanLecturerSchedule(scheduleItems);
        }

        ScheduleDependency dependency = scheduleProcessor.resolveDependencies(scheduleItems);

        if (dependency.hasAudiences()) {
            audiencesSynced = performAudiencesSync(syncResult, dependency.getAudiences());
        }

        if (dependency.hasSubjects()) {
            subjectsSynced = performSubjectsSync(syncResult, dependency.getSubjects());
        }

        if (dependency.hasAcademicHours()) {
            academicHoursSynced =
                    performAcademicHoursSync(syncResult, dependency.getAcademicHours());
        }

        if (dependency.hasLecturers()) {
            lecturersSynced = performLecturersSync(syncResult, dependency.getLecturers(),
                    syncLecturerSchedule);
        }

        if (dependency.hasGroups()) {
            performGroupsSync(syncResult, dependency.getGroups());
        }

        if (audiencesSynced && subjectsSynced && academicHoursSynced && lecturersSynced) {
            scheduleProcessor.process(scheduleItems);
            return true;
        }

        return false;
    }

    private void performGroupsSync(SyncResult syncResult, String[] groups) {
        GetGroupsMethod getGroups = new GetGroupsMethod();
        getGroups.prepare(RESTMethod.Filter.BY_ID_IN, groups);

        MethodResponse<ArrayList<Group>> groupsResponse = getGroups.executeBlocking();

        if (canProcess(groupsResponse, syncResult)) {
            groupsProcessor.process(groupsResponse.getResponse());
        }
    }

    private boolean performAudiencesSync(SyncResult syncResult, String[] audiencesIds) {
        GetAudiencesMethod method = new GetAudiencesMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, audiencesIds);

        MethodResponse<ArrayList<Audience>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            boolean successful = true;

            AudienceDependency dependency = audiencesProcessor.resolveDependencies(response.getResponse());

            if (dependency.hasCampuses()) {
                successful = performCampusesSync(syncResult, dependency.getCampuses());
            }

            if (successful) {
                audiencesProcessor.process(response.getResponse());
                return true;
            }
        }

        return false;
    }

    private boolean performCampusesSync(SyncResult syncResult, String[] campusesIds) {
        GetCampusesMethod method = new GetCampusesMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, campusesIds);

        MethodResponse<ArrayList<Campus>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            campusesProcessor.process(response.getResponse());
            return true;
        }

        return false;
    }

    private boolean performSubjectsSync(SyncResult syncResult, String[] subjectsIds) {
        GetSubjectsMethod method = new GetSubjectsMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, subjectsIds);

        MethodResponse<ArrayList<Subject>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            subjectsProcessor.process(response.getResponse());
            return true;
        }

        return false;
    }

    private boolean performAcademicHoursSync(SyncResult syncResult, String[] academicHoursIds) {
        GetAcademicHoursMethod method = new GetAcademicHoursMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, academicHoursIds);

        MethodResponse<ArrayList<AcademicHour>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            academicHoursProcessor.process(response.getResponse());
            return true;
        }

        return false;
    }

    private boolean performLecturersSync(SyncResult syncResult, String[] lecturersIds,
                                         boolean syncSchedule) {
        GetLecturersMethod method = new GetLecturersMethod();
        method.prepare(RESTMethod.Filter.BY_ID_IN, lecturersIds);

        MethodResponse<ArrayList<Lecturer>> response = method.executeBlocking();

        if (canProcess(response, syncResult)) {
            return performLecturersSync(syncResult, response.getResponse(), syncSchedule);
        }

        return false;
    }

    private boolean performLecturersSync(SyncResult syncResult, ArrayList<Lecturer> lecturers,
                                         boolean syncSchedule) {
        boolean successful = true;

        if (syncSchedule) {
            LecturerDependency dependency =
                    lecturersProcessor.resolveDependencies(lecturers);

            if (dependency.hasLecturers()) {
                for (String id : dependency.getLecturers()) {
                    successful &= performLecturerScheduleSync(syncResult, id);
                }
            }
        }

        if (successful) {
            lecturersProcessor.process(lecturers);
        }

        return successful;
    }

    private boolean performLecturerScheduleSync(SyncResult syncResult, String lecturerId) {
        GetScheduleMethod method = new GetScheduleMethod();
        method.prepare(RESTMethod.Filter.BY_LECTURER_ID, lecturerId);

        MethodResponse<ArrayList<GlobalScheduleItem>> scheduleItemsResponse =
                method.executeBlocking();

        if (canProcess(scheduleItemsResponse, syncResult)) {
            ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();

            for (GlobalScheduleItem item : scheduleItemsResponse.getResponse()) {
                scheduleItems.addAll(item.getScheduleItems());
            }

            return performScheduleSync(syncResult, scheduleItems, false);
        }

        return false;
    }

    private boolean canProcess(MethodResponse methodResponse, SyncResult syncResult) {
        if (methodResponse.getResponseCode() == RESTMethod.ResponseCode.OK) {
            return true;
        }

        syncResult.stats.numIoExceptions++;

        return false;
    }
}
