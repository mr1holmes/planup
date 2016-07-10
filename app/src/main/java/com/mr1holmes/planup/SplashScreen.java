package com.mr1holmes.planup;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mr1holmes.planup.data.PlanupContract;
import com.mr1holmes.planup.service.FetchUserFriendsService;
import com.mr1holmes.planup.sync.PlanupSyncAdapter;
import com.mr1holmes.planup.util.AccountUtils;
import com.mr1holmes.planup.util.LogUtils;
import com.mr1holmes.planup.util.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Welcome Activity
 */
public class SplashScreen extends AppCompatActivity {

    private LoginButton loginButton;
    private ProgressBar mProgress;
    private CallbackManager callbackManager;
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.LOGV(TAG, "SplashScreen onCreate");
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_splashscreen);

        PlanupSyncAdapter.initializeSyncAdapter(this);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        mProgress = (ProgressBar) findViewById(R.id.login_progress);

        loginButton.setReadPermissions(AccountUtils.FACEBOOK_PERMISSIONS);

        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                LogUtils.LOGD(TAG, "permissions = " + AccessToken.getCurrentAccessToken().getPermissions());


                // Check if all required permissions are provided
                if (AccountUtils.isLoggedIn()) {
                    LogUtils.LOGV(TAG, "User provided all the permissions");

                    loginButton.setVisibility(View.GONE);
                    mProgress.setVisibility(View.VISIBLE);

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    parseAndSend(object);
                                }
                            });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,first_name,last_name,picture{url}");
                    request.setParameters(parameters);
                    request.executeAsync();

                    // Fetch friends of this user and add them to database
                    Intent fetchUserService = new Intent(SplashScreen.this, FetchUserFriendsService.class);
                    startService(fetchUserService);

                } else {
                    LogUtils.LOGV(TAG, "User did not provide some permissions, try again");
                    LoginManager.getInstance().logInWithReadPermissions(SplashScreen.this,
                            Arrays.asList(AccountUtils.FACEBOOK_PERMISSIONS));
                }
            }

            @Override
            public void onCancel() {
                // App code
                LogUtils.LOGD(TAG, "Cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                LogUtils.LOGD(TAG, "Error");
            }
        });

        if (AccountUtils.isLoggedIn()) {
            // show startup screen for few second and then move to next screen
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startNext();
                }
            }, 1500);
        } else {
            mProgress.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }

    }

    private void parseAndSend(JSONObject responseObject) {
        try {

            String user_id = responseObject.getString("id");
            String first_name = responseObject.getString("first_name");
            String last_name = responseObject.getString("last_name");
            String profile_url = DatabaseUtils.sqlEscapeString(
                    responseObject.getJSONObject("picture").getJSONObject("data").getString("url"));
            String fcm_token = FirebaseInstanceId.getInstance().getToken();

            // Add user in database
            ContentValues contentValues = new ContentValues();
            contentValues.put(PlanupContract.TABLE_USER.COLUMN_USER_ID, user_id);
            contentValues.put(PlanupContract.TABLE_USER.COLUMN_FIRST_NAME, first_name);
            contentValues.put(PlanupContract.TABLE_USER.COLUMN_LAST_NAME, last_name);
            contentValues.put(PlanupContract.TABLE_USER.COLUMN_PROFILE_URL, profile_url);
            contentValues.put(PlanupContract.TABLE_USER.COLUMN_FCM_TOKEN, fcm_token);

            try {
                getContentResolver().insert(PlanupContract.TABLE_USER.CONTENT_URI, contentValues);
            } catch (Exception se) {
                se.printStackTrace();
            }

            // Store id to identify current user
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.local_id), user_id);
            editor.commit();

            JSONObject dataObj = new JSONObject();
            dataObj.put("type", "user");
            dataObj.put(PlanupContract.TABLE_USER.COLUMN_USER_ID, user_id);
            dataObj.put(PlanupContract.TABLE_USER.COLUMN_FIRST_NAME, first_name);
            dataObj.put(PlanupContract.TABLE_USER.COLUMN_LAST_NAME, last_name);
            dataObj.put(PlanupContract.TABLE_USER.COLUMN_PROFILE_URL, profile_url);
            dataObj.put(PlanupContract.TABLE_USER.COLUMN_FCM_TOKEN, fcm_token);

            JSONObject requestObject = new JSONObject();
            requestObject.put("data", dataObj);

            // Add user on server
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    getString(R.string.server_endpoint) + "/users", null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    LogUtils.LOGV(TAG, "User added on server " + response);
                    startNext();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    LogUtils.LOGD(TAG, "Something went wrong while adding user on server");
                    // DEBUG
                    startNext();
                }
            });

            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void startNext() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}

