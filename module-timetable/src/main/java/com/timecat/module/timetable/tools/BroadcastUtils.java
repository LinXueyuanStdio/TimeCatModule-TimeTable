package com.timecat.module.timetable.tools;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.timecat.module.timetable.appwidget.ScheduleAppWidget;

/**
 * Created by Liu ZhuangFei on 2018/8/14.
 */

public class BroadcastUtils {

    public static void refreshAppWidget(Context context) {
        Intent intent = new Intent(ScheduleAppWidget.UPDATE_ACTION);
        intent.putExtra(ScheduleAppWidget.INT_EXTRA_START,0);
        intent.setComponent(new ComponentName(context, ScheduleAppWidget.class));
        context.sendBroadcast(intent);
    }


}
