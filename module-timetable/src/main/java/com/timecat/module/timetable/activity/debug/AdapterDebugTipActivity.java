package com.timecat.module.timetable.activity.debug;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.UserDebugModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterDebugTipActivity extends AppCompatActivity {

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        nameEdit = (EditText) findViewById(R.id.id_name_edittext);
        userkeydit = (EditText) findViewById(R.id.id_userkey_edittext);
    }

    public EditText nameEdit;


    public EditText userkeydit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_adapter_debug_tip);
        bindView();
        if (isHasLocal()) {
            ActivityTools.toActivity(AdapterDebugTipActivity.this,
                    AdapterDebugListActivity.class);
        }
    }

    private void setOnClick() {
        findViewById(R.id.cv_login).setOnClickListener(v -> onAdapterBtnClicked());
        findViewById(R.id.ib_back).setOnClickListener(v -> goBack());
    }

    public void onAdapterBtnClicked() {
        final String name = nameEdit.getText().toString();
        final String userkey = userkeydit.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(userkey)) {
            Toasty.warning(this, "不允许为空，请填充完整!").show();
        } else {
            TimetableRequest.getUserInfo(this, name, userkey,
                    new Callback<ObjResult<UserDebugModel>>() {
                        @Override
                        public void onResponse(Call<ObjResult<UserDebugModel>> call, Response<ObjResult<UserDebugModel>> response) {
                            ObjResult<UserDebugModel> result = response.body();
                            if (result != null) {
                                if (result.getCode() == 200) {
                                    saveToLocal(name, userkey);
                                    ActivityTools.toActivity(AdapterDebugTipActivity.this,
                                            AdapterDebugListActivity.class);
                                } else {
                                    clearLocal();
                                    Toasty.error(AdapterDebugTipActivity.this, result.getMsg()).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ObjResult<UserDebugModel>> call, Throwable t) {
                            Toasty.error(AdapterDebugTipActivity.this, t.getMessage()).show();
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }

    boolean isHasLocal() {
        String name = ShareTools.getString(this, "debug_name", null);
        String userkey = ShareTools.getString(this, "debug_userkey", null);
        if (name != null && userkey != null) return true;
        return false;
    }

    void clearLocal() {
        ShareTools.putString(this, "debug_name", null);
        ShareTools.putString(this, "debug_userkey", null);
    }

    void saveToLocal(String name, String userkey) {
        ShareTools.putString(this, "debug_name", name);
        ShareTools.putString(this, "debug_userkey", userkey);
    }
}
