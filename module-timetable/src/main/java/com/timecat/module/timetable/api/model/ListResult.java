package com.timecat.module.timetable.api.model;

import java.util.List;

/**
 * Created by Liu ZhuangFei on 2018/2/23.
 */

public class ListResult<T> extends BaseResult {

    List<T> data;

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }
}
