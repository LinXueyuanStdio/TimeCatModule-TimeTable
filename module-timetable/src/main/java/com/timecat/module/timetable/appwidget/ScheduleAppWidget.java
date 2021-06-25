package com.timecat.module.timetable.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.timecat.module.timetable.R;
import com.timecat.module.timetable.tools.TimetableTools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class ScheduleAppWidget extends AppWidgetProvider {
    private static final String TAG = "ScheduleAppWidget";
    public static final String CLICK_ACTION = "com.zhuangfei.action.POINTER_CLICK";
    public static final String UPDATE_ACTION = "com.zhuangfei.action.APPWIDGET_UPDATE";
    public static final String UPDATE_APPWIDGET="android.appwidget.action.APPWIDGET_UPDATE";

    public static final String INT_EXTRA_START = "int_extra_start22";

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(UPDATE_APPWIDGET)||intent.getAction().equals(UPDATE_ACTION) || intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, ScheduleAppWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.id_widget_listview);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, ComponentName provider) {

        Log.d(TAG, "updateAppWidget: ");
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_schedule_app_widget);

        Intent serviceIntent = new Intent(context, ScheduleService.class);
        views.setRemoteAdapter(R.id.id_widget_listview, serviceIntent);

        SimpleDateFormat sdf=new SimpleDateFormat("MM月dd日");
        views.setTextViewText(R.id.id_appwidget_date,sdf.format(new Date()));

        SimpleDateFormat sdf2=new SimpleDateFormat("EEEE");
        int curWeek = TimetableTools.getCurWeek(context);
        views.setTextViewText(R.id.id_appwidget_week,"第"+curWeek+"周  "+sdf2.format(new Date())+" / 时光猫");

        // template to handle the click listener for each item
        Intent intent = new Intent();
        intent.setAction(CLICK_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 设置intent模板
        views.setPendingIntentTemplate(R.id.id_widget_listview, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
    }
}

