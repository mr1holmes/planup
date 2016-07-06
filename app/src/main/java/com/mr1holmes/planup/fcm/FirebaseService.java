package com.mr1holmes.planup.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mr1holmes.planup.util.LogUtils;

/**
 * Created by mr1holmes on 29/6/16.
 */
public class FirebaseService extends FirebaseMessagingService {

    private final static String TAG = FirebaseService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        LogUtils.LOGD(TAG, "Firebase message received " + remoteMessage.toString());
    }
}
