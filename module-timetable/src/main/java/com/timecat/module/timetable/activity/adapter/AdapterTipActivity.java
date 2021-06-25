package com.timecat.module.timetable.activity.adapter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.CheckModel;
import com.timecat.module.timetable.api.model.ObjResult;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterTipActivity extends AppCompatActivity {

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        schoolEdit = (EditText) findViewById(R.id.id_school_edittext);
        urlEdit = (EditText) findViewById(R.id.id_url_edittext);
        nameText = (TextView) findViewById(R.id.tv_name);
    }

    public EditText schoolEdit;
    public EditText urlEdit;
    TextView nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_adapter_tip);
        bindView();
        schoolEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && charSequence.length() >= 2) {
                    check(charSequence.toString());
                } else {
                    nameText.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void setOnClick() {
        findViewById(R.id.cv_adapter).setOnClickListener(v -> onAdapterBtnClicked());
        findViewById(R.id.tv_name).setOnClickListener(v -> onNameTextClicked());
        findViewById(R.id.ib_back).setOnClickListener(v -> goBack());
    }

    public void onAdapterBtnClicked() {
        final String school = schoolEdit.getText().toString();
        final String url = urlEdit.getText().toString();
        if (TextUtils.isEmpty(school) || TextUtils.isEmpty(url)) {
            Toasty.warning(this, "不允许为空，请填充完整!").show();
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toasty.warning(this, "请填写正确的url，以http://或https://开头").show();
            return;
        } else {
            if (!school.endsWith("学校") && !school.endsWith("学院") && !school.endsWith("大学")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("校名不太对哟")
                        .setMessage("你的校名好像不太对哟，务必填写全称")
                        .setPositiveButton("确定是对的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityTools.toActivity(AdapterTipActivity.this, UploadHtmlActivity.class,
                                        new BundleModel()
                                                .put("url", url)
                                                .put("school", school));
                                AdapterTipActivity.this.finish();
                            }
                        })
                        .setNegativeButton("取消", null);
                builder.create().show();
            } else {
                ActivityTools.toActivity(AdapterTipActivity.this, UploadHtmlActivity.class,
                        new BundleModel()
                                .put("url", url)
                                .put("school", school));
                AdapterTipActivity.this.finish();
            }

        }
    }

    public void check(String school) {
        TimetableRequest.checkSchool(this, school, new Callback<ObjResult<CheckModel>>() {
            @Override
            public void onResponse(Call<ObjResult<CheckModel>> call, Response<ObjResult<CheckModel>> response) {
                ObjResult<CheckModel> result = response.body();
                if (result == null) {
                    Toasty.error(AdapterTipActivity.this, "result is null").show();
                } else if (result.getCode() != 200) {
                    Toasty.error(AdapterTipActivity.this, result.getMsg()).show();
                } else {
                    CheckModel model = result.getData();
                    if (model != null) {
                        if (model.getHave() == 1 && !TextUtils.isEmpty(model.getUrl()) && !TextUtils.isEmpty(model.getName())) {
                            urlEdit.setText(model.getUrl() == null ? "" : model.getUrl());
                            nameText.setVisibility(View.VISIBLE);
                            nameText.setText("推荐:" + model.getName());
                        } else {
                            nameText.setVisibility(View.INVISIBLE);
                            urlEdit.setText("");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ObjResult<CheckModel>> call, Throwable t) {
            }
        });
    }


    public void onNameTextClicked() {
        String val = nameText.getText().toString();
        if (val != null && val.length() > 3) {
            val = val.substring(3);
        }
        if (!TextUtils.isEmpty(val)) schoolEdit.setText(val);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }


    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }
}
