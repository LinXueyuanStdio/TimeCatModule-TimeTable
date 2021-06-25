package com.timecat.module.timetable.api.service;

import com.timecat.module.timetable.api.constants.UrlContacts;
import com.timecat.module.timetable.api.model.ListResult;
import com.timecat.module.timetable.api.model.MajorModel;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.TimetableModel;
import com.timecat.module.timetable.api.model.TimetableResultModel;
import com.timecat.module.timetable.api.model.ValuePair;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Liu ZhuangFei on 2018/2/23.
 */

public interface TimetableService {

    @POST(UrlContacts.URL_GET_BY_MAJOR)
    @FormUrlEncoded
    Call<ObjResult<TimetableResultModel>> getByMajor(@Field("major") String major);

    @POST(UrlContacts.URL_FIND_MAJOR)
    @FormUrlEncoded
    Call<ListResult<MajorModel>> findMajor(@Field("major") String major);

    @POST(UrlContacts.URL_GET_BY_NAME)
    @FormUrlEncoded
    Call<ListResult<TimetableModel>> getByName(@Field("name") String name);

    @POST(UrlContacts.URL_PUT_VALUE)
    @FormUrlEncoded
    Call<ObjResult<ValuePair>> putValue(@Field("value") String value);

    @POST(UrlContacts.URL_GET_VALUE)
    @FormUrlEncoded
    Call<ObjResult<ValuePair>> getValue(@Field("id") String id);
 }
