package com.timecat.module.timetable.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.timecat.module.timetable.R;
import com.timecat.module.timetable.activity.schedule.AddTimetableActivity;
import com.timecat.module.timetable.activity.schedule.TimetableDetailActivity;
import com.timecat.module.timetable.api.model.ScheduleName;
import com.timecat.module.timetable.api.model.TimetableModel;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.event.ConfigChangeEvent;
import com.timecat.module.timetable.event.ToggleWeekViewEvent;
import com.timecat.module.timetable.event.UpdateScheduleEvent;
import com.timecat.module.timetable.event.UpdateTabTextEvent;
import com.timecat.module.timetable.model.ScheduleDao;
import com.timecat.module.timetable.theme.IThemeView;
import com.timecat.module.timetable.theme.MyThemeLoader;
import com.timecat.module.timetable.timetable_custom.CustomWeekView;
import com.timecat.module.timetable.tools.BroadcastUtils;
import com.timecat.module.timetable.tools.TimetableTools;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleFragment extends LazyLoadFragment implements IThemeView {

    private static final String TAG = "MainActivity";

    private Activity context;


    private void bindView(View view) {
        findView(view);
        setOnClick(view);
    }

    private void findView(View view) {
        mTimetableView = (TimetableView) view.findViewById(R.id.id_timetableView);
        mWeekView = (CustomWeekView) view.findViewById(R.id.id_weekview);
        containerLayout = (LinearLayout) view.findViewById(R.id.container);
        mTitleTextView = (TextView) view.findViewById(R.id.id_title);
        mCurScheduleTextView = (TextView) view.findViewById(R.id.id_schedulename);
        menuImageView = (ImageView) view.findViewById(R.id.id_main_menu);
        loadLayout = (LinearLayout) view.findViewById(R.id.id_loadlayout);
    }

    public TimetableView mTimetableView;


    public CustomWeekView mWeekView;

    private List<Schedule> schedules;

    public Activity getContext() {
        return context;
    }

    int target;


    LinearLayout containerLayout;

    private View mView;


    public TextView mTitleTextView;


    public TextView mCurScheduleTextView;


    ImageView menuImageView;

    public static final int REQUEST_IMPORT = 1;


    LinearLayout loadLayout;

    int tmp = 1;

    MyThemeLoader mThemeLoader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.timetable_fragment_schedule, container, false);
        EventBus.getDefault().register(this);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
        inits();
        adjustAndGetData();
    }

    @Override
    public void onResume() {
        super.onResume();
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
                try {
                    mTimetableView.onDateBuildListener().onHighLight();
                    int newCurWeek = TimetableTools.getCurWeek(context);
                    if (newCurWeek != mTimetableView.curWeek()) {
                        mTimetableView.onDateBuildListener().onUpdateDate(mTimetableView.curWeek(), newCurWeek);
                        mTimetableView.changeWeekForce(newCurWeek);
                        mWeekView.curWeek(newCurWeek).updateView();
                    }
                } catch (Exception e) {
                }
            }
        }
    };

    private void inits() {
        menuImageView.setColorFilter(Color.WHITE);
        menuImageView.setVisibility(View.VISIBLE);
        context = getActivity();
        schedules = new ArrayList<>();
        mThemeLoader = new MyThemeLoader(this);

        int id = ScheduleDao.getApplyScheduleId(context);
        ScheduleName scheduleName = DataSupport.find(ScheduleName.class, id);
        if (scheduleName != null) {
            mCurScheduleTextView.setText(scheduleName.getName());
        } else {
            mCurScheduleTextView.setText("默认课表");
        }

        int curWeek = TimetableTools.getCurWeek(context);
        tmp = curWeek;


        //设置周次选择属性
        mWeekView.data(schedules)
                .curWeek(curWeek)
                .itemCount(25)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        tmp = week;
                        EventBus.getDefault().post(new UpdateTabTextEvent("第" + week + "周"));
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);
                    }
                })
                .callback(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false)
                .showView();

        int status = ShareTools.getInt(context, "hidenotcur", 0);
        if (status == 0) {
            mTimetableView.isShowNotCurWeek(true);
        } else {
            mTimetableView.isShowNotCurWeek(false);
        }

        int status2 = ShareTools.getInt(context, "hideweekends", 0);
        if (status2 == 0) {
            mTimetableView.isShowWeekends(true);
        } else {
            mTimetableView.isShowWeekends(false);
        }

        mTimetableView.curWeek(curWeek)
                .maxSlideItem(12)
                .itemHeight(ScreenUtils.dip2px(context, 50))
