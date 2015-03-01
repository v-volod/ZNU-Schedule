package ua.zp.rozklad.app.util;

import com.yandex.metrica.YandexMetrica;

import java.util.HashMap;
import java.util.Map;

import ua.zp.rozklad.app.account.GroupAccount;
import ua.zp.rozklad.app.rest.resource.Department;
import ua.zp.rozklad.app.rest.resource.Group;

/**
 * @author Vojko Vladimir
 */
public class MetricaUtils {
    private static final String DEPARTMENT = "Факультет";
    private static final String GROUP = "Группа";
    private static final String SUBGROUP = "Подгруппа";

    private interface Events {
        String ADD_GROUP = "Добавлена группа";
        String CHANGE_GROUP = "Смена группы";
    }

    public static void reportGroupChange(GroupAccount account) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(DEPARTMENT, account.getDepartmentName());
        attributes.put(GROUP, account.getGroupName());

        YandexMetrica.reportEvent(Events.CHANGE_GROUP, attributes);
    }

    public static void reportGroupAdd(Department department, Group group, int subgroup) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(DEPARTMENT, department.getName());
        attributes.put(GROUP, group.getName());
        if (group.getSubgroupCount() > 0) {
            attributes.put(SUBGROUP, subgroup);
        }

        YandexMetrica.reportEvent(Events.ADD_GROUP, attributes);
    }
}
