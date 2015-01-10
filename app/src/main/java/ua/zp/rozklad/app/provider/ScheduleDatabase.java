package ua.zp.rozklad.app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import ua.zp.rozklad.app.provider.ScheduleContract.AcademicHourColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.AudienceColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.CampusColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.ClassTypeColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.Department;
import ua.zp.rozklad.app.provider.ScheduleContract.GroupColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.LecturerColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.ScheduleColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.SubjectColumns;
import ua.zp.rozklad.app.provider.ScheduleContract.SyncColumns;

/**
 * @author Vojko Vladimir
 */
public class ScheduleDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schedule.db";
    private static final int DEBUG_VERSION = 1;
    private static final int CUR_DATABASE_VERSION = DEBUG_VERSION;

    interface Tables {
        String DEPARTMENT = "department";
        String GROUP = "`group`";
        String LECTURER = "lecturer";
        String SUBJECT = "subject";
        String ACADEMIC_HOUR = "academic_hour";
        String CAMPUS = "campus";
        String AUDIENCE = "audience";
        String CLASS_TYPE = "class_type";
        String SCHEDULE = "schedule";
    }

    public ScheduleDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.DEPARTMENT + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Department.DEPARTMENT_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.GROUP + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GroupColumns.DEPARTMENT_ID + " INTEGER NOT NULL, "
                + GroupColumns.GROUP_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.LECTURER + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LecturerColumns.LECTURER_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.SUBJECT + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SubjectColumns.SUBJECT_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.ACADEMIC_HOUR + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AcademicHourColumns.NUM + " INTEGER NOT NULL, "
                + AcademicHourColumns.START_TIME + " INTEGER NOT NULL, "
                + AcademicHourColumns.END_TIME + " INTEGER NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.CAMPUS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CampusColumns.CAMPUS_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.AUDIENCE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AudienceColumns.CAMPUS_ID + " INTEGER NOT NULL, "
                + AudienceColumns.AUDIENCE_NUMBER + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.CLASS_TYPE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ClassTypeColumns.CLASS_TYPE_NAME + " TEXT NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + Tables.SCHEDULE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ScheduleColumns.GROUP_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.SUBJECT_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.DAY_OF_WEEK + " INTEGER NOT NULL, "
                + ScheduleColumns.ACADEMIC_HOUR_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.LECTURER_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.AUDIENCE_ID + " INTEGER NOT NULL, "
                + ScheduleColumns.PERIODICITY + " INTEGER NOT NULL, "
                + ScheduleColumns.START_DATE + " INTEGER NOT NULL, "
                + ScheduleColumns.END_DATE + " INTEGER NOT NULL, "
                + ScheduleColumns.CLASS_TYPE_ID + " INTEGER NOT NULL, "
                + SyncColumns.UPDATED + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
