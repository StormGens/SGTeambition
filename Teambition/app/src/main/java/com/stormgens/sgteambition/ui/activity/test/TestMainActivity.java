package com.stormgens.sgteambition.ui.activity.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.stormgens.sgteambition.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestMainActivity extends AppCompatActivity {

    @Bind(R.id.btn_test_webview)
    Button mBtnTestWebview;
    @Bind(R.id.btn_test_tablayout)
    Button mBtnTestTablayout;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);
        ButterKnife.bind(this);
    }
}
