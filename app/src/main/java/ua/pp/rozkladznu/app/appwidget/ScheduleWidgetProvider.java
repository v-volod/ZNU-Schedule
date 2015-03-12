package ua.pp.rozkladznu.app.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import ua.pp.rozkladznu.app.account.GroupAccount;

/**
 * Created by kkxmshu on 11.03.15.
 */
public class ScheduleWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
