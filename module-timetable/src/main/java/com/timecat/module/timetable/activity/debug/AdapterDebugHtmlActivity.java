package com.timecat.module.timetable.activity.debug;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.HtmlSummary;
import com.timecat.module.timetable.api.model.ListResult;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterDebugHtmlActivity extends AppCompatActivity {


    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        listView = (ListView) findViewById(R.id.id_listView);
        titleTextView = (TextView) findViewById(R.id.id_debug_html_title);
    }

    ListView listView;
    SimpleAdapter simpleAdapter;
    List<Map<String, String>> list = new ArrayList<>();

    String aid = "";
    TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_adapter_debug_html);
        bindView();
        String schoolName = getIntent().getStringExtra("schoolName");
        aid = getIntent().getStringExtra("aid");

        simpleAdapter = new SimpleAdapter(this, list, R.layout.timetable_item_adapter_debug,
                new String[]{"name"},
                new int[]{R.id.tv_name});
        listView.setAdapter(simpleAdapter);

        titleTextView.setText(schoolName);
        getData(schoolName);
    }

    public void getData(String schoolName) {
        TimetableRequest.findHtmlSummary(this, schoolName, new Callback<ListResult<HtmlSummary>>() {
            @Override
            public void onResponse(Call<ListResult<HtmlSummary>> call, Response<ListResult<HtmlSummary>> response) {
                ListResult<HtmlSummary> result = response.body();
                if (result != null) {
                    List<HtmlSummary> summaryList = result.getData();
                    if (result.getCode() == 200) {
                        showList(summaryList);
                    } else {
                        Toasty.error(AdapterDebugHtmlActivity.this, result.getMsg()).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ListResult<HtmlSummary>> call, Throwable t) {
                Toasty.error(AdapterDebugHtmlActivity.this, t.getMessage()).show();
            }
        });
    }

    private void showList(List<HtmlSummary> htmlSummary) {
        list.clear();
        for (HtmlSummary model : htmlSummary) {
            if (model != null) {
                Map<String, String> map = new HashMap<>();
                map.put("name", model.getFilename());
                list.add(map);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    public void itemClick(int pos) {
        Intent intent = new Intent(this, DebugActivity.class);
        intent.putExtra("uid", ShareTools.getString(this, "debug_userkey", null));
        intent.putExtra("aid", aid);
        intent.putExtra("filename", list.get(pos).get("name"));
        startActivity(intent);
    }


    private void setOnClick() {
        findViewById(R.id.ib_back).setOnClickListener(v -> goBack());
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
}
