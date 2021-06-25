package com.timecat.module.timetable.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jess.arms.http.imageloader.glide.GlideOptions;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.tools.VersionTools;
import com.zhuangfei.toolkit.tools.ActivityTools;

public class AboutActivity extends AppCompatActivity {


    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        versionText = (TextView) findViewById(R.id.tv_version);
        payLayout = (LinearLayout) findViewById(R.id.id_pay_layout);
        contentLayout = (LinearLayout) findViewById(R.id.id_content);
        alipayView = (ImageView) findViewById(R.id.id_ali_pay);
        wxpayView = (ImageView) findViewById(R.id.id_wx_pay);
    }

    TextView versionText;
    LinearLayout payLayout;
    LinearLayout contentLayout;
    ImageView alipayView;
    ImageView wxpayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_about);
        bindView();
        versionText.setText("版本号:" + VersionTools.getVersionName());

        String ali = "http://www.liuzhuangfei.com/apis/area/images/alipay.jpg";
        String wx = "http://www.liuzhuangfei.com/apis/area/images/wxpay.jpg";
        Glide.with(this).load(ali)
                .apply(new GlideOptions()
                        .placeholder(R.drawable.timetable_ic_launcher_background)
                        .error(R.drawable.timetable_ic_launcher_background))
                .into(alipayView);

        Glide.with(this)
                .load(wx)
                .apply(new GlideOptions()
                        .placeholder(R.drawable.timetable_ic_launcher_background)
                        .error(R.drawable.timetable_ic_launcher_background))
                .into(wxpayView);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }


    private void setOnClick() {
        findViewById(R.id.ib_back).setOnClickListener(v -> goBack());
        findViewById(R.id.id_zanzhu).setOnClickListener(v -> onZanzhuClicked());
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(this, MenuActivity.class);
    }


    public void onZanzhuClicked() {
        if (payLayout.getVisibility() == View.GONE) {
            payLayout.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            payLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }
}
