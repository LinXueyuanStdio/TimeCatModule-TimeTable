package com.timecat.module.timetable.activity.adapter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

import es.dmoral.toasty.Toasty;

public class AdapterSameTypeActivity extends AppCompatActivity {

    public static final String STRING_EXTRA_NAME = "extra_name";
    public static final String INT_EXTRA_ID = "extra_id";

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        nameEdit = (EditText) findViewById(R.id.id_school_edittext);
        titleTextView = (TextView) findViewById(R.id.id_title);
    }

    EditText nameEdit;


    TextView titleTextView;

    String js = null;
    int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_adapter_same_type);
        bindView();
        inits();
    }

    private void inits() {
        js = BundleTools.getString(this, "js", null);
        String type = BundleTools.getString(this, "type", null);
        if (TextUtils.isEmpty(js) || TextUtils.isEmpty(type)) {
            Toasty.error(this, "js或者教务类型未知，结果不可预期！").show();
            goBack();
        } else {
            titleTextView.setText(type);
        }
    }

    private void setOnClick() {
        findViewById(R.id.cv_other).setOnClickListener(v -> save());
        findViewById(R.id.id_back).setOnClickListener(v -> goBack());
    }

    public void save() {
        final String name = nameEdit.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toasty.warning(this, "不可为空", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("前往" + titleTextView.getText())
                    .setMessage("将前往百度查找该校的教务处，请登录自己学校的教务处，看到课表后点击解析按钮即可!")
                    .setPositiveButton("前往教务处", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityTools.toActivity(AdapterSameTypeActivity.this, AdapterSchoolActivity.class,
                                    new BundleModel().setFromClass(AdapterSameTypeActivity.class)
                                            .put("school", name)
                                            .put("url", "https://www.baidu.com/s?ie=UTF-8&wd=" + name + " 教务处")
                                            .put("parsejs", js));
                            AdapterSameTypeActivity.this.finish();
                            if (dialogInterface != null) dialogInterface.dismiss();
                        }
                    }).setNegativeButton("取消", null);
            builder.create().show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }


    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }
}
