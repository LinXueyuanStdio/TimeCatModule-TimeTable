package com.timecat.module.timetable.activity.schedule;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.model.ScheduleName;
import com.zhuangfei.toolkit.tools.ActivityTools;

import es.dmoral.toasty.Toasty;

public class CreateScheduleNameActivity extends AppCompatActivity {

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        nameEdit = (EditText) findViewById(R.id.et_schedulename);
    }

    EditText nameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_create_schedule_name);
        bindView();
    }


    private void setOnClick() {
        findViewById(R.id.cv_save).setOnClickListener(v -> save());
        findViewById(R.id.id_back).setOnClickListener(v -> goBack());
    }

    public void save() {
        String name = nameEdit.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toasty.warning(this, "课表名称不可为空", Toast.LENGTH_SHORT).show();
        } else if (name.equals("默认课表")) {
            Toasty.error(this, "名称不合法").show();
        } else {
            ScheduleName scheduleName = new ScheduleName();
            scheduleName.setName(name);
            scheduleName.setTime(System.currentTimeMillis());
            boolean isSave = scheduleName.save();
            if (isSave) {
                Toasty.success(this, "创建课表成功", Toast.LENGTH_SHORT).show();
                goBack();
            } else {
                Toasty.error(this, "创建课表失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }


    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MultiScheduleActivity.class);
    }
}
