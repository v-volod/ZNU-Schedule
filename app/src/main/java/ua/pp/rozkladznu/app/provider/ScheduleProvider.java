package ua.pp.rozkladznu.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import ua.pp.rozkladznu.app.provider.ScheduleContract.AcademicHour;
import ua.pp.rozkladznu.app.provider.ScheduleContract.Audience;
import ua.pp.rozkladznu.app.provider.ScheduleContract.Campus;
import ua.pp.rozkladznu.app.provider.ScheduleContract.Department;
import ua.pp.rozkladznu.app.provider.ScheduleContract.FullLecturer;
import ua.pp.rozkladznu.app.provider.ScheduleContract.FullSchedule;
import ua.pp.rozkladznu.app.provider.ScheduleContract.FullSubject;
import ua.pp.rozkladznu.app.provider.ScheduleContract.Group;
import ua.pp.rozkladznu.app.provider.ScheduleContract.Lecturer;
import ua.pp.rozkladznu.app.provider.ScheduleContract.Schedule;
import ua.pp.rozkladznu.app.provider.ScheduleContract.Subject;
import ua.pp.rozkladznu.app.provider.ScheduleDatabase.Tables;

/**
 * Provider that stores {@link ScheduleContract} data.
 *
 * @author Vojko Vladimir
 */
public class ScheduleProvider extends ContentProvider {

