package ua.pp.rozkladznu.app.rest.resource;

/**
 * @author Vojko Vladimir
 */
public class ScheduleItem extends Resource {

    private int id;
    private int groupId;
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

    public ScheduleItem(GlobalScheduleItem item, int groupId) {
        id = item.getId();
        this.groupId = groupId;
        subgroup = item.getSubgroup();
        subjectId = item.getSubjectId();
        dayOfWeek = item.getDayOfWeek();
        academicHourId = item.getAcademicHourId();
        lecturerId = item.getLecturerId();
        audienceId = item.getAudienceId();
        periodicity = item.getPeriodicity();
        startDate = item.getStartDate();
        endDate = item.getEndDate();
        classType = item.getClassType();
        freeTrajectory = item.getFreeTrajectory();
        lastUpdate = item.getLastUpdate();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
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

    @Override
    public String toString() {
        return "id: " + id + ", groupId: " + groupId + ", lecturerId: " + lecturerId + ", last_update: " + lastUpdate;
    }
}
