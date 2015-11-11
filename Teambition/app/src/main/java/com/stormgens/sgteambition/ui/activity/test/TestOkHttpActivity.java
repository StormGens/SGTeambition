package com.stormgens.sgteambition.ui.activity.test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
        final String code = preferences.getString(SPKeys.CODE, null);
        OkHttpClient client = new OkHttpClient();
        Log.v("zlq", code + "");
        Request request2 = new Request.Builder().url(ApiUrls.CHECK_ACCESS_TOKEN)
                .header("Authorization", "w01t3voxJORMhWnhF_EiN4EtrGI=QDhMrPq603b5080be1825f8d0afdfdeeed4261c0987f2d60daa6220eecefeaf1456e3a09fcf93125cc0425c91466d615b6049938ad6bf8ebc1f1d2d56381ae581b0b78bcbdf2602745a688a7898acc9fd1c63c1c57bd5941427bbc55f55be1a4301c823f450d0713e9520c5eef4033f285911ad0").build();
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


            }
        });
    }

}
