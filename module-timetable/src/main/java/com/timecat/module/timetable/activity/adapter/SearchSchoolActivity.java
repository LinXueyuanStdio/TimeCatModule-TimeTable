package com.timecat.module.timetable.activity.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.identity.readonly.RouterHub;
import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.activity.WebViewActivity;
import com.timecat.module.timetable.activity.hpu.HpuRepertoryActivity;
import com.timecat.module.timetable.activity.hpu.ImportMajorActivity;
import com.timecat.module.timetable.adapter.SearchSchoolAdapter;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.AdapterResultV2;
import com.timecat.module.timetable.api.model.ListResult;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.School;
import com.timecat.module.timetable.api.model.StationModel;
import com.timecat.module.timetable.api.model.TemplateModel;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.model.SearchResultModel;
import com.timecat.module.timetable.tools.StationManager;
import com.timecat.module.timetable.tools.ViewTools;
import com.xiaojinzi.component.anno.RouterAnno;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RouterAnno(hostAndPath = RouterHub.TIMETABLE_SearchSchoolActivity)
public class SearchSchoolActivity extends AppCompatActivity {

    Activity context;

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        searchListView = (ListView) findViewById(R.id.id_search_listview);
        searchEditText = (EditText) findViewById(R.id.id_search_edittext);
        loadLayout = (LinearLayout) findViewById(R.id.id_loadlayout);
    }

    ListView searchListView;
    List<SearchResultModel> models;
    List<SearchResultModel> allDatas;
    SearchSchoolAdapter searchAdapter;
    List<TemplateModel> templateModels;
    String baseJs;

    EditText searchEditText;
    LinearLayout loadLayout;

    boolean firstStatus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_search_school);
        ViewTools.setStatusTextGrayColor(this);
        bindView();
        inits();
    }

    public void setLoadLayout(boolean isShow) {
        if (isShow) {
            loadLayout.setVisibility(View.VISIBLE);
        } else {
            loadLayout.setVisibility(View.GONE);
        }
    }

    private void inits() {
        context = this;
//        backLayout = findViewById(R.id.id_back);
//        backLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                goBack();
//            }
//        });

        models = new ArrayList<>();
        allDatas = new ArrayList<>();
        searchAdapter = new SearchSchoolAdapter(this, allDatas, models);
        searchListView.setAdapter(searchAdapter);
        searchEditText.addTextChangedListener(textWatcher);


        String school = ShareTools.getString(SearchSchoolActivity.this, ShareConstants.STRING_SCHOOL_NAME, "unknow");
        search(school);
    }

    public void itemClick(int i) {
        SearchResultModel model = models.get(i);
        if (model == null) return;
        //通用算法解析
        if (model.getType() == SearchResultModel.TYPE_COMMON) {
            TemplateModel templateModel = (TemplateModel) model.getObject();
            if (templateModel != null) {
                if (baseJs == null) {
                    ToastTools.show(this, "基础函数库发生异常，请联系qq:1193600556");
                } else if (templateModel.getTemplateTag().startsWith("custom/")) {
                    ActivityTools.toActivityWithout(this, AdapterTipActivity.class);
                } else {
                    ActivityTools.toActivityWithout(this,
                            AdapterSameTypeActivity.class, new BundleModel()
                                    .put("type", templateModel.getTemplateName())
                                    .put("js", templateModel.getTemplateJs() + baseJs));
                }
            }
        }
        //学校教务导入
        else if (model.getType() == SearchResultModel.TYPE_SCHOOL) {
            School school = (School) model.getObject();
            if (school != null) {
                if (school.getParsejs() != null && school.getParsejs().startsWith("template/")) {
                    TemplateModel searchModel = searchInTemplate(templateModels, school.getParsejs());
                    if (baseJs == null) {
                        ToastTools.show(this, "基础函数库发生异常，请联系qq:1193600556");
                        return;
                    }
                    if (searchModel != null) {
                        ActivityTools.toActivityWithout(this, AdapterSchoolActivity.class,
                                new BundleModel().setFromClass(SearchSchoolActivity.class)
                                        .put("school", school.getSchoolName())
                                        .put("url", school.getUrl())
                                        .put("type", school.getType())
                                        .put("parsejs", searchModel.getTemplateJs() + baseJs));
                    } else {
                        ToastTools.show(this, "通用解析模板发生异常，请联系qq:1193600556");
                    }
                } else {
                    ActivityTools.toActivityWithout(this, AdapterSchoolActivity.class,
                            new BundleModel().setFromClass(SearchSchoolActivity.class)
                                    .put("school", school.getSchoolName())
                                    .put("url", school.getUrl())
                                    .put("type", school.getType())
                                    .put("parsejs", school.getParsejs()));
                }

            }
        }
        //服务站
        else {
            StationModel stationModel = (StationModel) model.getObject();
            StationManager.openStationWithout(this, stationModel);
        }
    }

    public TemplateModel searchInTemplate(List<TemplateModel> models, String tag) {
        if (models == null || tag == null) return null;
        for (TemplateModel model : models) {
            if (model != null) {
                if (tag.equals(model.getTemplateTag())) {
                    return model;
                }
            }
        }
        return null;
    }

    public Activity getContext() {
        return context;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String key = charSequence.toString();
            firstStatus = false;
            if (TextUtils.isEmpty(key)) {
                models.clear();
                allDatas.clear();
                searchAdapter.notifyDataSetChanged();
            } else {
                search(charSequence.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private void setOnClick() {
        findViewById(R.id.id_search_search).setOnClickListener(v -> search());
        findViewById(R.id.id_menu_search).setOnClickListener(v -> toSearchActivity());
        findViewById(R.id.id_menu_changeclass).setOnClickListener(v -> changeClass());
        findViewById(R.id.id_menu_food).setOnClickListener(v -> food());
        findViewById(R.id.id_menu_score).setOnClickListener(v -> score());
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClick(position);
            }
        });
    }

    public void search() {
        String key = searchEditText.getText().toString();
        search(key);
    }

    public void search(final String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        models.clear();
        allDatas.clear();
        searchStation(key);

        if (!TextUtils.isEmpty(key)) {
            setLoadLayout(true);
            TimetableRequest.getAdapterSchoolsV2(this, key, new Callback<ObjResult<AdapterResultV2>>() {
                @Override
                public void onResponse(Call<ObjResult<AdapterResultV2>> call, Response<ObjResult<AdapterResultV2>> response) {
                    ObjResult<AdapterResultV2> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            showResult(result.getData(), key);
                        } else {
                            Toast.makeText(SearchSchoolActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchSchoolActivity.this, "school response is null!", Toast.LENGTH_SHORT).show();
                    }
                    setLoadLayout(false);
                }

                @Override
                public void onFailure(Call<ObjResult<AdapterResultV2>> call, Throwable t) {
                    setLoadLayout(false);
                }
            });
        }
    }

    public void searchStation(final String key) {
        if (!TextUtils.isEmpty(key)) {
            setLoadLayout(true);
            TimetableRequest.getStations(this, key, new Callback<ListResult<StationModel>>() {
                @Override
                public void onResponse(Call<ListResult<StationModel>> call, Response<ListResult<StationModel>> response) {
                    setLoadLayout(false);
                    ListResult<StationModel> result = response.body();
                    if (result != null) {
                        if (result.getCode() == 200) {
                            showStationResult(result.getData(), key);
                        } else {
                            Toast.makeText(SearchSchoolActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchSchoolActivity.this, "school response is null!", Toast.LENGTH_SHORT).show();
                    }
                    setLoadLayout(false);
                }

                @Override
                public void onFailure(Call<ListResult<StationModel>> call, Throwable t) {
                    setLoadLayout(false);
                }
            });
        }
    }

    private void showStationResult(List<StationModel> result, String key) {
        if (!firstStatus && searchEditText.getText() != null && key != null && !searchEditText.getText().toString().equals(key)) {
            return;
        }
        if (result == null) return;
        List<SearchResultModel> addList = new ArrayList<>();
        for (int i = 0; i < Math.min(result.size(), SearchSchoolAdapter.TYPE_STATION_MAX_SIZE); i++) {
            StationModel model = result.get(i);
            SearchResultModel searchResultModel = new SearchResultModel();
            searchResultModel.setType(SearchResultModel.TYPE_STATION);
            if (result.size() > 3) {
                searchResultModel.setType(SearchResultModel.TYPE_STATION_MORE);
            }
            searchResultModel.setObject(model);
            addModelToList(searchResultModel);
        }

        for (int i = 0; i < result.size(); i++) {
            StationModel model = result.get(i);
            SearchResultModel searchResultModel = new SearchResultModel();
            searchResultModel.setType(SearchResultModel.TYPE_STATION);
            searchResultModel.setObject(model);
            addList.add(searchResultModel);
        }

        sortResult();
        addAllDataToList(addList);
        searchAdapter.notifyDataSetChanged();
    }

    /**
     * @param result
     * @param key    用于校验输入框是否发生了变化，如果变化，则忽略
     */
    private void showResult(AdapterResultV2 result, String key) {
        if (!firstStatus && searchEditText.getText() != null && key != null && !searchEditText.getText().toString().equals(key)) {
            return;
        }
        if (result == null) return;
        baseJs = result.getBase();
        templateModels = result.getTemplate();
        List<School> list = result.getSchoolList();
        if (list == null) {
            return;
        }

        if (templateModels != null) {
            for (TemplateModel model : templateModels) {
                if (firstStatus || (model.getTemplateName() != null && model.getTemplateName().indexOf(key) != -1)) {
                    SearchResultModel searchResultModel = new SearchResultModel();
                    searchResultModel.setType(SearchResultModel.TYPE_COMMON);
                    searchResultModel.setObject(model);
                    addModelToList(searchResultModel);
                }
            }
        }

        for (School schoolBean : list) {
            SearchResultModel searchResultModel = new SearchResultModel();
            searchResultModel.setType(SearchResultModel.TYPE_SCHOOL);
            searchResultModel.setObject(schoolBean);
            addModelToList(searchResultModel);
        }

        SearchResultModel searchResultModel = new SearchResultModel();
        searchResultModel.setType(SearchResultModel.TYPE_COMMON);
        TemplateModel addAdapterModel = new TemplateModel();
        addAdapterModel.setTemplateName("添加学校适配");
        addAdapterModel.setTemplateTag("custom/upload");
        searchResultModel.setObject(addAdapterModel);

        if (firstStatus || addAdapterModel.getTemplateName().indexOf(key) != -1) {
            addModelToList(searchResultModel);
        }
        sortResult();
        searchAdapter.notifyDataSetChanged();
    }

    public void sortResult() {
        if (models != null) {
            Collections.sort(models);
        }
    }

    public synchronized void addModelToList(SearchResultModel searchResultModel) {
        if (models != null) {
            models.add(searchResultModel);
        }
    }

    public synchronized void addAllDataToList(List<SearchResultModel> searchResultModels) {
        if (allDatas != null) {
            for (SearchResultModel model : searchResultModels) {
                allDatas.add(model);
            }
        }
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(getContext(), MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }


    public void toSearchActivity() {
        ActivityTools.toActivity(this, HpuRepertoryActivity.class);
    }


    public void changeClass() {
        ActivityTools.toActivity(this, ImportMajorActivity.class);
    }


    public void food() {
        Toasty.info(this, "暂未开放!").show();
    }


    public void score() {
        int show = ShareTools.getInt(this, ShareConstants.KEY_SHOW_ALERTDIALOG, 1);
        if (show == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("查询指南")
                    .setMessage("步骤如下：\n\n1.点击[确认]\n2.登录VPN,若失败,可以使用其他同学的校园网账号,vpn密码默认是身份证后六位" +
                            "\n3.登陆教务处,输入个人教务处账号,密码默认为学号\n4.登陆成功后,网页无法点击,这是正常现象." +
                            "\n4.此时,点击右上角,选择[兼容模式菜单],选择需要的功能即可\n");

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BundleModel model = new BundleModel();
                    model.setFromClass(MainActivity.class);
                    model.put("title", "成绩查询");
                    model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
                    ShareTools.putInt(SearchSchoolActivity.this, ShareConstants.KEY_SHOW_ALERTDIALOG, 0);
                    ActivityTools.toActivity(SearchSchoolActivity.this, WebViewActivity.class, model);
                }
            }).setNegativeButton("取消", null);
            builder.create().show();
        } else {
            BundleModel model = new BundleModel();
            model.setFromClass(MainActivity.class);
            model.put("title", "成绩查询");
            model.put("url", "https://vpn.hpu.edu.cn/por/login_psw.csp");
            ActivityTools.toActivity(this, WebViewActivity.class, model);
        }
    }
}
