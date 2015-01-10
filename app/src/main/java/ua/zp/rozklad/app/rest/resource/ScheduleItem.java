package ua.zp.rozklad.app.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ua.zp.rozklad.app.rest.RESTMethod;

import static java.lang.Integer.parseInt;

/**
 * @author Vojko Vladimir
 */
public class ScheduleItem extends Resource {

    private int id;
    private int groupId;
    private int subjectId;
    private int dayOfWeek;
    private int academicHourId;
    private int lecturerId;
    private int audienceId;
    private int periodicity;
    private long startDate;
    private long endDate;
    private int classType;
    private long lastUpdate;

    public ScheduleItem(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        groupId = json.getInt(RESTMethod.Key.GROUP_ID);
        subjectId = json.getInt(RESTMethod.Key.LESSON_ID);
        dayOfWeek = json.getInt(RESTMethod.Key.DAY);
        academicHourId = json.getInt(RESTMethod.Key.TIME_ID);
        lecturerId = json.getInt(RESTMethod.Key.TEACHER_ID);
        audienceId = json.getInt(RESTMethod.Key.AUDIENCE_ID);
        periodicity = json.getInt(RESTMethod.Key.PERIODICITY);
        startDate = parseDate(json.getString(RESTMethod.Key.DATE_START));
        endDate = parseDate(json.getString(RESTMethod.Key.DATE_END));
        classType = json.getInt(RESTMethod.Key.LESSON_TYPE);
        lastUpdate = json.getLong(RESTMethod.Key.LAST_UPDATE);
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getAcademicHourId() {
        return academicHourId;
    }

    public int getLecturerId() {
        return lecturerId;
    }

    public int getAudienceId() {
        return audienceId;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public int getClassType() {
        return classType;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    private long parseDate(String dateToParse) {
        String[] date = dateToParse.split("-");

        Calendar calendar = new GregorianCalendar();
        calendar.set(parseInt(date[0]), parseInt(date[1]), parseInt(date[2]), 0, 0);

        return calendar.getTimeInMillis();
    }
}
