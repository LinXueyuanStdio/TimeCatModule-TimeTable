package com.timecat.module.timetable.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.identity.readonly.RouterHub;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.BaseResult;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.event.UpdateSchoolEvent;
import com.timecat.module.timetable.tools.DeviceTools;
import com.xiaojinzi.component.anno.RouterAnno;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RouterAnno(hostAndPath = RouterHub.TIMETABLE_BindSchoolActivity)
public class BindSchoolActivity extends AppCompatActivity {

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        schoolEdit = (EditText) findViewById(R.id.id_school_edit);
    }

    EditText schoolEdit;

    public static final String FINISH_WHEN_NON_NULL = "finish_when_non_null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_bind_school);
        bindView();

        int finishWhenNonNull = getIntent().getIntExtra(FINISH_WHEN_NON_NULL, 1);
        String schoolName = ShareTools.getString(BindSchoolActivity.this, ShareConstants.STRING_SCHOOL_NAME, null);
        if (finishWhenNonNull == 1 && !TextUtils.isEmpty(schoolName)) {
            finish();
        }
    }


    private void setOnClick() {
        findViewById(R.id.id_bind_button).setOnClickListener(v -> onBindButtonClicked());
    }

    public void onBindButtonClicked() {
        String school = schoolEdit.getText().toString();
        if (!TextUtils.isEmpty(school) && !TextUtils.isEmpty(school.trim())) {
            bindSchoolForWeb(school.trim());
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.timetable_anim_station_static, R.anim.timetable_anim_station_close_activity);
    }

    public void bindSchoolForWeb(final String school) {
        String deviceId = DeviceTools.getDeviceId(this);
        if (deviceId == null) return;
        TimetableRequest.bindSchool(this, deviceId, school, new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                if (response == null) return;
                BaseResult result = response.body();
                if (result.getCode() == 200) {
                    ShareTools.putString(BindSchoolActivity.this, ShareConstants.STRING_SCHOOL_NAME, school);
                    Toast.makeText(BindSchoolActivity.this, "关联成功", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new UpdateSchoolEvent(school));
//                    ActivityTools.toActivityWithout(BindSchoolActivity.this,MainActivity.class);
                    finish();
                } else {
                    Toast.makeText(BindSchoolActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                Toast.makeText(BindSchoolActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
