package ua.pp.rozkladznu.app.processor.dependency;

import java.util.HashSet;
import java.util.Set;

import ua.pp.rozkladznu.app.rest.resource.Audience;

/**
 * @author Vojko Vladimir
 */
public class AudienceDependency {

    private Set<String> campuses;

    public AudienceDependency() {
        campuses = new HashSet<>();
    }

    public boolean hasCampuses() {
        return campuses.size() != 0;
    }

    public String[] getCampuses() {
        return campuses.toArray(new String[campuses.size()]);
    }

    public void addCampus(String campus) {
        campuses.add(campus);
    }

    public void addAudience(Audience audience) {
        addCampus(String.valueOf(audience.getCampusId()));
    }
}
