package com.mr1holmes.planup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.mr1holmes.planup.sync.PlanupSyncAdapter;
import com.mr1holmes.planup.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mr1holmes on 25/6/16.
 */
public class SplashScreen extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private final String TAG = LogUtils.makeLogTag(this.getClass());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_splashscreen);

        PlanupSyncAdapter.initializeSyncAdapter(this);

        String url = "https://planup-backend.herokuapp.com";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LogUtils.LOGD(TAG, response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.LOGV(TAG, error + "");
            }
        });

        //VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions("email", "user_friends");

        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                LogUtils.LOGD(TAG, "Success" + loginResult.getAccessToken());

//                new GraphRequest(
//                        AccessToken.getCurrentAccessToken(),
//                        "/" + AccessToken.getCurrentAccessToken().getUserId(),
//                        null,
//                        HttpMethod.GET,
//                        new GraphRequest.Callback() {
//                            public void onCompleted(GraphResponse response) {
//                                LogUtils.LOGD(TAG, response.toString());
//                            }
//                        }
//                ).executeAsync();

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                LogUtils.LOGD(TAG, response.toString());

                                // Application code
                                String email = null;
                                try {
                                    email = object.getString("email");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                LogUtils.LOGD(TAG, "email " + email);

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

