package com.timecat.module.timetable.listener;

import com.timecat.module.timetable.api.model.ScheduleName;

/**
 * Created by Liu ZhuangFei on 2018/9/9.
 */
public interface OnSwitchTableListener {
    void onSwitchTable(ScheduleName scheduleName);
}
