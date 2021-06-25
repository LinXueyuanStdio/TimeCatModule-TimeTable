package com.timecat.module.timetable.api.service;

import com.timecat.module.timetable.api.constants.UrlContacts;
import com.timecat.module.timetable.api.model.AdapterInfo;
import com.timecat.module.timetable.api.model.AdapterResultV2;
import com.timecat.module.timetable.api.model.BaseResult;
import com.timecat.module.timetable.api.model.CheckBindResultModel;
import com.timecat.module.timetable.api.model.CheckModel;
import com.timecat.module.timetable.api.model.HtmlDetail;
import com.timecat.module.timetable.api.model.HtmlSummary;
import com.timecat.module.timetable.api.model.ListResult;
import com.timecat.module.timetable.api.model.MessageModel;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.School;
import com.timecat.module.timetable.api.model.SchoolPersonModel;
import com.timecat.module.timetable.api.model.StationModel;
import com.timecat.module.timetable.api.model.UserDebugModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Liu ZhuangFei on 2018/2/23.
 */

public interface SchoolService {

    @POST(UrlContacts.URL_GET_ADAPTER_SCHOOLS)
    @FormUrlEncoded
    Call<ListResult<School>> getAdapterSchools(@Field("key") String key);

    @POST(UrlContacts.URL_GET_ADAPTER_SCHOOLS_V2)
    @FormUrlEncoded
    Call<ObjResult<AdapterResultV2>> getAdapterSchoolsV2(@Field("key") String key);

    @POST(UrlContacts.URL_PUT_HTML)
    @FormUrlEncoded
    Call<BaseResult> putHtml(@Field("school") String school,
                             @Field("url") String url,
                             @Field("html") String html);

    @POST(UrlContacts.URL_CHECK_SCHOOL)
    @FormUrlEncoded
    Call<ObjResult<CheckModel>> checkSchool(@Field("school") String school);

    @POST(UrlContacts.URL_GET_USER_INFO)
    @FormUrlEncoded
    Call<ObjResult<UserDebugModel>> getUserInfo(@Field("name") String name, @Field("id") String id);

    @POST(UrlContacts.URL_FIND_HTML_SUMMARY)
    @FormUrlEncoded
    Call<ListResult<HtmlSummary>> findHtmlummary(@Field("school") String schoolName);

    @POST(UrlContacts.URL_FIND_HTML_DETAIL)
    @FormUrlEncoded
    Call<ObjResult<HtmlDetail>> findHtmlDetail(@Field("filename") String schoolName);

    @POST(UrlContacts.URL_GET_ADAPTER_INFO)
    @FormUrlEncoded
    Call<ObjResult<AdapterInfo>> getAdapterInfo(@Field("key") String uid,
                                                @Field("aid") String aid);

    @POST(UrlContacts.URL_GET_STATIONS)
    @FormUrlEncoded
    Call<ListResult<StationModel>> getStations(@Field("key") String key);

    @POST(UrlContacts.URL_GET_MESSAGES)
    @FormUrlEncoded
    Call<ListResult<MessageModel>> getMessages(@Field("device") String device,
                                               @Field("school") String school,
                                               @Field("mode") String mode);

    @POST(UrlContacts.URL_SET_MESSAGE_READ)
    @FormUrlEncoded
    Call<BaseResult> setMessageRead(@Field("id") int messageId);

    @POST(UrlContacts.URL_BIND_SCHOOL)
    @FormUrlEncoded
    Call<BaseResult> bindSchool(@Field("device") String device,
                                @Field("school") String school);

    @POST(UrlContacts.URL_GET_SCHOOL_PERSON_COUNT)
    @FormUrlEncoded
    Call<ObjResult<SchoolPersonModel>> getSchoolPersonCount(@Field("school") String school);

    @POST(UrlContacts.URL_CHECK_IS_BIND_SCHOOL)
    @FormUrlEncoded
    Call<ObjResult<CheckBindResultModel>> checkIsBindSchool(@Field("device") String device);

    @POST(UrlContacts.URL_GET_STATION_BY_ID)
    @FormUrlEncoded
    Call<ListResult<StationModel>> getStationById(@Field("id") int id);
 }
