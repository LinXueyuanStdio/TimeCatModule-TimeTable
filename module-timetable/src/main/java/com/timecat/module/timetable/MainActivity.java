package com.timecat.module.timetable;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.timecat.identity.readonly.RouterHub;
import com.timecat.module.timetable.activity.BindSchoolActivity;
import com.timecat.module.timetable.activity.adapter.SearchSchoolActivity;
import com.timecat.module.timetable.adapter.MyFragmentPagerAdapter;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.CheckBindResultModel;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.ScheduleName;
import com.timecat.module.timetable.api.model.ShareModel;
import com.timecat.module.timetable.api.model.TimetableModel;
import com.timecat.module.timetable.api.model.ValuePair;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.event.SwitchPagerEvent;
import com.timecat.module.timetable.event.ToggleWeekViewEvent;
import com.timecat.module.timetable.event.UpdateSchoolEvent;
import com.timecat.module.timetable.event.UpdateTabTextEvent;
import com.timecat.module.timetable.fragment.FuncFragment;
import com.timecat.module.timetable.fragment.ScheduleFragment;
import com.timecat.module.timetable.tools.DeviceTools;
import com.timecat.module.timetable.tools.ImportTools;
import com.timecat.module.timetable.tools.ViewTools;
import com.xiaojinzi.component.anno.RouterAnno;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RouterAnno(hostAndPath = RouterHub.TIMETABLE_MainActivity)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    final int SUCCESSCODE = 1;

    ViewPager mViewPager;
    List<Fragment> mFragmentList;
    MyFragmentPagerAdapter mAdapter;

    public static final int REQUEST_IMPORT = 1;

    int toItem = 0;

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        tabTitle1 = (TextView) findViewById(R.id.id_title_tab1);
        tabTitle2 = (TextView) findViewById(R.id.id_title_tab2);
        titleNavView = (View) findViewById(R.id.id_title_nav);
        searchLayout = (LinearLayout) findViewById(R.id.id_search_school);
        schoolTextView = (TextView) findViewById(R.id.id_main_school_text);
        curWeekText = (TextView) findViewById(R.id.id_title);
    }

    TextView tabTitle1;

    TextView tabTitle2;

    View titleNavView;//指示条

    LinearLayout searchLayout;

    RelativeLayout.LayoutParams lp;
    float leftStart = 0;
    float leftEnd = 0;
    int normalTextSize = 15;
    int highlighTextSize = 20;
    int navWidthDip = 16;//指示条宽度
    int titleWidthDip = 40;//Tab宽度
    int marLeftDip = 10;//边距

    //搜索框的边距dp
    int searchViewStartMarDp = 10;
    int searchViewEndMarDp = 80;
    int searchViewStartMar = 0;
    int searchViewEndMar = 0;
    RelativeLayout.LayoutParams searchLayoutParams;
    int searchViewStartMarRightDp = 10;
    int searchViewStartMarRight = 0;

    TextView schoolTextView;

    TextView curWeekText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_main);
