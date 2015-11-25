package com.stormgens.sgteambition.ui.activity.test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.stormgens.sgteambition.R;
import com.stormgens.sgteambition.api.ApiUrls;
import com.stormgens.sgteambition.constant.SPKeys;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestOkHttpActivity extends AppCompatActivity {


    @Bind(R.id.btn_okhttp_get_check)
    Button btnOkhttpGetCheck;

    @OnClick(R.id.btn_okhttp_get_check)
    void clickCheckAccessToken() {
        SharedPreferences preferences = getSharedPreferences(SPKeys.USER_INFO_SP, MODE_PRIVATE);
        final String accessToken = preferences.getString(SPKeys.ACCESS_TOKEN, "");
        OkHttpClient client = new OkHttpClient();
        Log.v("zlq", "accessToken:"+accessToken);
        Request request2 = new Request.Builder().url(ApiUrls.CHECK_ACCESS_TOKEN)
                .header("Authorization", accessToken).build();
        Call call1 = client.newCall(request2);
        call1.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.v("okhttp", response.body().string());
                            Toast.makeText(TestOkHttpActivity.this, "accessToken依然有效", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ok_http);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Toast.makeText(TestOkHttpActivity.this, "巴拉巴拉", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
