package com.timecat.module.timetable.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.adapter.MessageAdapter;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.ListResult;
import com.timecat.module.timetable.api.model.MessageModel;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.tools.DeviceTools;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        listView = (ListView) findViewById(R.id.id_listview);
        loadLayout = (LinearLayout) findViewById(R.id.id_loadlayout);
    }

    ListView listView;

    MessageAdapter adapter;
    List<MessageModel> list;

    LinearLayout loadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_message);
        bindView();
        inits();
    }

    private void inits() {
        list = new ArrayList<>();
        adapter = new MessageAdapter(this, list);
        listView.setAdapter(adapter);
        getMessages();
    }

    public void setLoadLayout(boolean isShow) {
        if (isShow) {
            loadLayout.setVisibility(View.VISIBLE);
        } else {
            loadLayout.setVisibility(View.GONE);
        }
    }

    public void getMessages() {
        String deviceId = DeviceTools.getDeviceId(this);
        if (deviceId == null) return;
        String school = ShareTools.getString(MessageActivity.this, ShareConstants.STRING_SCHOOL_NAME, "unknow");
        setLoadLayout(true);
        TimetableRequest.getMessages(this, deviceId, school, null, new Callback<ListResult<MessageModel>>() {
            @Override
            public void onResponse(Call<ListResult<MessageModel>> call, Response<ListResult<MessageModel>> response) {
                setLoadLayout(false);
                if (response == null) return;
                ListResult<MessageModel> result = response.body();
                if (result.getCode() == 200) {
                    showMessages(result.getData());
                } else {
                    Toast.makeText(MessageActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListResult<MessageModel>> call, Throwable t) {
                setLoadLayout(false);
            }
        });
    }

    private void showMessages(List<MessageModel> models) {
        if (models == null) return;
        list.clear();
        list.addAll(models);
        adapter.notifyDataSetChanged();
    }


    private void setOnClick() {
        findViewById(R.id.id_back).setOnClickListener(v -> goBack());
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
