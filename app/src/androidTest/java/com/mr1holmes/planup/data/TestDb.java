package com.mr1holmes.planup.data;

import android.test.AndroidTestCase;

/**
 * Created by mr1holmes on 30/6/16.
 */
public class TestDb extends AndroidTestCase {

    public void setUp() {
        deleteTheDatabase();
    }

    public void deleteTheDatabase() {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }
}
