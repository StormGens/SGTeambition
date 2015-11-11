package com.stormgens.sgteambition.ui.activity.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.stormgens.sgteambition.R;
import com.stormgens.sgteambition.constant.SPKeys;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestMainActivity extends AppCompatActivity {

//    @Bind(R.id.btn_test_webview)
//    Button mBtnTestWebview;
//    @Bind(R.id.btn_test_tablayout)
//    Button mBtnTestTablayout;
//    @Bind(R.id.btn_test_retrofit)
//    Button mBtnTestRetrofit;


    @OnClick(R.id.btn_test_webview)
    void ClickWebView() {
        Intent intent = new Intent(this, WebViewTestActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.btn_test_tablayout)
    void ClickTablayout(){
        Intent intent=new Intent(this,TestTabActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_test_retrofit)
    void clickRetrofitLayout(){
        Intent intent=new Intent(this,TestRetrofitActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_test_okhttp)
    void clickOkHttp(){
        Intent intent=new Intent(this,TestOkHttpActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);
        ButterKnife.bind(this);
        String accessToken=getSharedPreferences(SPKeys.USER_INFO_SP,MODE_PRIVATE).getString(SPKeys
                        .ACCESS_TOKEN, null);
        Toast.makeText(TestMainActivity.this, ""+accessToken, Toast.LENGTH_SHORT).show();
    }
}
