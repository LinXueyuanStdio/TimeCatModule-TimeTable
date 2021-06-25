package com.timecat.module.timetable.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.activity.hpu.ImportMajorActivity;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.SchoolPersonModel;
import com.timecat.module.timetable.api.model.TimetableModel;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.event.ConfigChangeEvent;
import com.timecat.module.timetable.event.UpdateBindDataEvent;
import com.timecat.module.timetable.tools.BroadcastUtils;
import com.timecat.module.timetable.tools.DeviceTools;
import com.timecat.module.timetable.tools.WidgetConfig;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    Activity context;

    LinearLayout backLayout;

    public static final int REQUEST_IMPORT = 1;

    boolean changeStatus = false;
    boolean changeStatus2 = false;


    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        hideNotCurSwitch = (SwitchCompat) findViewById(R.id.id_switch_hidenotcur);
        hideWeekendsSwitch = (SwitchCompat) findViewById(R.id.id_switch_hideweekends);
        checkedAutoSwitch = (SwitchCompat) findViewById(R.id.id_checkauto);
        max15Switch = (SwitchCompat) findViewById(R.id.id_widget_max15);
        hideWeeksSwitch = (SwitchCompat) findViewById(R.id.id_widget_hideweeks);
        hideDateSwitch = (SwitchCompat) findViewById(R.id.id_widget_hidedate);
        showQinglvSwitch = (SwitchCompat) findViewById(R.id.id_show_qinglv);
        deviceText = (TextView) findViewById(R.id.id_device_text);
        schoolText = (TextView) findViewById(R.id.id_school_text);
        personCountText = (TextView) findViewById(R.id.id_school_count_text);
    }

    SwitchCompat hideNotCurSwitch;
    SwitchCompat hideWeekendsSwitch;
    SwitchCompat checkedAutoSwitch;
    SwitchCompat max15Switch;
    SwitchCompat hideWeeksSwitch;
    SwitchCompat hideDateSwitch;
    SwitchCompat showQinglvSwitch;
    TextView deviceText;
    TextView schoolText;
    TextView personCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_menu);
        bindView();
        inits();
    }

    private void inits() {
        context = this;

        String deviceId = DeviceTools.getDeviceId(this);
        if (deviceId != null) {
            if (deviceId.length() >= 8) {
                deviceText.setText("UID:" + deviceId.substring(deviceId.length() - 8));
            } else {
                deviceText.setText("UID:" + deviceId);
            }
        } else {
            deviceText.setText("设备号获取失败");
        }

        backLayout = findViewById(R.id.id_back);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        int hide = ShareTools.getInt(this, "hidenotcur", 0);
        if (hide == 0) {
            hideNotCurSwitch.setChecked(false);
        } else {
            hideNotCurSwitch.setChecked(true);
        }

        int show = ShareTools.getInt(this, ShareConstants.INT_GUANLIAN, 1);
        if (show == 1) {
            showQinglvSwitch.setChecked(true);
        } else {
            showQinglvSwitch.setChecked(false);
        }

        int alpha = ShareTools.getInt(this, "hideweekends", 0);
        if (alpha == 0) {
            hideWeekendsSwitch.setChecked(false);
        } else {
            hideWeekendsSwitch.setChecked(true);
        }

        int isIgnoreUpdate = ShareTools.getInt(this, "isIgnoreUpdate", 0);
        if (isIgnoreUpdate == 0) {
            checkedAutoSwitch.setChecked(true);
        } else {
            checkedAutoSwitch.setChecked(false);
        }

        boolean maxItem = WidgetConfig.get(this, WidgetConfig.CONFIG_MAX_ITEM);
        max15Switch.setChecked(maxItem);

        boolean hideWeeks = WidgetConfig.get(this, WidgetConfig.CONFIG_HIDE_WEEKS);
        hideWeeksSwitch.setChecked(hideWeeks);

        boolean hideDate = WidgetConfig.get(this, WidgetConfig.CONFIG_HIDE_DATE);
        hideDateSwitch.setChecked(hideDate);

        String schoolName = ShareTools.getString(MenuActivity.this, ShareConstants.STRING_SCHOOL_NAME, null);
        if (schoolName == null) {
            schoolText.setText("未关联学校");
        } else {
            schoolText.setText(schoolName);
            getSchoolPersonCount(schoolName);
        }
    }

    public Activity getContext() {
        return context;
    }

    public void clearData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空数据")
                .setMessage("确认后将删除本地保存的所有课程数据且无法恢复！请谨慎操作")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ShareTools.clear(getContext());
                        DataSupport.deleteAll(TimetableModel.class);
                        Intent intent = new Intent(getContext(), ImportMajorActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);
                        getContext().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);//动画
                        getContext().finish();
                    }
                })
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    public void getSchoolPersonCount(final String school) {
        TimetableRequest.getSchoolPersonCount(this, school, new Callback<ObjResult<SchoolPersonModel>>() {
            @Override
            public void onResponse(Call<ObjResult<SchoolPersonModel>> call, Response<ObjResult<SchoolPersonModel>> response) {
                if (response == null) return;
                ObjResult<SchoolPersonModel> result = response.body();
                if (result.getCode() == 200) {
                    SchoolPersonModel schoolPersonModel = result.getData();
                    if (schoolPersonModel != null) {
                        personCountText.setText(schoolPersonModel.getCount() + "名校友");
                    }
                } else {
                    Toast.makeText(MenuActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ObjResult<SchoolPersonModel>> call, Throwable t) {
            }
        });
    }

//    @OnClick(R2.id.id_menu_about)
//    public void about() {
//        ActivityTools.toActivity(MenuActivity.this, AboutActivity.class);
//        finish();
//    }
//
//    @OnClick(R2.id.id_menu_update2)
//    public void issues() {
//
//    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(getContext(), MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void onHideNotCurSwitchClicked(boolean b) {
        changeStatus = true;
        if (b) {
            ShareTools.putInt(this, "hidenotcur", 1);
        } else {
            ShareTools.putInt(this, "hidenotcur", 0);
        }
    }

    public void onHideWeekendsSwitchClicked(boolean b) {
        changeStatus = true;
        if (b) {
            ShareTools.putInt(this, "hideweekends", 1);
        } else {
            ShareTools.putInt(this, "hideweekends", 0);
        }
    }

    public void onShowQinglvSwitchClicked(boolean b) {
        changeStatus2 = true;
        if (b) {
            ShareTools.putInt(this, ShareConstants.INT_GUANLIAN, 1);
        } else {
            ShareTools.putInt(this, ShareConstants.INT_GUANLIAN, 0);
        }
    }

    public void onCheckedAutoSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "isIgnoreUpdate", 0);
        } else {
            ShareTools.putInt(this, "isIgnoreUpdate", 1);
        }
    }

    public void onCheckedHideWeeksSwitchClicked(boolean b) {
        WidgetConfig.apply(this, WidgetConfig.CONFIG_HIDE_WEEKS, b);
        BroadcastUtils.refreshAppWidget(this);
    }

    public void onCheckedMax15SwitchClicked(boolean b) {
        WidgetConfig.apply(this, WidgetConfig.CONFIG_MAX_ITEM, b);
        BroadcastUtils.refreshAppWidget(this);
    }

    public void onCheckedHideDateSwitchClicked(boolean b) {
        WidgetConfig.apply(this, WidgetConfig.CONFIG_HIDE_DATE, b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (changeStatus) {
            EventBus.getDefault().post(new ConfigChangeEvent());
        }
        if (changeStatus2) {
            EventBus.getDefault().post(new UpdateBindDataEvent());
        }
    }

    private void setOnClick() {
        findViewById(R.id.id_menu_modify_school).setOnClickListener(v -> onModifyButtonClicked());
        hideNotCurSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onHideNotCurSwitchClicked(isChecked);
            }
        });
        hideWeekendsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onHideWeekendsSwitchClicked(isChecked);
            }
        });
        checkedAutoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedAutoSwitchClicked(isChecked);
            }
        });
        max15Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedMax15SwitchClicked(isChecked);
            }
        });
        hideWeeksSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedHideWeeksSwitchClicked(isChecked);
            }
        });
        hideDateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedHideDateSwitchClicked(isChecked);
            }
        });
        showQinglvSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onShowQinglvSwitchClicked(isChecked);
            }
        });
    }

    public void onModifyButtonClicked() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("修改学校")
                .setMessage("你可以修改本设备关联的学校，学校信息将作为筛选服务的重要依据")
                .setPositiveButton("修改学校", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openBindSchoolActivity();
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    public void openBindSchoolActivity() {
        Intent intent = new Intent(this, BindSchoolActivity.class);
        intent.putExtra(BindSchoolActivity.FINISH_WHEN_NON_NULL, 0);
        startActivity(intent);
        overridePendingTransition(R.anim.timetable_anim_station_open_activity, R.anim.timetable_anim_station_static);//动画
        finish();
    }
}
