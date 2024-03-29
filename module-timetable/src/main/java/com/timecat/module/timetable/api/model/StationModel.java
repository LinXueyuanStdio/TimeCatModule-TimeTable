package com.timecat.module.timetable.api.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Liu ZhuangFei on 2019/2/6.
 */
public class StationModel extends DataSupport implements Serializable{
    private String tag;
    private String name;
    private String img;
    private String url;
    private int stationId;
    private String owner;
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
