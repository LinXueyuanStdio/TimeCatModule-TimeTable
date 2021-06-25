package com.timecat.module.timetable.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.ContentLoadingProgressBar;

import com.timecat.module.timetable.MainActivity;
import com.timecat.module.timetable.R;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.BundleTools;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";


    private void findView() {
        webView = (WebView) findViewById(R.id.id_webview);
        titleTextView = (TextView) findViewById(R.id.id_web_title);
        layout = (LinearLayout) findViewById(R.id.id_webview_layout);
        helpView = (TextView) findViewById(R.id.id_webview_help);
        loadingProgressBar = (ContentLoadingProgressBar) findViewById(R.id.id_loadingbar);
    }

    // wenview与加载条
    WebView webView;
    // 标题
    TextView titleTextView;
    LinearLayout layout;
    TextView helpView;
    ContentLoadingProgressBar loadingProgressBar;

    // 关闭
    private LinearLayout closeLayout;
    Class returnClass;
    String url, title;

    boolean isScoreQuery = false;
    boolean isUseBrower = false;

    //所有成绩
    public static final String URL_SCORE_ALL = "https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/gradeLnAllAction.do?type=ln&oper=qb";

    //本学期成绩
    public static final String URL_SCORE_TERM = "https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/bxqcjcxAction.do";

    //空教室
    public static final String URL_EMPTYROOM = "https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xszxcxAction.do?oper=xszxcx_lb";

    //选课
    public static final String URL_COURSE_CHOOSE = "https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do";

    //退课
    public static final String URL_COURSE_DELETE = "https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=7";

    //选课结果
    public static final String URL_COURSE_RESULT = "https://vpn.hpu.edu.cn/web/1/http/2/218.196.240.97/xkAction.do?actionType=6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity_web_view);
        findView();
        setOnClick();
        initUrl();
        initView();
        loadWebView();
    }

    private void initUrl() {
        returnClass = BundleTools.getFromClass(this, MainActivity.class);
        url = BundleTools.getString(this, "url", "http://www.liuzhuangfei.com");
        title = BundleTools.getString(this, "title", "WebView");
        int isUse = (int) BundleTools.getInt(this, "isUse", 0);
        if (isUse == 1) isUseBrower = true;
        if (title != null && title.indexOf("成绩") != -1) {
            isScoreQuery = true;
            helpView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示弹出菜单
     */
    private void setOnClick() {
        findViewById(R.id.id_webview_help).setOnClickListener(v -> showPopmenu());
        findViewById(R.id.id_webview_btn1).setOnClickListener(v -> onButton1CLicked());
        findViewById(R.id.id_webview_btn2).setOnClickListener(v -> onButton2CLicked());
        findViewById(R.id.id_webview_btn3).setOnClickListener(v -> onButton3CLicked());
        findViewById(R.id.id_webview_btn4).setOnClickListener(v -> onButton4CLicked());
        findViewById(R.id.id_webview_btn5).setOnClickListener(v -> onButton5CLicked());
        findViewById(R.id.id_webview_btn6).setOnClickListener(v -> onButton6CLicked());
    }

    public void showPopmenu() {
        PopupMenu popup = new PopupMenu(this, helpView);
        popup.getMenuInflater().inflate(R.menu.timetable_menu_webview2, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.top1) {
                    layout.setVisibility(View.VISIBLE);

                } else if (i == R.id.top2) {
                    webView.loadUrl(url);

                } else {
                }
                return true;
            }
        });

        popup.show();
    }

    public void onButton1CLicked() {
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_SCORE_ALL);
    }

    public void onButton2CLicked() {
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_SCORE_TERM);
    }

    public void onButton3CLicked() {
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_EMPTYROOM);
    }

    public void onButton4CLicked() {
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_COURSE_CHOOSE);
    }

    public void onButton5CLicked() {
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_COURSE_DELETE);
    }

    public void onButton6CLicked() {
        layout.setVisibility(View.GONE);
        webView.loadUrl(URL_COURSE_RESULT);
    }

    private void initView() {
        titleTextView.setText(title);
        closeLayout = (LinearLayout) findViewById(R.id.id_close);
        closeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ActivityTools.toBackActivityAnim(WebViewActivity.this,
                        returnClass);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("gb2312");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);

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

                if (isUseBrower) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else {
                    webView.loadUrl(url);
                }
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
                titleTextView.setText(title);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            ActivityTools.toBackActivityAnim(this, returnClass);
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
}
