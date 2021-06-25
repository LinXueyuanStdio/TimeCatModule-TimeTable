package com.timecat.module.timetable.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.bingoogolapple.qrcode.core.QRCodeView.Delegate;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.model.ScheduleName;
import com.timecat.module.timetable.model.ScheduleDao;
import com.timecat.module.timetable.tools.BroadcastUtils;
import com.timecat.module.timetable.tools.ImageUtil;
import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import es.dmoral.toasty.Toasty;

public class ScanActivity extends AppCompatActivity implements Delegate {

  public static final int REQUEST_SCAN = 2;
  public static final int REQUEST_OPEN_LOCAL = 10;
  ZXingView zxingview;
  private LinearLayout backLayout;
  private LinearLayout localLayout;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timetable_activity_scan);
    initView();
    initEvent();
  }

  private void initView() {
    zxingview = findViewById(R.id.zxingview);
    backLayout = (LinearLayout) findViewById(R.id.id_back);
    localLayout = (LinearLayout) findViewById(R.id.id_scan_local);

  }

  private void initEvent() {
    zxingview.setDelegate(this);
    zxingview.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
    zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别

    backLayout.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {
        ActivityTools.toBackActivityAnim(ScanActivity.this, MainActivity.class);
      }
    });

    localLayout.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {
        openImage();
      }
    });
  }

  public void openImage() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("image/*");
    startActivityForResult(intent, REQUEST_OPEN_LOCAL);
  }

  @Override
  protected void onStop() {
    zxingview.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    zxingview.onDestroy(); // 销毁二维码扫描控件
    super.onDestroy();
  }

  public void analyzeCode(String url) {
    if (SuperUtils.isSuperUrl(url)) {
      Intent intent = new Intent(this, AuthActivity.class);
      intent.putExtra(AuthActivity.FLAG_TYPE, AuthActivity.TYPE_SCAN);
      intent.putExtra(AuthActivity.PARAMS_SCAN_URL, url);
      startActivityForResult(intent, REQUEST_SCAN);
    } else {
      Toast.makeText(this, "扫描的二维码不是超级课程表课程码", Toast.LENGTH_SHORT).show();
      goBack();
    }
  }


  /**
   * 获取返回的数据
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_SCAN && resultCode == AuthActivity.RESULT_STATUS) {
      SuperResult result = SuperUtils.getResult(data);
      displayLessons(result);
    }

    zxingview.startSpotAndShowRect(); // 显示扫描框，并开始识别
    if (requestCode == REQUEST_OPEN_LOCAL) {
      if (data != null) {
        Uri uri = data.getData();
        try {
          // 本来就用到 QRCodeView 时可直接调 QRCodeView 的方法，走通用的回调
          zxingview.decodeQRCode(ImageUtil.getImageAbsolutePath(this, uri));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void displayLessons(SuperResult result) {
    if (result == null) {
      Toasty.error(this, "result is null").show();
    } else if (result.getLessons() == null) {
      Toasty.error(this, "lessons is null").show();
    } else {
      if (result.isSuccess()) {
        ScheduleName newName = ScheduleDao.saveSuperLessons(result.getLessons());
        if (newName != null) {
          showDialogOnApply(newName);
        }
      } else {
        Toasty.error(this, "" + result.getErrMsg()).show();
      }
    }
  }

  private void showDialogOnApply(final ScheduleName name) {
    if (name == null) {
      return;
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("你导入的数据已存储在课表包[" + name.getName() + "]下!\n是否直接设置为当前课表?")
        .setTitle("课表导入成功")
        .setPositiveButton("设为当前课表", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            ScheduleDao.changeFuncStatus(ScanActivity.this, true);
            ScheduleDao.applySchedule(ScanActivity.this, name.getId());
            BroadcastUtils.refreshAppWidget(ScanActivity.this);
            if (dialogInterface != null) {
              dialogInterface.dismiss();
            }
            ActivityTools.toBackActivityAnim(ScanActivity.this,
                MainActivity.class, new BundleModel().put("item", 1));
          }
        })
        .setNegativeButton("稍后设置", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            if (dialogInterface != null) {
              dialogInterface.dismiss();
            }
            goBack();
          }
        });
    builder.create().show();
  }

  public void goBack() {
    ActivityTools.toBackActivityAnim(this, MainActivity.class);
  }

  @Override
  public void onBackPressed() {
    goBack();
  }

  @Override
  public void onScanQRCodeSuccess(String result) {
    analyzeCode(result);

  }

  @Override
  public void onCameraAmbientBrightnessChanged(boolean isDark) {

  }

  @Override
  public void onScanQRCodeOpenCameraError() {
    Toasty.error(ScanActivity.this, "打开相机出错", Toast.LENGTH_SHORT)
        .show();
    ActivityTools.toBackActivityAnim(ScanActivity.this, MainActivity.class);
  }
}