//        ViewTools.setTransparent(this);
        ViewTools.setStatusTextGrayColor(this);
        bindView();
        EventBus.getDefault().register(this);
        shouldcheckPermission();
        inits();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Serializable serializable = intent.getSerializableExtra("model");
        if (serializable != null) {
            BundleModel model = (BundleModel) serializable;
            if (model != null) {
                toItem = (int) model.get("item", 0);
                if (mViewPager.getCurrentItem() != toItem) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(0x125);
                        }
                    }, 100);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        }, 300);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                getFromClip();
            }
            if (msg.what == 0x125) {
                select(toItem);
            }
        }
    };

    public void openBindSchoolActivity() {
        Intent intent = new Intent(this, BindSchoolActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.timetable_anim_station_open_activity, R.anim.timetable_anim_station_static);//动画
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
////        super.onRestoreInstanceState(savedInstanceState);
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//    }

    private void inits() {
        String schoolName = ShareTools.getString(MainActivity.this, ShareConstants.STRING_SCHOOL_NAME, null);
        if (schoolName == null) {
            checkIsBindSchool();
        } else {
            EventBus.getDefault().post(new UpdateSchoolEvent(schoolName));
        }

        searchLayoutParams = (RelativeLayout.LayoutParams) searchLayout.getLayoutParams();
        searchViewStartMar = ScreenUtils.dip2px(MainActivity.this, searchViewStartMarDp);
        searchViewEndMar = ScreenUtils.dip2px(MainActivity.this, searchViewEndMarDp);
        searchViewStartMarRight = ScreenUtils.dip2px(MainActivity.this, searchViewStartMarRightDp);
        ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
        if (scheduleName == null) {
            scheduleName = new ScheduleName();
            scheduleName.setName("默认课表");
            scheduleName.setTime(System.currentTimeMillis());
            scheduleName.save();
            ShareTools.put(this, ShareConstants.INT_SCHEDULE_NAME_ID, scheduleName.getId());
        }

        lp = (RelativeLayout.LayoutParams) titleNavView.getLayoutParams();
        lp.width = ScreenUtils.dip2px(this, navWidthDip);
        lp.height = ScreenUtils.dip2px(this, 3);
        leftStart = ScreenUtils.dip2px(MainActivity.this, titleWidthDip / 2 - navWidthDip / 2);
        leftEnd = ScreenUtils.dip2px(MainActivity.this, titleWidthDip + marLeftDip + titleWidthDip / 2 - navWidthDip / 2);

        mViewPager = findViewById(R.id.id_viewpager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: " + position + "&&&" + positionOffset + "&&&" + positionOffsetPixels);

//                marLeft=(int)((screenWidth/3)*(position+positionOffset));
//                tabBottomViewParams.setMargins(marLeft,0,0,0);
//                tabBottomView.setLayoutParams(tabBottomViewParams);

                //20dp:Tab宽度的一半，8dp:指示条宽度的一半
                float marLeft = leftStart + (leftEnd - leftStart) * (position + positionOffset);
                lp.setMargins((int) marLeft, 0, 0, 0);
                titleNavView.setLayoutParams(lp);


                float newMar = searchViewStartMar + (searchViewEndMar - searchViewStartMar) * (position + positionOffset);
                searchLayoutParams.setMargins((int) newMar, 0, searchViewStartMarRight, 0);
                searchLayout.setLayoutParams(searchLayoutParams);
            }

            @Override
            public void onPageSelected(int position) {
                updateTabStatus(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FuncFragment());
        mFragmentList.add(new ScheduleFragment());
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
        int item = (int) BundleTools.getInt(this, "item", 0);
        select(item);
    }

    private void checkIsBindSchool() {
        String deviceId = DeviceTools.getDeviceId(this);
        if (deviceId == null) return;
        TimetableRequest.checkIsBindSchool(this, deviceId, new Callback<ObjResult<CheckBindResultModel>>() {
            @Override
            public void onResponse(Call<ObjResult<CheckBindResultModel>> call, Response<ObjResult<CheckBindResultModel>> response) {
                if (response == null) return;
                ObjResult<CheckBindResultModel> result = response.body();
                if (result == null) return;
                if (result.getCode() == 200) {
                    CheckBindResultModel model = result.getData();
                    if (model == null) return;
                    if (model.getIsBind() == 1) {
                        ShareTools.putString(MainActivity.this, ShareConstants.STRING_SCHOOL_NAME, model.getSchool());
                        EventBus.getDefault().post(new UpdateSchoolEvent(model.getSchool()));
                    } else {
                        openBindSchoolActivity();
                    }
                } else {
                    ToastTools.show(MainActivity.this, result.getMsg());
                }
            }

            @Override
            public void onFailure(Call<ObjResult<CheckBindResultModel>> call, Throwable t) {
            }
        });
    }

    public void getFromClip() {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        if (data != null) {
            if (data.getItemCount() > 0) {
                ClipData.Item item = data.getItemAt(0);
                if (item.getText() != null) {
                    String content = item.getText().toString();
                    if (!TextUtils.isEmpty(content)) {
                        int index = content.indexOf("#");
                        if (index != -1 && content.indexOf("时光猫") != -1) {
                            if (content.length() > index + 1) {
                                String id = content.substring(index + 1);
                                showDialogOnImport(id);
                                clearClip();
                            }
                        }
                    }
                }
            }
        }
    }

    public void clearClip() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", "");
        cm.setPrimaryClip(mClipData);
    }

    public void getValue(String id) {
        TimetableRequest.getValue(this, id, new Callback<ObjResult<ValuePair>>() {
            @Override
            public void onResponse(Call<ObjResult<ValuePair>> call, Response<ObjResult<ValuePair>> response) {
                ObjResult<ValuePair> result = response.body();
                if (result != null) {
                    if (result.getCode() == 200) {
                        ValuePair pair = result.getData();
                        if (pair != null) {
                            onImportFromClip(pair);
                        } else {
                            Toasty.error(MainActivity.this, "PutValue:data is null").show();
                        }
                    } else {
                        Toasty.error(MainActivity.this, "PutValue:" + result.getMsg()).show();
                    }
                } else {
                    Toasty.error(MainActivity.this, "PutValue:result is null").show();
                }
            }

            @Override
            public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
            }
        });
    }

    private void showDialogOnImport(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("有人给你分享了课表，是否导入?")
                .setTitle("导入分享")
                .setPositiveButton("导入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getValue(id);
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    public void onImportFromClip(ValuePair pair) {
        if (pair == null) return;
        ScheduleName newName = new ScheduleName();
        SimpleDateFormat sdf = new SimpleDateFormat("导入-HHmm");
        newName.setName(sdf.format(new Date()));
        newName.setTime(System.currentTimeMillis());
        newName.save();

        try {
            ShareModel shareModel = new Gson().fromJson(pair.getValue(), ShareModel.class);
            if (shareModel != null) {
                if (shareModel.getType() == ShareModel.TYPE_PER_TABLE) {
                    List<TimetableModel> list = shareModel.getData();
                    List<TimetableModel> finalList = new ArrayList<>();
                    if (list != null) {
                        for (TimetableModel m : list) {
                            TimetableModel model = new TimetableModel();
                            model.setScheduleName(newName);
                            model.setName(m.getName());
                            model.setTeacher(m.getTeacher());
                            model.setStep(m.getStep());
                            model.setDay(m.getDay());
                            model.setStart(m.getStart());
                            model.setWeeks(m.getWeeks());
                            model.setWeekList(m.getWeekList());
                            model.setRoom(m.getRoom());
                            model.setMajor(m.getMajor());
                            model.setTerm(m.getTerm());
                            finalList.add(model);
                        }
                        DataSupport.saveAll(finalList);
                        ImportTools.showDialogOnApply(MainActivity.this, newName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.success(this, "Error:" + e.getMessage()).show();
        }
    }

    public void select(int i) {
        int lightColor = Color.RED;
        titleNavView.setBackgroundColor(lightColor);
        updateTabStatus(i);
        switch (i) {
            case 0:
                mViewPager.setCurrentItem(0);
                break;
            case 1:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    public void updateTabStatus(int i) {
        initTabs();
        int lightColor = Color.RED;
        titleNavView.setBackgroundColor(lightColor);
        switch (i) {
            case 0:
                tabTitle1.setTextSize(highlighTextSize);
                lp.setMargins((int) leftStart, 0, 0, 0);
                break;
            case 1:
                curWeekText.setVisibility(View.VISIBLE);
                tabTitle2.setTextSize(highlighTextSize);
                lp.setMargins((int) leftEnd, 0, 0, 0);
                break;
        }
    }

    private void initTabs() {
        int defaultColor = Color.BLACK;
        tabTitle1.setTextColor(defaultColor);
        tabTitle2.setTextColor(defaultColor);

        tabTitle1.setTextSize(normalTextSize);
        tabTitle2.setTextSize(normalTextSize);

        curWeekText.setVisibility(View.GONE);
    }

    private void shouldcheckPermission() {
        PermissionGen.with(MainActivity.this)
                .addRequestCode(SUCCESSCODE)
                .permissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.READ_PHONE_STATE
                )
                .request();
    }

    //申请权限结果的返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //权限申请成功
    @PermissionSuccess(requestCode = SUCCESSCODE)
    public void doSomething() {
        //在这个方法中做一些权限申请成功的事情
        String schoolName = ShareTools.getString(MainActivity.this, ShareConstants.STRING_SCHOOL_NAME, null);
        if (schoolName == null) {
            checkIsBindSchool();
        }
    }

    //申请失败
    @PermissionFail(requestCode = SUCCESSCODE)
    public void doFailSomething() {
        ToastTools.show(this, "权限不足，运行中可能会出现故障!请务必开启读取设备信息权限，设备号将作为你的账户");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSwitchPagerEvent(SwitchPagerEvent event) {
        mViewPager.setCurrentItem(1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateSchoolEvent(UpdateSchoolEvent event) {
        if (event != null && event.getSchool() != null) {
            schoolTextView.setText(event.getSchool());
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
            finish();
        }
    }


    private void setOnClick() {
        findViewById(R.id.id_title_tab1).setOnClickListener(v -> onSelectedTab1());
        findViewById(R.id.id_title_tab2).setOnClickListener(v -> onSelectedTab2());
        findViewById(R.id.id_title).setOnClickListener(v -> onCurWeekTextClicked());
        findViewById(R.id.id_search_school).setOnClickListener(v -> toSearchSchool());
    }

    public void onSelectedTab1() {
        select(0);
    }


    public void onSelectedTab2() {
        select(1);
    }


    public void onCurWeekTextClicked() {
        if (mViewPager.getCurrentItem() == 1) {
            EventBus.getDefault().post(new ToggleWeekViewEvent());
        }
    }


    public void toSearchSchool() {
        ActivityTools.toActivityWithout(this, SearchSchoolActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateTabTextEvent(UpdateTabTextEvent event) {
        if (event == null) return;
        if (!TextUtils.isEmpty(event.getText())) {
            curWeekText.setText(event.getText());
        }
    }
}
