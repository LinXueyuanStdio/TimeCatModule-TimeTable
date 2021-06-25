package com.timecat.module.timetable.activity.schedule;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.adapter.MultiScheduleAdapter;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.ObjResult;
import com.timecat.module.timetable.api.model.ScheduleName;
import com.timecat.module.timetable.api.model.ShareModel;
import com.timecat.module.timetable.api.model.TimetableModel;
import com.timecat.module.timetable.api.model.ValuePair;
import com.timecat.module.timetable.constants.ExtrasConstants;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.event.UpdateScheduleEvent;
import com.timecat.module.timetable.model.ScheduleDao;
import com.timecat.module.timetable.tools.BroadcastUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.OnItemClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiScheduleActivity extends Activity {
    private static final String TAG = "MultiScheduleFragment";


    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        listView = (ListView) findViewById(R.id.id_multi_listview);
        titleTextView = (TextView) findViewById(R.id.id_title);
        loadLayout = (LinearLayout) findViewById(R.id.id_loadlayout);
    }

    ListView listView;
    MultiScheduleAdapter adapter;
    List<ScheduleName> nameList;
//    List<Integer> scheduleCounts;


    TextView titleTextView;

    private Activity context;

    public LinearLayout loadLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_multi_schedule);
        bindView();
        inits();
        getData();
    }

    private void inits() {
        context = this;
        nameList = new ArrayList<>();
        adapter = new MultiScheduleAdapter(context, nameList);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
    }

    public void apply(ScheduleName scheduleName) {
        if (scheduleName == null) return;
        int id = scheduleName.getId();
        ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, id);
        BroadcastUtils.refreshAppWidget(MultiScheduleActivity.this);
        ScheduleDao.changeStatus(MultiScheduleActivity.this, true);
        EventBus.getDefault().post(new UpdateScheduleEvent());
        Toasty.success(context, "切换课表成功").show();
    }

    private void deleteScheduleName(final ScheduleName scheduleName) {
        if (scheduleName == null) return;
        if (scheduleName.getName().equals("默认课表")) {
            Toasty.error(context, "默认课表，不允许删除").show();
            return;
        }
        if (scheduleName.getModels() == null || scheduleName.getModels().size() == 0) {
            scheduleName.delete();
            getData();
            Toasty.success(context, "删除成功").show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("删除[" + scheduleName.getName() + "]")
                    .setMessage("本课表下有课，是否确认删除？")
                    .setPositiveButton("确认删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            scheduleName.delete();
                            int cur = ShareTools.getInt(context, ShareConstants.INT_SCHEDULE_NAME_ID, -1);
                            if (cur == scheduleName.getId()) {
                                ScheduleDao.changeFuncStatus(MultiScheduleActivity.this, true);
                                ScheduleName newName = DataSupport.where("name=?", "默认课表").findFirst(ScheduleName.class);
                                if (newName != null) {
                                    ShareTools.put(context, ShareConstants.INT_SCHEDULE_NAME_ID, newName.getId());
                                }
                                EventBus.getDefault().post(new UpdateScheduleEvent());
                            }
                            getData();
                            Toasty.success(context, "删除成功").show();
                            ScheduleDao.changeStatus(MultiScheduleActivity.this, true);
                            dialog.dismiss();
                        }
                    }).setNegativeButton("取消", null);

            builder.create().show();
        }
    }


    public void showListDialog(final int pos) {
        final String items[] = {"课程管理", "修改课表名", "删除本课表", "分享本课表", "设置为当前课表"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择操作");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        ActivityTools.toActivity(context,
                                TimetableManagerActivity.class,
                                new BundleModel()
                                        .put(ExtrasConstants.INT_SCHEDULE_NAME_ID,
                                                nameList.get(pos).getId())
                                        .put(ExtrasConstants.STRING_SCHEDULE_NAME, nameList.get(pos).getName()));
                        break;
                    case 1:
                        modifyScheduleName(nameList.get(pos));
                        break;
                    case 2:
                        deleteScheduleName(nameList.get(pos));
                        break;
                    case 3:
                        Toasty.info(MultiScheduleActivity.this, "正在上传..").show();
                        ScheduleName newName = nameList.get(pos);
                        if (newName != null) {
                            List<TimetableModel> modelList = ScheduleDao.getAllWithScheduleId(newName.getId());
                            ShareModel shareModel = new ShareModel();
                            shareModel.setType(ShareModel.TYPE_PER_TABLE);
                            shareModel.setData(modelList);
                            String value = new Gson().toJson(shareModel);
                            putValue(value);
                        }
                        break;
                    case 4:
                        ScheduleDao.changeFuncStatus(MultiScheduleActivity.this, true);
                        apply(nameList.get(pos));
                        getData();
                        BroadcastUtils.refreshAppWidget(context);
                        break;
                }
            }
        });
        builder.setNegativeButton("取消操作", null);
        builder.create().show();
    }

    private void modifyScheduleName(ScheduleName scheduleName) {
        if (scheduleName == null) return;
        int cur = ShareTools.getInt(context, ShareConstants.INT_SCHEDULE_NAME_ID, -1);
        if (cur == scheduleName.getId()) {
            ScheduleDao.changeFuncStatus(MultiScheduleActivity.this, true);
        }

        BundleModel model = new BundleModel()
                .put(ModifyScheduleNameActivity.STRING_EXTRA_NAME, scheduleName.getName())
                .put(ModifyScheduleNameActivity.INT_EXTRA_ID, scheduleName.getId());
        ActivityTools.toActivity(context, ModifyScheduleNameActivity.class, model);
    }

    public void putValue(String value) {
        TimetableRequest.putValue(this, value, new Callback<ObjResult<ValuePair>>() {
            @Override
            public void onResponse(Call<ObjResult<ValuePair>> call, Response<ObjResult<ValuePair>> response) {
                ObjResult<ValuePair> result = response.body();
                if (result != null) {
                    if (result.getCode() == 200) {
                        ValuePair pair = result.getData();
                        if (pair != null) {
                            shareTable(pair);
                        } else {
                            Toasty.error(MultiScheduleActivity.this, "PutValue:data is null").show();
                        }
                    } else {
                        Toasty.error(MultiScheduleActivity.this, "PutValue:" + result.getMsg()).show();
                    }
                } else {
                    Toasty.error(MultiScheduleActivity.this, "PutValue:result is null").show();
                }
            }

            @Override
            public void onFailure(Call<ObjResult<ValuePair>> call, Throwable t) {
                Toasty.error(MultiScheduleActivity.this, "Error:" + t.getMessage()).show();
            }
        });
    }

    private void shareTable(ValuePair pair) {
        if (pair != null) {
            String content = "哈喽，你收到了来自时光猫的分享！\n复制这条消息，打开「时光猫」即可导入#" + pair.getId() + "";

            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", content);
            cm.setPrimaryClip(mClipData);
            Toasty.success(this, "已复制到剪切板,快复制给你的朋友吧!").show();

            Intent share_intent = new Intent();
            share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
            share_intent.setType("text/plain");//设置分享内容的类型
            share_intent.putExtra(Intent.EXTRA_SUBJECT, "分享课程");
            share_intent.putExtra(Intent.EXTRA_TEXT, content);//添加分享内容
            share_intent = Intent.createChooser(share_intent, "分享课程");
            startActivity(share_intent);
        }
    }

    public void getData() {
        loadLayout.setVisibility(View.VISIBLE);
        FindMultiExecutor executor = DataSupport.order("time desc").findAsync(ScheduleName.class);
        executor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(final List<T> t) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nameList.clear();
                        nameList.addAll((Collection<? extends ScheduleName>) t);
                        titleTextView.setText("课表包(" + nameList.size() + ")");

                        int index = -1;
                        int cur = ShareTools.getInt(context, ShareConstants.INT_SCHEDULE_NAME_ID, -1);
                        for (int i = 0; i < nameList.size(); i++) {
                            ScheduleName nameBean = nameList.get(i);
                            if (cur != -1 && cur == nameBean.getId()) {
                                index = i;
                            }
                        }

                        if (index != -1) {
                            ScheduleName curName = nameList.get(index);
                            nameList.remove(index);
                            nameList.add(0, curName);
                        }

                        adapter.notifyDataSetChanged();
                        loadLayout.setVisibility(View.GONE);
                    }
                });
            }
        });
    }


    private void setOnClick() {
        findViewById(R.id.cv_create).setOnClickListener(v -> toCreateScheduleNameActivity());
        findViewById(R.id.iv_back).setOnClickListener(v -> goBack());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toManagerActivity(position);
            }
        });
    }

    public void toCreateScheduleNameActivity() {
        ActivityTools.toActivity(context, CreateScheduleNameActivity.class);
    }

    public void toManagerActivity(int pos) {
        showListDialog(pos);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }


    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MainActivity.class);
    }
}
