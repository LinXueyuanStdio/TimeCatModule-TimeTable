package com.timecat.module.timetable.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.activity.WebViewActivity;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.BaseResult;
import com.timecat.module.timetable.api.model.MessageModel;
import com.timecat.module.timetable.api.model.StationModel;
import com.timecat.module.timetable.constants.ShareConstants;
import com.timecat.module.timetable.tools.DeviceTools;
import com.timecat.module.timetable.tools.StationManager;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Liu ZhuangFei on 2018/8/15.
 */

public class MessageAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;

    List<MessageModel> list;
    Activity context;
    String schoolName;
    String device;

    SharedPreferences messagePreferences;
    SharedPreferences.Editor messageEditor;

    Set<String> readSet;

    public MessageAdapter(Activity context, List<MessageModel> list) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.list = list;
        device=DeviceTools.getDeviceId(context);
        messagePreferences=context.getSharedPreferences("app_message",Context.MODE_PRIVATE);
        messageEditor=messagePreferences.edit();
        readSet=getReadSet();
        schoolName=ShareTools.getString(context,ShareConstants.STRING_SCHOOL_NAME,"unknow");
    }

    public synchronized Set<String> getReadSet() {
        Set<String> r=messagePreferences.getStringSet("app_message_set",new HashSet<String>());
        Set<String> newSet=new HashSet<>(r);
        return newSet;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.timetable_item_message, null);
            holder.contentTextView = (TextView) convertView.findViewById(R.id.item_content);
            holder.targetTextView = (TextView) convertView.findViewById(R.id.item_message_target);
            holder.stationLayout = (LinearLayout) convertView.findViewById(R.id.item_station_layout);
            holder.urlLayout = (LinearLayout) convertView.findViewById(R.id.item_url_layout);
            holder.urlTitleTextView = (TextView) convertView.findViewById(R.id.item_message_url_title);
            holder.stationNameTextView = (TextView) convertView.findViewById(R.id.item_message_station_name);
            holder.stationImageView = (ImageView) convertView.findViewById(R.id.item_message_station_img);
            holder.readTextView=convertView.findViewById(R.id.item_message_isread);
            holder.timeTextView=convertView.findViewById(R.id.item_message_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final MessageModel model = list.get(position);
        if (model != null) {
            SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm");
            if(model.getTime()!=null){
                holder.timeTextView.setText(sdf.format(new Date(Long.parseLong(model.getTime()+"000"))));
            }else {
                holder.timeTextView.setText("未知时间");
            }

            if(model.getIsread()==0&&(model.getTarget()!=null&&!readSet.contains(String.valueOf(model.getId())))){
                holder.readTextView.setVisibility(View.VISIBLE);
            }else {
                holder.readTextView.setVisibility(View.GONE);
            }

            holder.targetTextView.setVisibility(View.VISIBLE);
            if(model.getTarget()!=null&&device!=null){
                if(model.getTarget().equals(device)){
                    holder.targetTextView.setText("To 当前设备");
                }else  if(model.getTarget().equals(schoolName)){
                    holder.targetTextView.setText("To 当前学校");
                }else if(model.getTarget().equals("all")){
                    holder.targetTextView.setText("To 所有用户");
                }else {
                    holder.targetTextView.setVisibility(View.GONE);
                }
            }else {
                holder.targetTextView.setVisibility(View.GONE);
            }

            final String target=model.getTarget();
            final int id=model.getId();
            final TextView finalTextView=holder.readTextView;

            holder.readTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(target!=null&&(target.equals("all")||target.equals(schoolName))){
                        readSet=getReadSet();
                        readSet.add(String.valueOf(id));
                        messageEditor.clear();
                        messageEditor.putStringSet("app_message_set",readSet).commit();
                        if(finalTextView!=null){
                            finalTextView.setVisibility(View.GONE);
                        }
                        Toast.makeText(context,"已标为已读!",Toast.LENGTH_SHORT).show();
                    }else {
                        setMessageRead(model.getId(),finalTextView);
                    }
                }
            });

            final String content = model.getContent();
            if (content != null) {
                String realContent = content.replaceAll("<station>.*?</station>", "");
                realContent = realContent.replaceAll("<url>.*?</url>", "");
                holder.contentTextView.setText(realContent);

                Pattern pattern = Pattern.compile("<station>(.*?)</station>");
                Matcher matcher = pattern.matcher(content);
                boolean isFind = matcher.find();
                if (isFind) {
                    final Map<String, String> map = new HashMap();
                    final String stationInfo = matcher.group(1);
                    String[] array = stationInfo.split("&next;");
                    if (array != null) {
                        for (int i = 0; i < array.length; i++) {
                            String[] params = array[i].split("=");
                            map.put(params[0], params[1]);
                        }
                    }
                    holder.stationLayout.setVisibility(View.VISIBLE);
                    holder.stationNameTextView.setText(map.get("name"));
                    holder.stationLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StationModel stationModel=new StationModel();
                            stationModel.setImg(map.get("img"));
                            stationModel.setName(map.get("name"));
                            stationModel.setUrl(map.get("url"));
                            int id=-1;
                            if(map.containsKey("id")){
                                id=Integer.valueOf(map.get("id"));
                            }
                            stationModel.setStationId(id);
                            StationManager.openStationWithout(context,stationModel);
                        }
                    });

                    Glide.with(context).load(map.get("img")).into(holder.stationImageView);
                } else {
                    holder.stationLayout.setVisibility(View.GONE);
                }

                Pattern pattern2 = Pattern.compile("<url>(.*?)</url>");
                Matcher matcher2 = pattern2.matcher(content);
                boolean isFind2 = matcher2.find();
                if (isFind2) {
                    final Map<String, String> map = new HashMap();
                    String urlInfo = matcher2.group(1);
                    String[] array = urlInfo.split("&next;");
                    if (array != null) {
                        for (int i = 0; i < array.length; i++) {
                            String[] params = array[i].split("=");
                            map.put(params[0], params[1]);
                        }
                    }
                    holder.urlLayout.setVisibility(View.VISIBLE);
                    holder.urlTitleTextView.setText(map.get("title"));
                    holder.urlLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityTools.toActivityWithout(context, WebViewActivity.class,
                                    new BundleModel().setFromClass(context.getClass())
                                            .put("title", map.get("title"))
                                            .put("url",map.get("href")));
                        }
                    });
                } else {
                    holder.urlLayout.setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }

    public void setMessageRead(final int id, final TextView readTextView){
        if(id==0) return;

        TimetableRequest.setMessageRead(context, id, new Callback<BaseResult>() {
            @Override
            public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                if(response==null) return;
                BaseResult baseResult=response.body();
                if(baseResult==null) return;
                if(baseResult.getCode()==200){
                    if(readTextView!=null){
                        readTextView.setVisibility(View.GONE);
                    }

                    readSet=getReadSet();
                    readSet.add(String.valueOf(id));
                    messageEditor.clear();
                    messageEditor.putStringSet("app_message_set",readSet).commit();
                    Toast.makeText(context,"已标为已读!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,baseResult.getMsg(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResult> call, Throwable t) {
                Toast.makeText(context,"Error:"+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //ViewHolder静态类
    static class ViewHolder {
        public TextView timeTextView;
        public TextView readTextView;
        public TextView targetTextView;
        public TextView contentTextView;
        public LinearLayout stationLayout;
        public ImageView stationImageView;
        public TextView stationNameTextView;
        public LinearLayout urlLayout;
        public TextView urlTitleTextView;
    }

}
