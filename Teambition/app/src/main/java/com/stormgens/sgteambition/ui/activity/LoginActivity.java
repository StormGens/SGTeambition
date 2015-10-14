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
import android.net.Uri;
import android.os.AsyncTask;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private LoadLoaginHtmlTask mAuthTask = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences=getPreferences(MODE_PRIVATE);
        String c= preferences.getString("code", null);
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
                attemptGetCode(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        mWvLogin.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressView.setVisibility(View.GONE);
                } else {
                    mProgressView.setVisibility(View.VISIBLE);
                }

                if (newProgress == 100) {
                    Logger.d("webView 100 progress:" + mWvLogin.getUrl());
                    if (mWvLogin.getUrl().startsWith(ApiKeys.RedirectUri)){
                        Toast.makeText(LoginActivity.this, "认证成功，跳转到主页面", Toast.LENGTH_SHORT).show();
                        gotoMainActivity();
                        finish();
                    }

                }

                super.onProgressChanged(view, newProgress);
            }
        });

    }

    private String attemptGetCode(String url) {
        if (url.startsWith(ApiKeys.RedirectUri)) {
            Params params = ParamsUtil.decodeUrl(url);
            String code = params.getParameter("code");
            Logger.d("授权成功，code：" + code);
            SharedPreferences preferences=getPreferences(MODE_PRIVATE);
            preferences.edit().putString("code", code).apply();
            return code;
        } else {
            return "";
        }

    }

    final class LoginJavaScriptInterface {

        public LoginJavaScriptInterface() {

        }

        @JavascriptInterface
        public void setAccount(String account, String password) {
            mAccount = account;
            mPassword = password;
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
        if (mAuthTask != null) {
            return;
        }

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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
//            mAuthTask = new UserLoginTask(mAccount, mPassword);
//            mAuthTask=new LoadLoaginHtmlTask(mAccount,mPassword);
//            mAuthTask.execute((Void) null);
            mRlWebview.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                gotoMainActivity();

            } else {
                mEtPassword.setError(getString(R.string.error_incorrect_password));
                mEtPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
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

    class LoadLoaginHtmlTask extends AsyncTask<Void,Void,String>{

        private final String mEmail;
        private final String mPassword;

        public LoadLoaginHtmlTask(String mEmail, String mPassword) {
            this.mEmail = mEmail;
            this.mPassword = mPassword;
        }

        @Override
        protected String doInBackground(Void... params) {
            final String url= ApiUrls.AUTH;
            int count = 3;
            while (count-- >= 0) {
                try {
                    String js = FileUtils.readAssetsFile("oauth.js", MyApp.getInstance());
                    js = js.replace("%email%", mEmail).replace("%password%", mPassword);
                    Logger.d(url);
                    Document dom = Jsoup.connect(url).get();
                    String html = dom.toString();
                    html = html.replace("</head>", js + "</head>")
                            .replace("action-type=\"submit\"", "action-type=\"submit\" id=\"submit\"");
                    return html;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            throw new RuntimeException("失败了");
        }

        @Override
        protected void onPostExecute(String s) {
            mRlWebview.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
            mWvLogin.loadDataWithBaseURL("https://account.teambition.com/", s, "text/html", "UTF-8",
                    "");

            Logger.d(s);
            super.onPostExecute(s);
        }
    }
}

