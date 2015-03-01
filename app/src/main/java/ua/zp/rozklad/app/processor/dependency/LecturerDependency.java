package ua.zp.rozklad.app.processor.dependency;

import java.util.HashSet;
import java.util.Set;

import ua.zp.rozklad.app.rest.resource.Lecturer;

/**
 * @author Vojko Vladimir
 */
public class LecturerDependency {

    private Set<String> lecturers;

    public LecturerDependency() {
        lecturers = new HashSet<>();
    }

    public boolean hasLecturers() {
        return lecturers.size() != 0;
    }

    public String[] getLecturers() {
        return lecturers.toArray(new String[lecturers.size()]);
    }
    
    public void addLecturer(Lecturer lecturer) {
        lecturers.add(String.valueOf(lecturer.getId()));
    }
}
