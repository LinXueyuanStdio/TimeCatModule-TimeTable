package com.timecat.module.timetable.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.timecat.module.timetable.api.TimetableRequest;
import com.timecat.module.timetable.api.model.ListResult;
import com.timecat.module.timetable.api.model.StationModel;
import com.timecat.module.timetable.event.ReloadStationEvent;
import com.timecat.module.timetable.event.UpdateStationHomeEvent;
import com.timecat.module.timetable.station.StationSdk;
import com.timecat.module.timetable.tools.StationManager;
import com.timecat.module.timetable.tools.ViewTools;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.toolkit.tools.BundleTools;
import com.zhuangfei.toolkit.tools.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 服务站加载引擎
 */
public class StationWebViewActivity extends AppCompatActivity {

    private static final String TAG = "StationWebViewActivity";

    private void bindView() {
        findView();
        setOnClick();
    }

    private void findView() {
        webView = (WebView) findViewById(R.id.id_webview);
        titleTextView = (TextView) findViewById(R.id.id_web_title);
        loadingProgressBar = (ContentLoadingProgressBar) findViewById(R.id.id_loadingbar);
        functionButton = (TextView) findViewById(R.id.id_btn_function);
        rootLayout = (LinearLayout) findViewById(R.id.id_station_root);
        actionbarLayout = (LinearLayout) findViewById(R.id.id_station_action_bg);
        moreImageView = (ImageView) findViewById(R.id.iv_station_more);
        closeImageView = (ImageView) findViewById(R.id.iv_station_close);
        buttonGroupLayout = (LinearLayout) findViewById(R.id.id_station_buttongroup);
        diverView = (View) findViewById(R.id.id_station_diver);
    }
    // wenview与加载条
    WebView webView;

    Class returnClass;

    // 标题
    TextView titleTextView;
    String url, title;

    ContentLoadingProgressBar loadingProgressBar;


    TextView functionButton;

    // 声明PopupWindow
    private CustomPopWindow popupWindow;

    StationModel stationModel;
    public static final String EXTRAS_STATION_MODEL = "station_model_extras";
    LinearLayout rootLayout;

    List<StationModel> localStationModels;
    boolean haveLocal = false;
    int deleteId = -1;

    LinearLayout actionbarLayout;
    Map<String, String> configMap;
    ImageView moreImageView;
    ImageView closeImageView;
    LinearLayout buttonGroupLayout;
    View diverView;//分隔竖线

