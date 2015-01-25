package ua.zp.rozklad.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

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

    public static class Lecturer implements LecturerColumns, BaseColumns, SyncColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LECTURER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".lecturer";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".lecturer";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                LECTURER_NAME,
                UPDATED
        };

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

        public static final String[] TABLE_SUMMARY = {
                _ID,
                SUBJECT_NAME,
                UPDATED
        };

        public static interface COLUMN_ID {
            int _ID = 0;
            int SUBJECT_NAME = 1;
            int UPDATED = 2;
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


        public static final String[] TABLE_SUMMARY = {
                _ID,
                START_TIME,
                END_TIME,
        };

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

        public static final String[] TABLE_SUMMARY = {
                _ID,
                CAMPUS_NAME,
                UPDATED
        };

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

        public static final String[] TABLE_SUMMARY = {
                _ID,
                CAMPUS_ID,
                AUDIENCE_NUMBER,
                UPDATED
        };

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

        public static final String[] TABLE_SUMMARY = {
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

        public static interface COLUMN_ID {
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

        /**
         * Build {@link Uri} for requested schedule.
         */
        public static Uri buildScheduleUri(int scheduleId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(scheduleId)).build();
        }
    }
}
