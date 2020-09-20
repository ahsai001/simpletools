package com.ahsailabs.simpletools.cores;

import android.content.Context;

import com.ahsailabs.simpletools.models.ReadQuranLogModel;
import com.ahsailabs.simpletools.models.ReadQuranLogModelSQLWHelper;
import com.ahsailabs.sqlitewrapper.SQLiteWrapper;
import com.google.android.gms.ads.MobileAds;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.core.BaseApplication;

/**
 * Created by ahsai on 5/16/2018.
 */

public class BaseApp extends BaseApplication {
    public static final String DATABASE_NAME = "simpletools.db";

    @Override
    public void onCreate() {
        APIConstant.setApiAppid("3");
        APIConstant.setApiKey("ewrwerasdf242341234asfsdf");
        APIConstant.setApiVersion("v1");

        super.onCreate();

        SQLiteWrapper.addDatabase(new SQLiteWrapper.Database() {
            @Override
            public Context getContext() {
                return getApplicationContext();
            }

            @Override
            public String getDatabaseName() {
                return DATABASE_NAME;
            }

            @Override
            public int getDatabaseVersion() {
                return 1;
            }

            @Override
            public void configure(SQLiteWrapper sqLiteWrapper) {
                ReadQuranLogModelSQLWHelper.designTable(sqLiteWrapper);
            }
        });


        MobileAds.initialize(this);
    }
}