    int needUpdate = 0;
    String[] textArray = null, linkArray = null;
    String tipText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeSetContentView();
        setContentView(R.layout.timetable_activity_station_web_view);
        bindView();
        initUrl();
        initView();
        loadWebView();
        findStationLocal();
        getStationById();
    }

    private void beforeSetContentView() {
        stationModel = (StationModel) BundleTools.getObject(this, EXTRAS_STATION_MODEL, null);
        if (stationModel == null) {
            ToastTools.show(this, "传参异常");
            finish();
        }
        configMap = StationManager.getStationConfig(stationModel.getUrl());
        if (configMap != null && !configMap.isEmpty()) {
            try {
                ViewTools.setStatusBarColor(this, Color.parseColor(configMap.get("statusColor")));
            } catch (Exception e) {
            }
        }
    }

    public void getStationById() {
        if (needUpdate == 0) return;
        TimetableRequest.getStationById(this, stationModel.getStationId(), new Callback<ListResult<StationModel>>() {
            @Override
            public void onResponse(Call<ListResult<StationModel>> call, Response<ListResult<StationModel>> response) {
                ListResult<StationModel> result = response.body();
                if (result != null) {
                    if (result.getCode() == 200) {
                        showStationResult(result.getData());
                    } else {
                        Toast.makeText(StationWebViewActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StationWebViewActivity.this, "station response is null!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ListResult<StationModel>> call, Throwable t) {

            }
        });
    }

    private void showStationResult(List<StationModel> result) {
        if (result == null || result.size() == 0) return;
        final StationModel model = result.get(0);
        if (model != null) {
            boolean update = false;
            if (model.getName() != null && !model.getName().equals(stationModel.getName())) {
                update = true;
            }
            if (model.getUrl() != null && !model.getUrl().equals(stationModel.getUrl())) {
                update = true;
            }
            if (model.getImg() != null && !model.getImg().equals(stationModel.getImg())) {
                update = true;
            }

            if (update) {
                final StationModel local = DataSupport.find(StationModel.class, stationModel.getId());
                if (local != null) {
                    local.setName(model.getName());
                    local.setUrl(model.getUrl());
                    local.setImg(model.getImg());
                    local.update(stationModel.getId());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("服务站更新")
                        .setMessage("本地保存的服务站已过期，需要重新加载")
                        .setPositiveButton("重新加载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ReloadStationEvent event = new ReloadStationEvent();
                                event.setStationModel(local);
                                EventBus.getDefault().post(new UpdateStationHomeEvent());
                                EventBus.getDefault().post(event);
                                finish();
                            }
                        });
                builder.create().show();
            }
        }
    }

    /**
     * 获取添加到首页的服务站
     */
    public void findStationLocal() {
        FindMultiExecutor findMultiExecutor = DataSupport.findAllAsync(StationModel.class);
        findMultiExecutor.listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<StationModel> stationModels = (List<StationModel>) t;
                if (localStationModels == null) {
                    localStationModels = new ArrayList<>();
                }
                localStationModels.clear();
                localStationModels.addAll(stationModels);
                haveLocal = searchInList(localStationModels, stationModel.getStationId());
            }
        });
    }

    public boolean searchInList(List<StationModel> list, int stationId) {
        if (list == null) return false;
        for (StationModel model : list) {
            if (model.getStationId() == stationId) {
                this.deleteId = model.getId();
                return true;
            }
        }
        return false;
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = StationManager.getRealUrl(stationModel.getUrl());
        title = stationModel.getName();
        if (returnClass == MainActivity.class) {
            needUpdate = 1;
        } else {
            needUpdate = 0;
        }
    }

    private void initView() {
        titleTextView.setText(title);
        if (configMap != null && !configMap.isEmpty()) {
            try {
                actionbarLayout.setBackgroundColor(Color.parseColor(configMap.get("actionColor")));
            } catch (Exception e) {
            }

            try {
                int textcolor = Color.parseColor(configMap.get("actionTextColor"));
                titleTextView.setTextColor(textcolor);
                moreImageView.setColorFilter(textcolor);
                closeImageView.setColorFilter(textcolor);
                GradientDrawable gd = new GradientDrawable();
                gd.setCornerRadius(ScreenUtils.dip2px(this, 25));
                gd.setStroke(2, textcolor);
                diverView.setBackgroundColor(textcolor);
                buttonGroupLayout.setBackgroundDrawable(gd);
            } catch (Exception e) {
            }
        }
    }

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.pop_add_home) {
                if (haveLocal) {
                    DataSupport.delete(StationModel.class, deleteId);
                    EventBus.getDefault().post(new UpdateStationHomeEvent());
                    ToastTools.show(StationWebViewActivity.this, "已从主页删除");
                } else {
                    if (localStationModels.size() >= 15) {
                        ToastTools.show(StationWebViewActivity.this, "已达到最大数量限制15，请先删除其他服务站后尝试");
                    } else {
                        stationModel.save();
                        ToastTools.show(StationWebViewActivity.this, "已添加到首页");
                        EventBus.getDefault().post(new UpdateStationHomeEvent());
                    }
                }
                findStationLocal();

            } else if (i == R.id.pop_about) {
                if (stationModel != null && stationModel.getOwner() != null) {
                    ToastTools.show(StationWebViewActivity.this, stationModel.getOwner());
                } else {
                    ToastTools.show(StationWebViewActivity.this, "所有者未知!");
                }

            } else if (i == R.id.pop_to_home) {
                webView.clearHistory();
                webView.loadUrl(stationModel.getUrl());

            } else {
            }
            popupWindow.dismiss();
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("gb2312");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new StationSdk(this, getStationSpace()), "sdk");

//        settings.setSupportZoom(true);
//        settings.setBuiltInZoomControls(true);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading: " + url);
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                loadingProgressBar.setProgress(newProgress);
                if (newProgress == 100) loadingProgressBar.hide();
                else loadingProgressBar.show();
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
//                titleTextView.setText(title);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            back();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    public void setButtonSettings(String btnText, String[] textArray, String[] linkArray) {
        if (TextUtils.isEmpty(btnText)) return;
        functionButton.setText(btnText);
        functionButton.setVisibility(View.VISIBLE);
        this.textArray = textArray;
        this.linkArray = linkArray;
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.timetable_anim_station_static, R.anim.timetable_anim_station_close_activity);
    }

    /**
     * 弹出popupWindow更改头像
     */

    private void setOnClick() {
        findViewById(R.id.id_station_more).setOnClickListener(v -> showMorePopWindow());
        findViewById(R.id.id_station_close).setOnClickListener(v -> back());
        findViewById(R.id.id_btn_function).setOnClickListener(v -> onButtonClicked());
    }

    public void showMorePopWindow() {
        popupWindow = new CustomPopWindow(StationWebViewActivity.this, haveLocal, itemsOnClick);
        popupWindow.showAtLocation(rootLayout,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupWindow.backgroundAlpha(StationWebViewActivity.this, 1f);
            }
        });
    }


    public void back() {
        finish();
    }

    public void showMessage(String msg) {
        ToastTools.show(this, msg);
    }

    public Context getStationContext() {
        return this;
    }

    public WebView getWebView() {
        return webView;
    }

    public String getStationSpace() {
        return "station_space_" + stationModel.getStationId();
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }


    public void onButtonClicked() {
        if (textArray == null || linkArray == null) return;
        if (textArray.length != linkArray.length) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("请选择功能")
                .setItems(textArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i < linkArray.length) {
                            webView.loadUrl(linkArray[i]);
                        }
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.create().show();
    }
}
