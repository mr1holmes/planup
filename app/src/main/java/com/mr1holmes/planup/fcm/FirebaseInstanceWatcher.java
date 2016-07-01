package com.mr1holmes.planup.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by mr1holmes on 29/6/16.
 */
public class FirebaseInstanceWatcher extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // TODO: Send new token to server
    }
}
