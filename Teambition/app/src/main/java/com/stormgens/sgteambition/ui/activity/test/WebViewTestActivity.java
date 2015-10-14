package com.stormgens.sgteambition.ui.activity.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.stormgens.sgteambition.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WebViewTestActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.btn_baidu)
    Button mBtnBaidu;
    @Bind(R.id.btn_baidu_intent)
    Button mBtnBaiduIntent;
    @Bind(R.id.btn_call_js)
    Button mBtnCallJs;
    @Bind(R.id.btn_wv_local_page)
    Button mBtnLocalPage;
    @Bind(R.id.webview)
    WebView mWebview;
    @Bind(R.id.btn_call_js_param)
    Button mBtnCallJsParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_test);
        ButterKnife.bind(this);
        initListeners();
        initWebView();
    }

    @SuppressLint("setJavaScriptEnabled")
    private void initWebView() {

        //其中包含多项配置。WebSettings用来对WebView的配置进行配置和管理，比如是否可以进行文件操作、缓存的设置、
        // 页面是否支持放大和缩小、是否允许使用数据库api、字体及文字编码设置、是否允许js脚本运行、
        // 是否允许图片自动加载、是否允许数据及密码保存等等
        mWebview.getSettings().setJavaScriptEnabled(true);

        mWebview.addJavascriptInterface(this, "webjs");

        //如果页面中链接， 如果希望点击链接继续在当前 browser 中响应，
        //而不是新开 Android 的系统browser 中响应该链接，必须覆盖 webview 的 WebViewClient 对象。

        //WebViewClient会在一些影响内容渲染的动作发生时被调用，比如表单的错误提交需要重新提交、页面开始加载及加载完成、
        //资源加载中、接收到http认证需要处理、页面键盘响应、页面中的url打开处理等等
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        //WebChromeClient会在一些影响浏览器ui交互动作发生时被调用，比如WebView关闭和隐藏、页面加载进展、
        //js确认框和警告框、js加载前、js操作超时、webView获得焦点等等
        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    private void initListeners() {
        mBtnBaiduIntent.setOnClickListener(this);
        mBtnBaidu.setOnClickListener(this);
        mBtnLocalPage.setOnClickListener(this);
        mBtnCallJs.setOnClickListener(this);
        mBtnCallJsParam.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mWebview.canGoBack()) {//可以后退时，希望网页后退而不是关掉Activity。
            mWebview.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_baidu_intent:
                Uri uri = Uri.parse("http://www.baidu.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.btn_baidu:
                mWebview.loadUrl("http://www.baidu.com");
                break;
            case R.id.btn_wv_local_page:
                mWebview.loadUrl("file:///android_asset/webjs.html");
                break;
            case R.id.btn_call_js:
                //调用页面里面的javacalljs()方法
                mWebview.loadUrl("javascript:javacalljs()");
                break;
            case R.id.btn_call_js_param:
                //调用页面里面的javacalljs()方法并传递参数
                mWebview.loadUrl("javascript:javacalljswithargs(" + "'hello world'" + ")");
                break;

        }
    }

    @JavascriptInterface
    public void startFunction() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WebViewTestActivity.this, "js调用了java函数", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @JavascriptInterface
    public void startFunction(final String str) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(WebViewTestActivity.this, "js调用了java函数并传递参数:" + str, Toast
                        .LENGTH_SHORT)
                        .show();
            }
        });
    }
}
