package com.stormgens.sgteambition.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stormgens.library.common.network.Params;
import com.stormgens.library.common.network.ParamsUtil;
import com.stormgens.library.common.utils.FileUtils;
import com.stormgens.library.common.utils.Logger;
import com.stormgens.sgteambition.MyApp;
import com.stormgens.sgteambition.R;
import com.stormgens.sgteambition.api.ApiKeys;
import com.stormgens.sgteambition.api.ApiUrls;
import com.stormgens.sgteambition.constant.SPKeys;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    @Bind(R.id.wv_login)
    WebView mWvLogin;
    @Bind(R.id.rl_webview)
    RelativeLayout mRlWebview;
    @Bind(R.id.eil_email)
    TextInputLayout mEilEmail;
    @Bind(R.id.eil_password)
    TextInputLayout mEilPassword;

    // UI references.
    @Bind(R.id.email)
    AutoCompleteTextView mActEmail;
    @Bind(R.id.password)
    EditText mEtPassword;
    @Bind(R.id.progress)
    View mProgressView;
    @Bind(R.id.login_form)
    View mLoginFormView;
    private String mAccount;
    private String mPassword;

    private boolean accountFilled;
    private boolean oauthed=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences=getPreferences(MODE_PRIVATE);
        String c= preferences.getString(SPKeys.CODE, null);
        if (!TextUtils.isEmpty(c)){
            gotoMainActivity();
            finish();
        }

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        // Set up the login form.
        mActEmail.addTextChangedListener(new SGTextWatcher(mEilEmail));
        mEtPassword.addTextChangedListener(new SGTextWatcher(mEilPassword));


        initLoginWebView();

        populateAutoComplete();

        mEtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void initLoginWebView() {
        WebSettings settings=mWvLogin.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");

        mWvLogin.addJavascriptInterface(new LoginJavaScriptInterface(), "loginjs");
        mWvLogin.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        mWvLogin.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressView.setVisibility(View.GONE);
                    Logger.d("webView 100 progress:" + mWvLogin.getUrl());

                    if (mWvLogin.getUrl().startsWith(ApiKeys.RedirectUri) && !oauthed) {
                        oauthed = true;

                        boolean success = attemptGetCode(mWvLogin.getUrl());
                        if (success) {
                            Toast.makeText(LoginActivity.this, "认证成功，跳转到主页面", Toast.LENGTH_SHORT).show();
                            mWvLogin.loadUrl("javascript:" + FileUtils.readAssetsFile("getaccount.js",
                                    MyApp.getInstance()));
                            mWvLogin.loadUrl("javascript:getAccount()");
                            gotoMainActivity();
                            finish();
                        } else {
                            mWvLogin.loadUrl(ApiUrls.AUTH);
                        }
                    }
                    if (!accountFilled && mWvLogin.getUrl().contains("https://account.teambition.com/login?next_url")) {
                        String script = FileUtils.readAssetsFile("login.js", MyApp.getInstance());
                        script = script.replace("%password%", mPassword);
                        script = script.replace("%email%", mAccount);
                        mWvLogin.loadUrl("javascript:" + script);
                        mWvLogin.loadUrl("javascript:fillAccount()");
                        accountFilled = true;
                    }
                    if (mWvLogin.getUrl().contains("https://account.teambition.com/oauth2/authorize?client_id")) {
                        String script = FileUtils.readAssetsFile("oauth.js", MyApp.getInstance());
                        mWvLogin.loadUrl("javascript:" + script);
                        mWvLogin.loadUrl("javascript:auth()");
                    }
                } else {
                    mProgressView.setVisibility(View.VISIBLE);
                }

                super.onProgressChanged(view, newProgress);
            }
        });

    }

    private boolean attemptGetCode(String url) {
            Params params = ParamsUtil.decodeUrl(url);
            String code = params.getParameter("code");
            if (TextUtils.isEmpty(code)){
                Toast.makeText(LoginActivity.this, "授权失败：" + params.getParameter("error"), Toast
                        .LENGTH_SHORT).show();
                return false;
            }else{
                Logger.d("授权成功，code：" + code);
                SharedPreferences preferences=getPreferences(MODE_PRIVATE);
                preferences.edit().putString(SPKeys.CODE, code).apply();
                return true;
            }
    }

    final class LoginJavaScriptInterface {

        public LoginJavaScriptInterface() {

        }

        @JavascriptInterface
        public void setAccount(String account, String password) {
            mAccount = account;
            mPassword = password;
            Toast.makeText(LoginActivity.this, "account:"+mAccount+"--password:"+password, Toast
                    .LENGTH_SHORT).show();
        }

    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mActEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Store values at the time of the login attempt.
        mAccount= mActEmail.getText().toString();
        mPassword = mEtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(mPassword) && !isPasswordValid(mPassword)) {
            mEilPassword.setErrorEnabled(true);
            mEilPassword.setError(getString(R.string.error_invalid_password));
            focusView = mEtPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mAccount)) {
            mEilEmail.setErrorEnabled(true);
            mEilEmail.setError(getString(R.string.error_field_required));
            focusView = mActEmail;
            cancel = true;
        } else if (!isEmailValid(mAccount)) {
            mEilEmail.setErrorEnabled(true);
            mEilEmail.setError(getString(R.string.error_invalid_email));
            focusView = mActEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            switchToWebView();
            mWvLogin.loadUrl(ApiUrls.AUTH);

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mActEmail.setAdapter(adapter);
    }

    private void gotoMainActivity(){
        startActivity(MainActivity.newIntent(LoginActivity.this));
    }


    class SGTextWatcher implements TextWatcher{
        TextInputLayout textInputLayout;

        public SGTextWatcher(TextInputLayout textInputLayout) {
            this.textInputLayout = textInputLayout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textInputLayout.setErrorEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    @Override
    public void onBackPressed() {
        if (mWvLogin!=null&&mWvLogin.canGoBack()&&mWvLogin.getVisibility()==View.VISIBLE){
            switchToLoginForm();
            mWvLogin.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void switchToLoginForm() {
        mRlWebview.setVisibility(View.GONE);
        mLoginFormView.setVisibility(View.VISIBLE);
    }

    private void switchToWebView(){
        mRlWebview.setVisibility(View.VISIBLE);
        mLoginFormView.setVisibility(View.GONE);
    }
}

