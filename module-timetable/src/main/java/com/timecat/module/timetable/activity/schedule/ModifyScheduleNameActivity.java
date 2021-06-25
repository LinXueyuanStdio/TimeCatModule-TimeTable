package com.timecat.module.timetable.activity.schedule;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.model.ScheduleName;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import org.litepal.crud.DataSupport;

import es.dmoral.toasty.Toasty;

public class ModifyScheduleNameActivity extends AppCompatActivity {

    public static final String STRING_EXTRA_NAME = "extra_name";
    public static final String INT_EXTRA_ID = "extra_id";


    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        nameEdit = (EditText) findViewById(R.id.et_schedulename);
    }

    EditText nameEdit;

    int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_modify_schedule_name);
        bindView();
        inits();
    }

    private void inits() {
        String name = BundleTools.getString(this, STRING_EXTRA_NAME, null);
        id = (int) BundleTools.getInt(this, INT_EXTRA_ID, -1);
        if (name == null || id == -1) {
            goBack();
        } else {
            nameEdit.setText(name);
        }
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
            ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
            if (scheduleName != null) {
                scheduleName.setName(name);
                scheduleName.update(scheduleName.getId());
                Toasty.success(this, "修课表成功", Toast.LENGTH_SHORT).show();
                goBack();
            } else {
                Toasty.error(this, "修改课表失败", Toast.LENGTH_SHORT).show();
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
