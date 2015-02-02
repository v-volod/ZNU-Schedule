package ua.zp.rozklad.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static ua.zp.rozklad.app.provider.ScheduleDatabase.Tables;

/**
 * Contract class for interacting with {@link ScheduleProvider}.
 *
 * @author Vojko Vladimir
 */
public class ScheduleContract {

    public interface SyncColumns {
        /**
         * Last time this entry was updated or synchronized.
         */
        String UPDATED = "updated";
    }

    interface LecturerColumns {
        String LECTURER_NAME = "lecturer_name";
    }

    interface SubjectColumns {
        String SUBJECT_NAME = "subject_name";
    }

    interface AcademicHourColumns {
        String NUM = "num";
        String START_TIME = "start_time";
        String END_TIME = "end_time";
    }

    interface CampusColumns {
        String CAMPUS_NAME = "campus_name";
    }

    interface AudienceColumns {
        String CAMPUS_ID = "campus_id";
        String AUDIENCE_NUMBER = "audience_number";
    }

    interface ScheduleColumns {
        String GROUP_ID = "group_id";
        String SUBGROUP = "subgroup";
        String SUBJECT_ID = "subject_id";
        String DAY_OF_WEEK = "day_of_week";
        String ACADEMIC_HOUR_ID = "academic_hour_id";
        String LECTURER_ID = "lecturer_id";
        String AUDIENCE_ID = "audience_id ";
        String PERIODICITY = "periodicity";
        String START_DATE = "start_date";
        String END_DATE = "end_date";
        String CLASS_TYPE = "class_type";
    }

    public static final String CONTENT_AUTHORITY = "ua.zp.rozklad.app.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_LECTURER = "lecturer";
    private static final String PATH_SUBJECT = "subject";
    private static final String PATH_ACADEMIC_HOUR = "academic_hour";
    private static final String PATH_CAMPUS = "campus";
    private static final String PATH_AUDIENCE = "audience";
    private static final String PATH_SCHEDULE = "schedule";
    private static final String PATH_FULL_SCHEDULE = "full_schedule";

    private static final String INNER_JOIN = " INNER JOIN ";
    private static final String ON = " ON ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String EQ = " = ";
    private static final String ASC = " ASC";
    private static final String DESC = " DESC";

    public static class Lecturer implements LecturerColumns, BaseColumns, SyncColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LECTURER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".lecturer";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".lecturer";

        public static interface SUMMARY {
            String[] PROJECTION = {
                    _ID,
                    LECTURER_NAME,
                    UPDATED
            };

            interface COLUMN {
                int _ID = 0;
                int LECTURER_NAME = 1;
                int UPDATED = 2;
            }
        }

