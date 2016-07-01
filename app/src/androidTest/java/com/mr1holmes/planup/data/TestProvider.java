package com.mr1holmes.planup.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.test.AndroidTestCase;

/**
 * Created by mr1holmes on 30/6/16.
 */
public class TestProvider extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                PlanupContract.TABLE_USER.CONTENT_URI,
                null,
                null);

        Cursor cursor = mContext.getContentResolver().query(
                PlanupContract.TABLE_USER.CONTENT_URI,
                null,
                null,
                null,
                null);
        assertEquals("Error: Records not deleted from User table during delete", 0, cursor.getCount());

        cursor.close();
    }

    public void testProviderRegistry() {
        PackageManager packageManager = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(), PlanupProvider.class.getName());

        try {
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = packageManager.getProviderInfo(componentName, 0);

            assertEquals("Error: PlanupProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + PlanupContract.CONTENT_AUTHORITY,
                    providerInfo.authority, PlanupContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: PlanupProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }
}
