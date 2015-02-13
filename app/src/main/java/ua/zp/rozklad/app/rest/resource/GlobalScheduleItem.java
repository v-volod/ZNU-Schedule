package ua.zp.rozklad.app.rest.resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ua.zp.rozklad.app.rest.RESTMethod;

import static java.lang.Integer.parseInt;

/**
 * @author Vojko Vladimir
 */
public class GlobalScheduleItem extends Resource {

    private int id;
    private int[] group;
    private int subgroup;
    private int subjectId;
    private int dayOfWeek;
    private int academicHourId;
    private int lecturerId;
    private int audienceId;
    private int periodicity;
    private long startDate;
    private long endDate;
    private int classType;
    private boolean freeTrajectory;
    private long lastUpdate;

    public GlobalScheduleItem(JSONObject json) throws JSONException {
        id = json.getInt(RESTMethod.Key.ID);
        JSONArray groupArray = json.getJSONArray(RESTMethod.Key.GROUP);
        group = new int[groupArray.length()];
        for (int i = 0; i < groupArray.length(); i++) {
            group[i] = groupArray.getInt(i);
        }
        subgroup = json.getInt(RESTMethod.Key.SUBGROUP);
        subjectId = json.getInt(RESTMethod.Key.LESSON_ID);
        dayOfWeek = json.getInt(RESTMethod.Key.DAY);
        academicHourId = json.getInt(RESTMethod.Key.TIME_ID);
        lecturerId = json.getInt(RESTMethod.Key.TEACHER_ID);
        audienceId = json.getInt(RESTMethod.Key.AUDIENCE_ID);
        periodicity = json.getInt(RESTMethod.Key.PERIODICITY);
        startDate = parseDate(json.getString(RESTMethod.Key.DATE_START));
        endDate = parseDate(json.getString(RESTMethod.Key.DATE_END));
        classType = json.getInt(RESTMethod.Key.LESSON_TYPE);
        freeTrajectory = json.getBoolean(RESTMethod.Key.FREE_TRAJECTORY);
        lastUpdate = json.getLong(RESTMethod.Key.LAST_UPDATE);
    }

    public int getId() {
        return id;
    }

    public int[] getGroup() {
        return group;
    }

    public int getSubgroup() {
        return subgroup;
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

    public boolean getFreeTrajectory() {
        return freeTrajectory;
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

    public ArrayList<ScheduleItem> getScheduleItems() {
        ArrayList<ScheduleItem> items = new ArrayList<>();
        for (int id : group) {
            items.add(new ScheduleItem(this, id));
        }
        return items;
    }
}
