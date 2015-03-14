package ua.pp.rozkladznu.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;

import ua.pp.rozkladznu.app.R;
import ua.pp.rozkladznu.app.rest.RESTMethod;

import static java.lang.String.format;

/**
 * @author Vojko Vladimir
 */
public class UiUtils {

    public static TypedValue getThemeAttribute(Context context, int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value;
    }

    public static void reportScheduleMistake(Activity parent, int groupId) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(RESTMethod.getReportUrlForGroup(groupId)));
        parent.startActivity(intent);
    }
}
