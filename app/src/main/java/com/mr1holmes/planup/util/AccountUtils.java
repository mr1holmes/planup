package com.mr1holmes.planup.util;

import com.facebook.AccessToken;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by mr1holmes on 7/7/16.
 */
public class AccountUtils {

    public static final String FACEBOOK_PERMISSIONS[] = {"email", "user_friends"};

    public static boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return (accessToken != null &&
                accessToken.getPermissions().containsAll(new HashSet<>(Arrays.asList(FACEBOOK_PERMISSIONS))));
    }
}