        /**
         * Build {@link Uri} for requested lecturer.
         */
        public static Uri buildLecturerUri(int lecturerId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(lecturerId)).build();
        }
    }

    public static class Subject implements SubjectColumns, BaseColumns, SyncColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBJECT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".subject";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".subject";

        public static interface SUMMARY {
            String[] PROJECTION = {
                    _ID,
                    SUBJECT_NAME,
                    UPDATED
            };

            interface COLUMN {
                int _ID = 0;
                int SUBJECT_NAME = 1;
                int UPDATED = 2;
            }
        }

        /**
         * Build {@link Uri} for requested subject.
         */
        public static Uri buildSubjectUri(int subjectId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(subjectId)).build();
        }
    }

    public static class AcademicHour implements AcademicHourColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACADEMIC_HOUR).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY +
                        ".academic_hour";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY +
                        ".academic_hour";

        public static interface SUMMARY {
            String[] PROJECTION = {
                    _ID,
                    START_TIME,
                    END_TIME,
            };

            interface COLUMN {
                int _ID = 0;
                int START_TIME = 1;
                int END_TIME = 2;
            }
        }

        /**
         * Build {@link Uri} for requested academic hour.
         */
        public static Uri buildAcademicHourUri(int subjectId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(subjectId)).build();
        }
    }

    public static class Campus implements CampusColumns, BaseColumns, SyncColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAMPUS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".campus";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".campus";

        public static interface SUMMARY {
            String[] PROJECTION = {
                    _ID,
                    CAMPUS_NAME,
                    UPDATED
            };

            interface COLUMN {
                int _ID = 0;
                int CAMPUS_NAME = 1;
                int UPDATED = 2;
            }
        }

        /**
         * Build {@link Uri} for requested campus.
         */
        public static Uri buildCampusUri(int campusId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(campusId)).build();
        }
    }

    public static class Audience implements AudienceColumns, BaseColumns, SyncColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUDIENCE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".audience";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".audience";

        public static interface SUMMARY {
            String[] PROJECTION = {
                    _ID,
                    CAMPUS_ID,
                    AUDIENCE_NUMBER,
                    UPDATED
            };

            interface COLUMN {
                int _ID = 0;
                int CAMPUS_ID = 1;
                int AUDIENCE_NUMBER = 2;
                int UPDATED = 3;
            }
        }

        /**
         * Build {@link Uri} for requested audience.
         */
        public static Uri buildAudienceUri(int audienceId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(audienceId)).build();
        }
    }

    public static class Schedule implements ScheduleColumns, BaseColumns, SyncColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCHEDULE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".schedule";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".schedule";

        public static interface SUMMARY {
            String[] PROJECTION = {
                    _ID,
                    GROUP_ID,
                    SUBGROUP,
                    SUBJECT_ID,
                    DAY_OF_WEEK,
                    ACADEMIC_HOUR_ID,
                    LECTURER_ID,
                    AUDIENCE_ID,
                    PERIODICITY,
                    START_DATE,
                    END_DATE,
                    CLASS_TYPE,
                    UPDATED
            };

            interface COLUMN {
                int _ID = 0;
                int GROUP_ID = 1;
                int SUBGROUP = 2;
                int SUBJECT_ID = 3;
                int DAY_OF_WEEK = 4;
                int ACADEMIC_HOUR_ID = 5;
                int LECTURER_ID = 6;
                int AUDIENCE_ID = 7;
                int PERIODICITY = 8;
                int START_DATE = 9;
                int END_DATE = 10;
                int CLASS_TYPE = 11;
                int UPDATED = 12;
            }
        }

        /**
         * Build {@link Uri} for requested schedule.
         */
        public static Uri buildScheduleUri(int scheduleId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(scheduleId)).build();
        }
    }

    public static class FullSchedule {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FULL_SCHEDULE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." +
                CONTENT_AUTHORITY + ".full_schedule";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/vnd." + CONTENT_AUTHORITY + ".full_schedule";

        public static final String _ID =
                Tables.SCHEDULE + "." + BaseColumns._ID;
        public static final String SCHEDULE_SUBJECT_ID =
                Tables.SCHEDULE + "." + ScheduleColumns.SUBJECT_ID;
        public static final String SCHEDULE_AUDIENCE_ID =
                Tables.SCHEDULE + "." + ScheduleColumns.AUDIENCE_ID;
        public static final String SCHEDULE_ACADEMIC_HOUR_ID =
                Tables.SCHEDULE + "." + ScheduleColumns.ACADEMIC_HOUR_ID;
        public static final String SCHEDULE_LECTURER_ID =
                Tables.SCHEDULE + "." + ScheduleColumns.LECTURER_ID;
        public static final String SUBGROUP =
                Tables.SCHEDULE + "." + ScheduleColumns.SUBGROUP;
        public static final String DAY_OF_WEEK =
                Tables.SCHEDULE + "." + ScheduleColumns.DAY_OF_WEEK;
        public static final String CLASS_TYPE =
                Tables.SCHEDULE + "." + ScheduleColumns.CLASS_TYPE;
        public static final String PERIODICITY =
                Tables.SCHEDULE + "." + ScheduleColumns.PERIODICITY;
        public static final String START_DATE =
                Tables.SCHEDULE + "." + ScheduleColumns.START_DATE;
        public static final String END_DATE =
                Tables.SCHEDULE + "." + ScheduleColumns.END_DATE;

        public static final String ACADEMIC_HOUR_ID =
                Tables.ACADEMIC_HOUR + "." + BaseColumns._ID;
        public static final String ACADEMIC_HOUR_NUM =
                Tables.ACADEMIC_HOUR + "." + AcademicHourColumns.NUM;
        public static final String ACADEMIC_HOUR_STAT_TIME =
                Tables.ACADEMIC_HOUR + "." + AcademicHourColumns.START_TIME;
        public static final String ACADEMIC_HOUR_END_TIME =
                Tables.ACADEMIC_HOUR + "." + AcademicHourColumns.END_TIME;

        public static final String SUBJECT_ID =
                Tables.SUBJECT + "." + BaseColumns._ID;
        public static final String SUBJECT_NAME =
                Tables.SUBJECT + "." + SubjectColumns.SUBJECT_NAME;

        public static final String CAMPUS_ID =
                Tables.CAMPUS + "." + BaseColumns._ID;
        public static final String CAMPUS_NAME =
                Tables.CAMPUS + "." + CampusColumns.CAMPUS_NAME;

        public static final String AUDIENCE_ID =
                Tables.AUDIENCE + "." + BaseColumns._ID;
        public static final String AUDIENCE_CAMPUS_ID =
                Tables.AUDIENCE + "." + AudienceColumns.CAMPUS_ID;
        public static final String AUDIENCE_NUMBER =
                Tables.AUDIENCE + "." + AudienceColumns.AUDIENCE_NUMBER;

        public static final String LECTURER_ID =
                Tables.LECTURER + "." + BaseColumns._ID;
        public static final String LECTURER_NAME =
                Tables.LECTURER + "." + LecturerColumns.LECTURER_NAME;

        public static interface SUMMARY {
            public static final String TABLES = Tables.SCHEDULE + INNER_JOIN + Tables.SUBJECT +
                    ON + SUBJECT_ID + EQ + SCHEDULE_SUBJECT_ID +
                    INNER_JOIN + Tables.AUDIENCE + ON + AUDIENCE_ID + EQ + SCHEDULE_AUDIENCE_ID +
                    INNER_JOIN + Tables.CAMPUS + ON + CAMPUS_ID + EQ + AUDIENCE_CAMPUS_ID +
                    INNER_JOIN + Tables.ACADEMIC_HOUR +
                    ON + ACADEMIC_HOUR_ID + EQ + SCHEDULE_ACADEMIC_HOUR_ID +
                    INNER_JOIN + Tables.LECTURER + ON + LECTURER_ID + EQ + SCHEDULE_LECTURER_ID;

            String[] PROJECTION = {
                    _ID,
                    SUBGROUP,
                    DAY_OF_WEEK,
                    ACADEMIC_HOUR_NUM,
                    ACADEMIC_HOUR_STAT_TIME,
                    ACADEMIC_HOUR_END_TIME,
                    CLASS_TYPE,
                    SUBJECT_NAME,
                    LECTURER_NAME,
                    CAMPUS_NAME,
                    AUDIENCE_NUMBER
            };

            String SORT_ORDER = DAY_OF_WEEK + ASC + "," + ACADEMIC_HOUR_NUM + ASC;

            interface COLUMN {
                int _ID = 0;
                int SUBGROUP = 1;
                int DAY_OF_WEEK = 2;
                int ACADEMIC_HOUR_NUM = 3;
                int ACADEMIC_HOUR_STAT_TIME = 4;
                int ACADEMIC_HOUR_END_TIME = 5;
                int CLASS_TYPE = 6;
                int SUBJECT_NAME = 7;
                int LECTURER_NAME = 8;
                int CAMPUS_NAME = 9;
                int AUDIENCE_NUMBER = 10;
            }
        }

        public static String buildSelection(long date, int subgroup, int periodicity) {
            String dateSelection =
                    "(" + START_DATE + "<= " + date + AND + END_DATE + " >= " + date + ")";
            String subgroupSelection = "(" + SUBGROUP + EQ + "0" + ((subgroup > 0) ?
                    OR + SUBGROUP + EQ + subgroup + ")" : ")");
            String periodicitySelection = "(" + PERIODICITY + EQ + "0" + ((periodicity > 0) ?
                    OR + PERIODICITY + EQ + periodicity + ")" : ")");
            return dateSelection + AND + subgroupSelection + AND + periodicitySelection;
        }
    }
}
