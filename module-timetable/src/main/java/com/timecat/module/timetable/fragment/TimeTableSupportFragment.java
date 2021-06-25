package com.timecat.module.timetable.fragment;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.timecat.extend.arms.BaseApplication;
import com.timecat.page.base.base.OnFragmentOpenDrawerListener;
import com.timecat.page.base.friend.toolbar.BaseToolbarSupportFragment;
import com.timecat.identity.readonly.RouterHub;
import com.timecat.component.router.app.NAV;
import com.timecat.layout.ui.entity.TabEntity;
import com.timecat.layout.ui.standard.tablayout.CommonTabLayout;
import com.timecat.layout.ui.standard.tablayout.listener.CustomTabEntity;
import com.timecat.layout.ui.standard.tablayout.listener.OnTabSelectListener;
import com.timecat.module.timetable.R;
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
import com.timecat.module.timetable.event.ReadClipboard;
import com.timecat.module.timetable.event.SwitchPagerEvent;
import com.timecat.module.timetable.event.ToggleWeekViewEvent;
import com.timecat.module.timetable.event.UpdateSchoolEvent;
import com.timecat.module.timetable.event.UpdateTabTextEvent;
import com.timecat.module.timetable.tools.DeviceTools;
import com.timecat.module.timetable.tools.ImportTools;
import com.xiaojinzi.component.anno.FragmentAnno;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

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

/**
 * @author dlink
 * @email linxy59@mail2.sysu.edu.cn
 * @date 2019/5/11
 * @description null
 * @usage null
 */
@FragmentAnno(RouterHub.TIMETABLE_TimeTableSupportFragment)
public class TimeTableSupportFragment extends BaseToolbarSupportFragment {

    final int SUCCESSCODE = 1;

    protected OnFragmentOpenDrawerListener mOpenDraweListener;
    protected Menu menu;
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();

