package com.mr1holmes.planup.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by mr1holmes on 1/7/16.
 */
public class TestUtilities extends AndroidTestCase {

    public static ContentValues createUser(String user_id) {
        ContentValues testvalues = new ContentValues();

        testvalues.put(PlanupContract.TABLE_USER.COLUMN_USER_ID, user_id);
        testvalues.put(PlanupContract.TABLE_USER.COLUMN_FIRST_NAME, "Chirag");
        testvalues.put(PlanupContract.TABLE_USER.COLUMN_LAST_NAME, "Chauhan");
        testvalues.put(PlanupContract.TABLE_USER.COLUMN_PROFILE_URL, "http://www.github.com/mr1holmes");
        testvalues.put(PlanupContract.TABLE_USER.COLUMN_FCM_TOKEN, "14988ye8f7s8g233y4guyre");

        return testvalues;
    }

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        valueCursor.moveToFirst();
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