    private ScheduleDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private interface URI_CODE {
        int DEPARTMENT = 100;
        int DEPARTMENT_ID = 101;
        int GROUP = 200;
        int GROUP_ID = 201;
        int LECTURER = 300;
        int LECTURER_ID = 301;
        int SUBJECT = 400;
        int SUBJECT_ID = 401;
        int ACADEMIC_HOUR = 500;
        int ACADEMIC_HOUR_ID = 501;
        int CAMPUS = 600;
        int CAMPUS_ID = 601;
        int AUDIENCE = 700;
        int AUDIENCE_ID = 701;
        int SCHEDULE = 900;
        int SCHEDULE_ID = 901;
        int FULL_SCHEDULE = 902;
        int FULL_SCHEDULE_ID = 903;
        int FULL_SUBJECT = 1000;
        int FULL_SUBJECT_ID = 1001;
        int FULL_LECTURER = 1101;
        int FULL_LECTURER_ID = 1102;
    }

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ScheduleContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "department", URI_CODE.DEPARTMENT);
        matcher.addURI(authority, "department/#", URI_CODE.DEPARTMENT_ID);

        matcher.addURI(authority, "group", URI_CODE.GROUP);
        matcher.addURI(authority, "group/#", URI_CODE.GROUP_ID);

        matcher.addURI(authority, "lecturer", URI_CODE.LECTURER);
        matcher.addURI(authority, "lecturer/#", URI_CODE.LECTURER_ID);

        matcher.addURI(authority, "subject", URI_CODE.SUBJECT);
        matcher.addURI(authority, "subject/#", URI_CODE.SUBJECT_ID);

        matcher.addURI(authority, "academic_hour", URI_CODE.ACADEMIC_HOUR);
        matcher.addURI(authority, "academic_hour/#", URI_CODE.ACADEMIC_HOUR_ID);

        matcher.addURI(authority, "campus", URI_CODE.CAMPUS);
        matcher.addURI(authority, "campus/#", URI_CODE.CAMPUS_ID);

        matcher.addURI(authority, "audience", URI_CODE.AUDIENCE);
        matcher.addURI(authority, "audience/#", URI_CODE.AUDIENCE_ID);

        matcher.addURI(authority, "schedule", URI_CODE.SCHEDULE);
        matcher.addURI(authority, "schedule/#", URI_CODE.SCHEDULE_ID);

        matcher.addURI(authority, "full_schedule/", URI_CODE.FULL_SCHEDULE);
        matcher.addURI(authority, "full_schedule/#", URI_CODE.FULL_SCHEDULE_ID);

        matcher.addURI(authority, "full_subject/", URI_CODE.FULL_SUBJECT);
        matcher.addURI(authority, "full_subject/#", URI_CODE.FULL_SUBJECT_ID);

        matcher.addURI(authority, "full_lecturer/", URI_CODE.FULL_LECTURER);
        matcher.addURI(authority, "full_lecturer/#", URI_CODE.FULL_LECTURER_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScheduleDatabase(getContext());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_CODE.DEPARTMENT:
                return Department.CONTENT_TYPE;
            case URI_CODE.DEPARTMENT_ID:
                return Department.CONTENT_ITEM_TYPE;
            case URI_CODE.GROUP:
                return Group.CONTENT_TYPE;
            case URI_CODE.GROUP_ID:
                return Group.CONTENT_ITEM_TYPE;
            case URI_CODE.LECTURER:
                return Lecturer.CONTENT_TYPE;
            case URI_CODE.LECTURER_ID:
                return Lecturer.CONTENT_ITEM_TYPE;
            case URI_CODE.SUBJECT:
                return Subject.CONTENT_TYPE;
            case URI_CODE.SUBJECT_ID:
                return Subject.CONTENT_ITEM_TYPE;
            case URI_CODE.ACADEMIC_HOUR:
                return AcademicHour.CONTENT_TYPE;
            case URI_CODE.ACADEMIC_HOUR_ID:
                return AcademicHour.CONTENT_ITEM_TYPE;
            case URI_CODE.CAMPUS:
                return Campus.CONTENT_TYPE;
            case URI_CODE.CAMPUS_ID:
                return Campus.CONTENT_ITEM_TYPE;
            case URI_CODE.AUDIENCE:
                return Audience.CONTENT_TYPE;
            case URI_CODE.AUDIENCE_ID:
                return Audience.CONTENT_ITEM_TYPE;
            case URI_CODE.SCHEDULE:
                return Schedule.CONTENT_TYPE;
            case URI_CODE.SCHEDULE_ID:
                return Schedule.CONTENT_ITEM_TYPE;
            case URI_CODE.FULL_SCHEDULE:
                return FullSchedule.CONTENT_TYPE;
            case URI_CODE.FULL_SCHEDULE_ID:
                return FullSchedule.CONTENT_ITEM_TYPE;
            case URI_CODE.FULL_SUBJECT:
                return FullSubject.CONTENT_TYPE;
            case URI_CODE.FULL_SUBJECT_ID:
                return FullSubject.CONTENT_ITEM_TYPE;
            case URI_CODE.FULL_LECTURER:
                return FullLecturer.CONTENT_TYPE;
            case URI_CODE.FULL_LECTURER_ID:
                return FullLecturer.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String table;
        long id;

        switch (match) {
            case URI_CODE.DEPARTMENT:
                table = Tables.DEPARTMENT;
                break;
            case URI_CODE.GROUP:
                table = Tables.GROUP;
                break;
            case URI_CODE.LECTURER:
                table = Tables.LECTURER;
                break;
            case URI_CODE.SUBJECT:
                table = Tables.SUBJECT;
                break;
            case URI_CODE.ACADEMIC_HOUR:
                table = Tables.ACADEMIC_HOUR;
                break;
            case URI_CODE.CAMPUS:
                table = Tables.CAMPUS;
                break;
            case URI_CODE.AUDIENCE:
                table = Tables.AUDIENCE;
                break;
            case URI_CODE.SCHEDULE:
                table = Tables.SCHEDULE;
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        id = db.insertOrThrow(table, null, values);

        notifyChange(uri, match);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deleteCount;
        String table;
        String where = null;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.DEPARTMENT:
                table = Tables.DEPARTMENT;
                break;
            case URI_CODE.DEPARTMENT_ID:
                table = Tables.DEPARTMENT;
                where = Department._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.GROUP:
                table = Tables.GROUP;
                break;
            case URI_CODE.GROUP_ID:
                table = Tables.GROUP;
                where = Group._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.LECTURER:
                table = Tables.LECTURER;
                break;
            case URI_CODE.LECTURER_ID:
                table = Tables.LECTURER;
                where = Lecturer._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.SUBJECT:
                table = Tables.SUBJECT;
                break;
            case URI_CODE.SUBJECT_ID:
                table = Tables.SUBJECT;
                where = Subject._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.ACADEMIC_HOUR:
                table = Tables.ACADEMIC_HOUR;
                break;
            case URI_CODE.ACADEMIC_HOUR_ID:
                table = Tables.ACADEMIC_HOUR;
                where = AcademicHour._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.CAMPUS:
                table = Tables.CAMPUS;
                break;
            case URI_CODE.CAMPUS_ID:
                table = Tables.CAMPUS;
                where = Campus._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.AUDIENCE:
                table = Tables.AUDIENCE;
                break;
            case URI_CODE.AUDIENCE_ID:
                table = Tables.AUDIENCE;
                where = Audience._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.SCHEDULE:
                table = Tables.SCHEDULE;
                break;
            case URI_CODE.SCHEDULE_ID:
                table = Tables.SCHEDULE;
                where = Schedule._ID + " = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        deleteCount = db.delete(table, appendSelection(where, selection), selectionArgs);

        notifyChange(uri, match);

        return deleteCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updateCount;
        String table;
        String where = null;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.DEPARTMENT:
                table = Tables.DEPARTMENT;
                break;
            case URI_CODE.DEPARTMENT_ID:
                table = Tables.DEPARTMENT;
                where = Department._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.GROUP:
                table = Tables.GROUP;
                break;
            case URI_CODE.GROUP_ID:
                table = Tables.GROUP;
                where = Group._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.LECTURER:
                table = Tables.LECTURER;
                break;
            case URI_CODE.LECTURER_ID:
                table = Tables.LECTURER;
                where = Lecturer._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.SUBJECT:
                table = Tables.SUBJECT;
                break;
            case URI_CODE.SUBJECT_ID:
                table = Tables.SUBJECT;
                where = Subject._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.ACADEMIC_HOUR:
                table = Tables.ACADEMIC_HOUR;
                break;
            case URI_CODE.ACADEMIC_HOUR_ID:
                table = Tables.ACADEMIC_HOUR;
                where = AcademicHour._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.CAMPUS:
                table = Tables.CAMPUS;
                break;
            case URI_CODE.CAMPUS_ID:
                table = Tables.CAMPUS;
                where = Campus._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.AUDIENCE:
                table = Tables.AUDIENCE;
                break;
            case URI_CODE.AUDIENCE_ID:
                table = Tables.AUDIENCE;
                where = Audience._ID + " = " + uri.getLastPathSegment();
                break;
            case URI_CODE.SCHEDULE:
                table = Tables.SCHEDULE;
                break;
            case URI_CODE.SCHEDULE_ID:
                table = Tables.SCHEDULE;
                where = Schedule.SCHEDULE_ID + " = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        updateCount = db.update(table, values, appendSelection(where, selection), selectionArgs);

        notifyChange(uri, match);

        return updateCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.DEPARTMENT:
                queryBuilder.setTables(Tables.DEPARTMENT);
                break;
            case URI_CODE.DEPARTMENT_ID:
                queryBuilder.setTables(Tables.DEPARTMENT);
                queryBuilder.appendWhere(Department._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.GROUP:
                queryBuilder.setTables(Tables.GROUP);
                break;
            case URI_CODE.GROUP_ID:
                queryBuilder.setTables(Tables.GROUP);
                queryBuilder.appendWhere(Group._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.LECTURER:
                queryBuilder.setTables(Tables.LECTURER);
                break;
            case URI_CODE.LECTURER_ID:
                queryBuilder.setTables(Tables.LECTURER);
                queryBuilder.appendWhere(Lecturer._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.SUBJECT:
                queryBuilder.setTables(Tables.SUBJECT);
                break;
            case URI_CODE.SUBJECT_ID:
                queryBuilder.setTables(Tables.SUBJECT);
                queryBuilder.appendWhere(Subject._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.ACADEMIC_HOUR:
                queryBuilder.setTables(Tables.ACADEMIC_HOUR);
                break;
            case URI_CODE.ACADEMIC_HOUR_ID:
                queryBuilder.setTables(Tables.ACADEMIC_HOUR);
                queryBuilder.appendWhere(AcademicHour._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.CAMPUS:
                queryBuilder.setTables(Tables.CAMPUS);
                break;
            case URI_CODE.CAMPUS_ID:
                queryBuilder.setTables(Tables.CAMPUS);
                queryBuilder.appendWhere(Campus._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.AUDIENCE:
                queryBuilder.setTables(Tables.AUDIENCE);
                break;
            case URI_CODE.AUDIENCE_ID:
                queryBuilder.setTables(Tables.AUDIENCE);
                queryBuilder.appendWhere(Audience._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.SCHEDULE:
                queryBuilder.setTables(Tables.SCHEDULE);
                break;
            case URI_CODE.SCHEDULE_ID:
                queryBuilder.setTables(Tables.SCHEDULE);
                queryBuilder.appendWhere(Schedule.SCHEDULE_ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.FULL_SCHEDULE:
                queryBuilder.setTables(FullSchedule.Summary.TABLES);
                break;
            case URI_CODE.FULL_SCHEDULE_ID:
                queryBuilder.setTables(FullSchedule.Summary.TABLES);
                queryBuilder.appendWhere(FullSchedule._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.FULL_SUBJECT:
                queryBuilder.setTables(FullSubject.TABLES);
                break;
            case URI_CODE.FULL_SUBJECT_ID:
                queryBuilder.setTables(FullSubject.TABLES);
                queryBuilder.appendWhere(FullSubject._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.FULL_LECTURER:
                queryBuilder.setTables(FullLecturer.TABLES);
                /*
                * Ignore "Empty" Lecturer which used in schedule items with undefined lecturer.
                * */
                queryBuilder.appendWhere(FullLecturer.LECTURER_NAME + "!=''");
                break;
            case URI_CODE.FULL_LECTURER_ID:
                queryBuilder.setTables(FullLecturer.TABLES);
                queryBuilder.appendWhere(FullLecturer._ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        Cursor cursor = queryBuilder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private void notifyChange(Uri uri) {
        notifyChange(uri, false);
    }

    private void notifyChange(Uri uri, boolean syncToNetwork) {
        getContext().getContentResolver().notifyChange(uri, null, syncToNetwork);
    }

    private void notifyChange(Uri uri, int match) {
        notifyChange(uri);
        switch (match) {
            case URI_CODE.SCHEDULE:
                notifyChange(FullSchedule.CONTENT_URI);
                break;
            case URI_CODE.SUBJECT:
                notifyChange(FullSchedule.CONTENT_URI);
                notifyChange(FullSubject.CONTENT_URI);
                break;
            case URI_CODE.ACADEMIC_HOUR:
                notifyChange(FullSchedule.CONTENT_URI);
                break;
            case URI_CODE.LECTURER:
                notifyChange(FullSchedule.CONTENT_URI);
                notifyChange(FullLecturer.CONTENT_URI);
                break;
            case URI_CODE.AUDIENCE:
            case URI_CODE.CAMPUS:
                notifyChange(FullSchedule.CONTENT_URI);
                break;
        }
    }

    private String appendSelection(String where, String selection) {
        if (TextUtils.isEmpty(where)) {
            return selection;
        } else {
            if (!TextUtils.isEmpty(selection)) {
                where += " AND " + selection;
            }
            return where;
        }
    }
}