//                .callback(new CalenderDateBuildAdapter(context))
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        BundleModel model = new BundleModel();
                        model.put("timetable", scheduleList);
                        model.setFromClass(getActivity().getClass());
                        model.put("item", 1);
                        ActivityTools.toActivityWithout(getContext(), TimetableDetailActivity.class, model);
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        EventBus.getDefault().post(new UpdateTabTextEvent("第" + curWeek + "周"));
                        mTitleTextView.setText("第" + curWeek + "周");
                        tmp = curWeek;
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        BundleModel model = new BundleModel();
                        model.setFromClass(getActivity().getClass())
                                .put(AddTimetableActivity.KEY_DAY, day)
                                .put(AddTimetableActivity.KEY_START, start);
                        ActivityTools.toActivityWithout(getContext(), AddTimetableActivity.class, model);
                    }
                })
                .callback(new ISchedule.OnFlaglayoutClickListener() {
                    @Override
                    public void onFlaglayoutClick(int day, int start) {
                        mTimetableView.hideFlaglayout();
                        BundleModel model = new BundleModel();
                        model.setFromClass(getActivity().getClass())
                                .put(AddTimetableActivity.KEY_DAY, day + 1)
                                .put(AddTimetableActivity.KEY_START, start);
                        ActivityTools.toActivityWithout(getContext(), AddTimetableActivity.class, model);
                    }
                })
                .showView();
        loadLayout.setVisibility(View.GONE);
        mThemeLoader.execute();
    }

    /**
     * 周次选择布局的左侧被点击时回调
     */
    protected void onWeekLeftLayoutClicked() {
        final String items[] = new String[25];
        for (int i = 0; i < 25; i++) {
            items[i] = "第" + (i + 1) + "周";
        }
        target = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.curWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target = i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target != -1) {
                    mWeekView.curWeek(target + 1).updateView();
                    mWeekView.scrollToIndex(target);
                    mTimetableView.changeWeekForce(target + 1);
                    ShareTools.putString(getContext(), ShareConstants.STRING_START_TIME, TimetableTools.getStartSchoolTime(target + 1));
                    BroadcastUtils.refreshAppWidget(context);
                    EventBus.getDefault().post(new UpdateScheduleEvent());
                    ToastTools.show(getContext(), "当前周:" + (target + 1) + "\n开学时间:" + TimetableTools.getStartSchoolTime(target + 1));
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private void adjustAndGetData() {
        int id = ScheduleDao.getApplyScheduleId(context);
        ScheduleName scheduleName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
        if (scheduleName == null) {
            scheduleName = new ScheduleName();
            scheduleName.setName("默认课表");
            scheduleName.setTime(System.currentTimeMillis());
            scheduleName.save();
            id = scheduleName.getId();
            ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, id);
        }

        if (scheduleName == null) return;
        ScheduleName newName = DataSupport.find(ScheduleName.class, id);
        if (newName == null) return;
        FindMultiExecutor executor = newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> dataModels = (List<TimetableModel>) t;
                if (dataModels != null) {
                    mTimetableView.data(ScheduleSupport.transform(dataModels)).updateView();
                    mWeekView.data(ScheduleSupport.transform(dataModels)).showView();
                }
            }
        });
    }


    private void setOnClick(View view) {
        view.findViewById(R.id.id_main_menu).setOnClickListener(v -> showPopMenu());
    }

    public void showPopMenu() {
        //创建弹出式菜单对象（最低版本11）
        PopupMenu popup = new PopupMenu(context, menuImageView);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.timetable_main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.id_menu2) {
                    ActivityTools.toActivityWithout(context, AddTimetableActivity.class);

                }
                return false;
            }
        });
        popup.show(); //这一行代码不要忘记了
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigChangeEvent(ConfigChangeEvent event) {
        int status = ShareTools.getInt(context, "hidenotcur", 0);
        if (status == 0) {
            mTimetableView.isShowNotCurWeek(true);
        } else {
            mTimetableView.isShowNotCurWeek(false);
        }

        int status2 = ShareTools.getInt(context, "hideweekends", 0);
        if (status2 == 0) {
            mTimetableView.isShowWeekends(true);
        } else {
            mTimetableView.isShowWeekends(false);
        }
        mTimetableView.updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateScheduleEvent(UpdateScheduleEvent event) {
        ScheduleName newName = DataSupport.find(ScheduleName.class, ScheduleDao.getApplyScheduleId(getActivity()));
        if (newName == null) return;
        final int curWeek = TimetableTools.getCurWeek(context);
        UpdateTabTextEvent updateTabTextEvent = new UpdateTabTextEvent();
        updateTabTextEvent.setText("第" + curWeek + "周");
        EventBus.getDefault().post(updateTabTextEvent);
        mCurScheduleTextView.setText(newName.getName());
        FindMultiExecutor executor = newName.getModelsAsync();
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<TimetableModel> dataModels = (List<TimetableModel>) t;
                if (dataModels != null) {
                    mTimetableView.curWeek(curWeek).data(ScheduleSupport.transform(dataModels)).updateView();
                    mWeekView.curWeek(curWeek).data(ScheduleSupport.transform(dataModels)).showView();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToggleWeekViewEvent(ToggleWeekViewEvent event) {
        if (mWeekView.isShowing()) {
            mWeekView.isShow(false);
            mTimetableView.changeWeekForce(mTimetableView.curWeek());
            mTimetableView.onDateBuildListener().onUpdateDate(tmp, mTimetableView.curWeek());
        } else {
            mWeekView.isShow(true);
            mWeekView.scrollToIndex(mTimetableView.curWeek() - 1);
        }
    }

    @Override
    public TimetableView getTimetableView() {
        return mTimetableView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
