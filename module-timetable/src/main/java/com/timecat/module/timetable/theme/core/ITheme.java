package com.timecat.module.timetable.theme.core;

/**
 * Created by Liu ZhuangFei on 2019/1/11.
 */
public interface ITheme {
    void execute();
    void onSuccess(ThemeModel themeModel);
    void onError(String exception);
}
