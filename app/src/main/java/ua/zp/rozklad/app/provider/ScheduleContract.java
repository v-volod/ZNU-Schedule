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

    interface DepartmentColumns {
        String DEPARTMENT_NAME = "department_name";
    }

    interface GroupColumns {
        String DEPARTMENT_ID = "department_id";
        String GROUP_NAME = "group_name";
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

    interface ClassTypeColumns {
        String CLASS_TYPE_NAME = "class_type_name";
    }

    interface ScheduleColumns {
        String GROUP_ID = "group_id";
        String SUBJECT_ID = "subject_id";
        String DAY_OF_WEEK = "day_of_week";
        String ACADEMIC_HOUR_ID = "academic_hour_id";
        String LECTURER_ID = "lecturer_id";
        String AUDIENCE_ID = "audience_id ";
        String PERIODICITY = "periodicity";
        String START_DATE = "start_date";
        String END_DATE = "end_date";
        String CLASS_TYPE_ID = "class_type_id";
    }

    public static final String CONTENT_AUTHORITY = "ua.zp.rozklad.app.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_DEPARTMENT = "department";
    private static final String PATH_GROUP = "group";
    private static final String PATH_LECTURER = "lecturer";
    private static final String PATH_SUBJECT = "subject";
    private static final String PATH_ACADEMIC_HOUR = "academic_hour";
    private static final String PATH_CAMPUS = "campus";
    private static final String PATH_AUDIENCE = "audience";
    private static final String PATH_CLASS_TYPE = "class_type";
    private static final String PATH_SCHEDULE = "schedule";
    private static final String PATH_SEARCH_DEPARTMENT = "search_department";
    private static final String PATH_SEARCH_GROUP = "search_group";

    public static class Department implements DepartmentColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DEPARTMENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".department";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".department";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                DEPARTMENT_NAME
        };

        /**
         * Build {@link Uri} for requested department.
         */
        public static Uri buildDepartmentUri(String departmentId) {
            return CONTENT_URI.buildUpon().appendPath(departmentId).build();
        }
    }

    public static class Group implements GroupColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".group";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".group";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                DEPARTMENT_ID,
                GROUP_NAME
        };

        /**
         * Build {@link Uri} for requested group.
         */
        public static Uri buildGroupUri(String groupId) {
            return CONTENT_URI.buildUpon().appendPath(groupId).build();
        }
    }

    public static class Lecturer implements LecturerColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LECTURER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".lecturer";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".lecturer";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                LECTURER_NAME
        };

        /**
         * Build {@link Uri} for requested lecturer.
         */
        public static Uri buildLecturerUri(String lecturerId) {
            return CONTENT_URI.buildUpon().appendPath(lecturerId).build();
        }
    }

    public static class Subject implements SubjectColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBJECT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".subject";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".subject";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                SUBJECT_NAME
        };

        /**
         * Build {@link Uri} for requested subject.
         */
        public static Uri buildSubjectUri(String subjectId) {
            return CONTENT_URI.buildUpon().appendPath(subjectId).build();
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
                END_TIME
        };

        /**
         * Build {@link Uri} for requested academic hour.
         */
        public static Uri buildAcademicHourUri(String subjectId) {
            return CONTENT_URI.buildUpon().appendPath(subjectId).build();
        }
    }

    public static class Campus implements CampusColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAMPUS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".campus";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".campus";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                CAMPUS_NAME
        };

        /**
         * Build {@link Uri} for requested campus.
         */
        public static Uri buildCampusUri(String campus) {
            return CONTENT_URI.buildUpon().appendPath(campus).build();
        }
    }

    public static class Audience implements AudienceColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUDIENCE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".audience";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".audience";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                CAMPUS_ID,
                AUDIENCE_NUMBER
        };

        /**
         * Build {@link Uri} for requested audience.
         */
        public static Uri buildAudienceUri(String audienceId) {
            return CONTENT_URI.buildUpon().appendPath(audienceId).build();
        }
    }

    public static class ClassType implements ClassTypeColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CLASS_TYPE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".class_type";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".class_type";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                CLASS_TYPE_NAME
        };

        /**
         * Build {@link Uri} for requested class type.
         */
        public static Uri buildClassTypeUri(String classTypeId) {
            return CONTENT_URI.buildUpon().appendPath(classTypeId).build();
        }
    }

    public static class Schedule implements ScheduleColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCHEDULE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".schedule";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".schedule";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                GROUP_ID,
                SUBJECT_ID,
                DAY_OF_WEEK,
                ACADEMIC_HOUR_ID,
                LECTURER_ID,
                AUDIENCE_ID,
                PERIODICITY,
                START_DATE,
                END_DATE,
                CLASS_TYPE_ID
        };

        /**
         * Build {@link Uri} for requested schedule.
         */
        public static Uri buildScheduleUri(String scheduleId) {
            return CONTENT_URI.buildUpon().appendPath(scheduleId).build();
        }
    }

    public static class SearchDepartment implements DepartmentColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_DEPARTMENT).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd." + CONTENT_AUTHORITY + ".search_department";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd." + CONTENT_AUTHORITY + ".search_department";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                DEPARTMENT_NAME
        };

        /**
         * Build {@link Uri} for requested department.
         */
        public static Uri buildSearchDepartmentUri(String departmentId) {
            return CONTENT_URI.buildUpon().appendPath(departmentId).build();
        }
    }

    public static class SearchGroup implements GroupColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_GROUP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".search_group";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + ".search_group";

        public static final String[] TABLE_SUMMARY = {
                _ID,
                DEPARTMENT_ID,
                GROUP_NAME
        };

        /**
         * Build {@link Uri} for requested group.
         */
        public static Uri buildSearchGroupUri(String groupId) {
            return CONTENT_URI.buildUpon().appendPath(groupId).build();
        }
    }
}
