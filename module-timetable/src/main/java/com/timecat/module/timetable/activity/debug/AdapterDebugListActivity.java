package com.timecat.module.timetable.activity.debug;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.AdapterDebugModel;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.UserDebugModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterDebugListActivity extends AppCompatActivity {


    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        listView = (ListView) findViewById(R.id.id_listView);
    }

    ListView listView;
    SimpleAdapter simpleAdapter;
    List<Map<String, String>> list = new ArrayList<>();

    String name;
    String userkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_adapter_debug_list);
        bindView();

        simpleAdapter = new SimpleAdapter(this, list, R.layout.timetable_item_adapter_debug,
                new String[]{"name"},
                new int[]{R.id.tv_name});
        listView.setAdapter(simpleAdapter);

        name = ShareTools.getString(this, "debug_name", null);
        userkey = ShareTools.getString(this, "debug_userkey", null);
        if (name == null || userkey == null) {
            ActivityTools.toBackActivityAnim(this, AdapterDebugTipActivity.class);
        }
        getData();
    }

    public void getData() {
        TimetableRequest.getUserInfo(this, name, userkey,
                new Callback<ObjResult<UserDebugModel>>() {
                    @Override
                    public void onResponse(Call<ObjResult<UserDebugModel>> call, Response<ObjResult<UserDebugModel>> response) {
                        ObjResult<UserDebugModel> result = response.body();
                        if (result != null) {
                            UserDebugModel modle = result.getData();
                            if (result.getCode() == 200) {
                                List<AdapterDebugModel> list = modle.getMyAdapter();
                                showList(list);
                            } else {
                                Toasty.error(AdapterDebugListActivity.this, result.getMsg()).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ObjResult<UserDebugModel>> call, Throwable t) {
                        Toasty.error(AdapterDebugListActivity.this, t.getMessage()).show();
                    }
                });
    }

    private void showList(List<AdapterDebugModel> data) {
        list.clear();
        int index = 1;
        for (AdapterDebugModel model : data) {
            if (model != null) {
                Map<String, String> map = new HashMap<>();
                map.put("name", model.getSchoolName());
                map.put("aid", model.getAid() + "");
                list.add(map);
            }
            index++;
        }
        simpleAdapter.notifyDataSetChanged();
    }

    public void itemClick(int pos) {
        Intent intent = new Intent(this, AdapterDebugHtmlActivity.class);
        intent.putExtra("schoolName", list.get(pos).get("name"));
        intent.putExtra("aid", list.get(pos).get("aid"));
        startActivity(intent);
    }

    private void setOnClick() {
        findViewById(R.id.ib_back).setOnClickListener(v -> goBack());
        findViewById(R.id.tv_logout).setOnClickListener(v -> logout());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClick(position);
            }
        });
    }

    public void goBack() {
        finish();
    }


    public void logout() {
        clearLocal();
        ActivityTools.toBackActivityAnim(this, AdapterDebugTipActivity.class);
    }

    void clearLocal() {
        ShareTools.putString(this, "debug_name", null);
        ShareTools.putString(this, "debug_userkey", null);
    }
}
