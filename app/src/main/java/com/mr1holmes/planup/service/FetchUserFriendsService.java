package com.mr1holmes.planup.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.mr1holmes.planup.data.PlanupContract;
import com.mr1holmes.planup.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fetch friend's details and add it to database
 */
public class FetchUserFriendsService extends IntentService {

    private static final String TAG = LogUtils.makeLogTag(FetchUserFriendsService.class);

    public FetchUserFriendsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.LOGV(TAG, "Fetching user friends started");

        GraphRequest.Callback graphCallback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject responseObject = response.getJSONObject();
                try {
                    JSONArray data = responseObject.getJSONObject("friends").getJSONArray("data");
                    for (int index = 0; index < data.length(); index++) {
                        JSONObject friendObject = data.getJSONObject(index);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PlanupContract.TABLE_USER.COLUMN_USER_ID, friendObject.getString("id"));
                        contentValues.put(PlanupContract.TABLE_USER.COLUMN_FIRST_NAME, friendObject.getString("first_name"));
                        contentValues.put(PlanupContract.TABLE_USER.COLUMN_LAST_NAME, friendObject.getString("last_name"));
                        contentValues.put(PlanupContract.TABLE_USER.COLUMN_PROFILE_URL, DatabaseUtils.sqlEscapeString(
                                friendObject.getJSONObject("picture").getJSONObject("data").getString("url")));

                        getContentResolver().insert(PlanupContract.TABLE_USER.CONTENT_URI, contentValues);
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }

                //get next batch of results if exists
                GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                if (nextRequest != null) {
                    nextRequest.setCallback(this);
                    nextRequest.executeAndWait();
                } else {
                    LogUtils.LOGV(TAG, "End of Fetch user paging");
                }

            }
        };
        Bundle parameters = new Bundle();
        parameters.putString("fields", "friends{id,first_name,last_name,picture{url}}");

        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "me", parameters, HttpMethod.GET, graphCallback).executeAndWait();

    }
}
