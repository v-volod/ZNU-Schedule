package ua.zp.rozklad.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import ua.zp.rozklad.app.provider.ScheduleContract.AcademicHour;
import ua.zp.rozklad.app.provider.ScheduleContract.Audience;
import ua.zp.rozklad.app.provider.ScheduleContract.Campus;
import ua.zp.rozklad.app.provider.ScheduleContract.ClassType;
import ua.zp.rozklad.app.provider.ScheduleContract.Department;
import ua.zp.rozklad.app.provider.ScheduleContract.Group;
import ua.zp.rozklad.app.provider.ScheduleContract.Lecturer;
import ua.zp.rozklad.app.provider.ScheduleContract.Schedule;
import ua.zp.rozklad.app.provider.ScheduleContract.Subject;
import ua.zp.rozklad.app.provider.ScheduleDatabase.Tables;

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
        int CLASS_TYPE = 800;
        int CLASS_TYPE_ID = 801;
        int SCHEDULE = 900;
        int SCHEDULE_ID = 901;
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

        matcher.addURI(authority, "class_type", URI_CODE.CLASS_TYPE);
        matcher.addURI(authority, "class_type/#", URI_CODE.CLASS_TYPE_ID);

        matcher.addURI(authority, "schedule", URI_CODE.SCHEDULE);
        matcher.addURI(authority, "schedule/#", URI_CODE.SCHEDULE_ID);

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
            case URI_CODE.CLASS_TYPE:
                return ClassType.CONTENT_TYPE;
            case URI_CODE.CLASS_TYPE_ID:
                return ClassType.CONTENT_ITEM_TYPE;
            case URI_CODE.SCHEDULE:
                return Schedule.CONTENT_TYPE;
            case URI_CODE.SCHEDULE_ID:
                return Schedule.CONTENT_ITEM_TYPE;
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
        long id;

        switch (match) {
            case URI_CODE.DEPARTMENT:
                id = db.insertOrThrow(Tables.DEPARTMENT, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.GROUP:
                id = db.insertOrThrow(Tables.GROUP, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.LECTURER:
                id = db.insertOrThrow(Tables.LECTURER, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.SUBJECT:
                id = db.insertOrThrow(Tables.SUBJECT, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.ACADEMIC_HOUR:
                id = db.insertOrThrow(Tables.ACADEMIC_HOUR, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.CAMPUS:
                id = db.insertOrThrow(Tables.CAMPUS, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.AUDIENCE:
                id = db.insertOrThrow(Tables.AUDIENCE, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.CLASS_TYPE:
                id = db.insertOrThrow(Tables.CLASS_TYPE, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            case URI_CODE.SCHEDULE:
                id = db.insertOrThrow(Tables.SCHEDULE, null, values);
                notifyChange(uri);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deleteCount;
        String where;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.DEPARTMENT:
                deleteCount = db.delete(Tables.DEPARTMENT, selection, selectionArgs);
                break;
            case URI_CODE.DEPARTMENT_ID:
                where = Department._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.DEPARTMENT, where, selectionArgs);
                break;
            case URI_CODE.GROUP:
                deleteCount = db.delete(Tables.GROUP, selection, selectionArgs);
                break;
            case URI_CODE.GROUP_ID:
                where = Group._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.GROUP, where, selectionArgs);
                break;
            case URI_CODE.LECTURER:
                deleteCount = db.delete(Tables.LECTURER, selection, selectionArgs);
                break;
            case URI_CODE.LECTURER_ID:
                where = Lecturer._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.LECTURER, where, selectionArgs);
                break;
            case URI_CODE.SUBJECT:
                deleteCount = db.delete(Tables.SUBJECT, selection, selectionArgs);
                break;
            case URI_CODE.SUBJECT_ID:
                where = Subject._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.SUBJECT, where, selectionArgs);
                break;
            case URI_CODE.ACADEMIC_HOUR:
                deleteCount = db.delete(Tables.ACADEMIC_HOUR, selection, selectionArgs);
                break;
            case URI_CODE.ACADEMIC_HOUR_ID:
                where = AcademicHour._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.ACADEMIC_HOUR, where, selectionArgs);
                break;
            case URI_CODE.CAMPUS:
                deleteCount = db.delete(Tables.CAMPUS, selection, selectionArgs);
                break;
            case URI_CODE.CAMPUS_ID:
                where = Campus._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.CAMPUS, where, selectionArgs);
                break;
            case URI_CODE.AUDIENCE:
                deleteCount = db.delete(Tables.AUDIENCE, selection, selectionArgs);
                break;
            case URI_CODE.AUDIENCE_ID:
                where = Audience._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.AUDIENCE, where, selectionArgs);
                break;
            case URI_CODE.CLASS_TYPE:
                deleteCount = db.delete(Tables.CLASS_TYPE, selection, selectionArgs);
                break;
            case URI_CODE.CLASS_TYPE_ID:
                where = ClassType._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.CLASS_TYPE, where, selectionArgs);
                break;
            case URI_CODE.SCHEDULE:
                deleteCount = db.delete(Tables.SCHEDULE, selection, selectionArgs);
                break;
            case URI_CODE.SCHEDULE_ID:
                where = Schedule._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(Tables.SCHEDULE, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        notifyChange(uri);

        return deleteCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updateCount;
        String where;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.DEPARTMENT:
                updateCount = db.update(Tables.DEPARTMENT, values, selection, selectionArgs);
                break;
            case URI_CODE.DEPARTMENT_ID:
                where = Department._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.DEPARTMENT, values, where, selectionArgs);
                break;
            case URI_CODE.GROUP:
                updateCount = db.update(Tables.GROUP, values, selection, selectionArgs);
                break;
            case URI_CODE.GROUP_ID:
                where = Group._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.GROUP, values, where, selectionArgs);
                break;
            case URI_CODE.LECTURER:
                updateCount = db.update(Tables.LECTURER, values, selection, selectionArgs);
                break;
            case URI_CODE.LECTURER_ID:
                where = Lecturer._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.LECTURER, values, where, selectionArgs);
                break;
            case URI_CODE.SUBJECT:
                updateCount = db.update(Tables.SUBJECT, values, selection, selectionArgs);
                break;
            case URI_CODE.SUBJECT_ID:
                where = Subject._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.SUBJECT, values, where, selectionArgs);
                break;
            case URI_CODE.ACADEMIC_HOUR:
                updateCount = db.update(Tables.ACADEMIC_HOUR, values, selection, selectionArgs);
                break;
            case URI_CODE.ACADEMIC_HOUR_ID:
                where = AcademicHour._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.ACADEMIC_HOUR, values, where, selectionArgs);
                break;
            case URI_CODE.CAMPUS:
                updateCount = db.update(Tables.CAMPUS, values, selection, selectionArgs);
                break;
            case URI_CODE.CAMPUS_ID:
                where = Campus._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.CAMPUS, values, where, selectionArgs);
                break;
            case URI_CODE.AUDIENCE:
                updateCount = db.update(Tables.AUDIENCE, values, selection, selectionArgs);
                break;
            case URI_CODE.AUDIENCE_ID:
                where = Audience._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.AUDIENCE, values, where, selectionArgs);
                break;
            case URI_CODE.CLASS_TYPE:
                updateCount = db.update(Tables.CLASS_TYPE, values, selection, selectionArgs);
                break;
            case URI_CODE.CLASS_TYPE_ID:
                where = ClassType._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.CLASS_TYPE, values, where, selectionArgs);
                break;
            case URI_CODE.SCHEDULE:
                updateCount = db.update(Tables.SCHEDULE, values, selection, selectionArgs);
                break;
            case URI_CODE.SCHEDULE_ID:
                where = Schedule._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Tables.SCHEDULE, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        notifyChange(uri);

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
            case URI_CODE.CLASS_TYPE:
                queryBuilder.setTables(Tables.CLASS_TYPE);
                break;
            case URI_CODE.CLASS_TYPE_ID:
                queryBuilder.setTables(Tables.CLASS_TYPE);
                queryBuilder.appendWhere(ClassType._ID + " = " + uri.getLastPathSegment());
                break;
            case URI_CODE.SCHEDULE:
                queryBuilder.setTables(Tables.SCHEDULE);
                break;
            case URI_CODE.SCHEDULE_ID:
                queryBuilder.setTables(Tables.SCHEDULE);
                queryBuilder.appendWhere(Schedule._ID + " = " + uri.getLastPathSegment());
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
        getContext().getContentResolver().notifyChange(uri, null);
    }

}