    CommonTabLayout mTab;
    ViewPager mViewPager;
    MyFragmentPagerAdapter mAdapter;
    TextView curWeekText;

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post(new ReadClipboard());
            }
        }, 300);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentOpenDrawerListener) {
            mOpenDraweListener = (OnFragmentOpenDrawerListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOpenDraweListener = null;
    }

    @Override
    protected void onNavIconClick(View v) {
        if (mOpenDraweListener != null) {
            mOpenDraweListener.onOpenDrawer(toolbar);
        }
    }

    protected int getMenuId() {
        return R.menu.timetable_fragment_timetable_menu;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.search) {
            ActivityTools.toActivityWithout(_mActivity, SearchSchoolActivity.class);
            return true;
        }
        return false;
    }

    @NonNull
    public Context getContext() {
        if (_mActivity == null) {
            return BaseApplication.getContext();
        }
        return _mActivity;
    }

    @Override
    protected void bindView(@NotNull View view) {
        super.bindView(view);
        mTab = view.findViewById(R.id.tab);
        mViewPager = view.findViewById(R.id.viewPager);
        curWeekText = view.findViewById(R.id.id_title);
        curWeekText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewPager.getCurrentItem() == 1) {
                    EventBus.getDefault().post(new ToggleWeekViewEvent());
                }
            }
        });
    }

    public void openBindSchoolActivity() {
        NAV.go(RouterHub.TIMETABLE_BindSchoolActivity);
    }

    private void inits() {
        String schoolName = ShareTools.getString(getContext(),
                ShareConstants.STRING_SCHOOL_NAME, null);
        if (schoolName == null) {
            checkIsBindSchool();
        } else {
            EventBus.getDefault().post(new UpdateSchoolEvent(schoolName));
        }
        ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
        if (scheduleName == null) {
            scheduleName = new ScheduleName();
            scheduleName.setName("默认课表");
            scheduleName.setTime(System.currentTimeMillis());
            scheduleName.save();
            ShareTools.put(getContext(), ShareConstants.INT_SCHEDULE_NAME_ID, scheduleName.getId());
        }

        List<Fragment> mFragmentList = new ArrayList<>();
        mFragmentList.add(new FuncFragment());
        mFragmentList.add(new ScheduleFragment());
        mAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);

        mTabEntities.add(new TabEntity("首页", schoolName == null ? "" : schoolName));
        mTabEntities.add(new TabEntity("课表", scheduleName.getName()));
        mTab.setTabData(mTabEntities);

        mTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
                if (position == 1) {
                    EventBus.getDefault().post(new ToggleWeekViewEvent());
                }
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTab.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void checkIsBindSchool() {
        String deviceId = DeviceTools.getDeviceId(getContext());
        if (deviceId == null) {
            return;
        }
        TimetableRequest
                .checkIsBindSchool(getContext(), deviceId, new Callback<ObjResult<CheckBindResultModel>>() {
                    @Override
                    public void onResponse(Call<ObjResult<CheckBindResultModel>> call,
                                           Response<ObjResult<CheckBindResultModel>> response) {
                        if (response == null) {
                            return;
                        }
                        ObjResult<CheckBindResultModel> result = response.body();
                        if (result == null) {
                            return;
                        }
                        if (result.getCode() == 200) {
                            CheckBindResultModel model = result.getData();
                            if (model == null) {
                                return;
                            }
                            if (model.getIsBind() == 1) {
                                ShareTools.putString(getContext(), ShareConstants.STRING_SCHOOL_NAME,
                                        model.getSchool());
                                EventBus.getDefault().post(new UpdateSchoolEvent(model.getSchool()));
                            } else {
                                openBindSchoolActivity();
                            }
                        } else {
                            ToastTools.show(getContext(), result.getMsg());
                        }
                    }

                    @Override
                    public void onFailure(Call<ObjResult<CheckBindResultModel>> call, Throwable t) {
                    }
                });
    }

    public void getFromClip() {
        ClipboardManager cm = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) {
            return;
        }
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
        ClipboardManager cm = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", "");
        cm.setPrimaryClip(mClipData);
    }

    public void getValue(String id) {
        TimetableRequest.getValue(getContext(), id, new Callback<ObjResult<ValuePair>>() {
            @Override
            public void onResponse(Call<ObjResult<ValuePair>> call,
                                   Response<ObjResult<ValuePair>> response) {
                ObjResult<ValuePair> result = response.body();
                if (result != null) {
                    if (result.getCode() == 200) {
                        ValuePair pair = result.getData();
                        if (pair != null) {
                            onImportFromClip(pair);
                        } else {
                            Toasty.error(getContext(), "PutValue:data is null").show();
                        }
                    } else {
                        Toasty.error(getContext(), "PutValue:" + result.getMsg()).show();
                    }
                } else {
                    Toasty.error(getContext(), "PutValue:result is null").show();
                }
            }

            @Override
            public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
            }
        });
    }

    private void showDialogOnImport(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        if (pair == null) {
            return;
        }
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
                        ImportTools.showDialogOnApply(getContext(), newName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.success(getContext(), "Error:" + e.getMessage()).show();
        }
    }

    private void shouldcheckPermission() {
        PermissionGen.with(this)
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //权限申请成功
    @PermissionSuccess(requestCode = SUCCESSCODE)
    public void doSomething() {
        //在这个方法中做一些权限申请成功的事情
        String schoolName = ShareTools
                .getString(getContext(), ShareConstants.STRING_SCHOOL_NAME, null);
        if (schoolName == null) {
            checkIsBindSchool();
        }
    }

    //申请失败
    @PermissionFail(requestCode = SUCCESSCODE)
    public void doFailSomething() {
        ToastTools.show(getContext(), "权限不足，运行中可能会出现故障!请务必开启读取设备信息权限，设备号将作为你的账户");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSwitchPagerEvent(SwitchPagerEvent event) {
        mViewPager.setCurrentItem(1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateSchoolEvent(UpdateSchoolEvent event) {
        if (event != null && event.getSchool() != null) {
            if (mTabEntities != null && mTabEntities.size() > 0) {
                mTabEntities.set(0, new TabEntity("首页", event.getSchool()));
                mTab.setTabData(mTabEntities);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateTabTextEvent(UpdateTabTextEvent event) {
        if (event == null) {
            return;
        }
        if (!TextUtils.isEmpty(event.getText())) {
            curWeekText.setVisibility(View.VISIBLE);
            curWeekText.setText(event.getText());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadClipboard(ReadClipboard event) {
        getFromClip();
    }

    @Override
    protected int layout() {
        return R.layout.timetable_fragment_timetable_support;
    }

    @Override
    public void lazyInit() {
        shouldcheckPermission();
        inits();
    }
}
