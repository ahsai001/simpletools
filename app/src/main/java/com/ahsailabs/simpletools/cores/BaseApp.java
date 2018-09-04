package com.ahsailabs.simpletools.cores;

import com.ahsailabs.simpletools.models.ReadQuranLogModel;
import com.google.android.gms.ads.MobileAds;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.core.BaseApplication;
import com.zaitunlabs.zlcore.models.AppListDataModel;
import com.zaitunlabs.zlcore.models.AppListModel;
import com.zaitunlabs.zlcore.models.AppListPagingModel;
import com.zaitunlabs.zlcore.models.InformationModel;
import com.zaitunlabs.zlcore.models.StoreDataModel;
import com.zaitunlabs.zlcore.models.StoreModel;
import com.zaitunlabs.zlcore.models.StorePagingModel;

/**
 * Created by ahsai on 5/16/2018.
 */

public class BaseApp extends BaseApplication {

    @Override
    public void onCreate() {
        addDBModelClass(InformationModel.class);
        addDBModelClass(AppListModel.class);
        addDBModelClass(AppListDataModel.class);
        addDBModelClass(AppListPagingModel.class);
        addDBModelClass(StoreModel.class);
        addDBModelClass(StoreDataModel.class);
        addDBModelClass(StorePagingModel.class);
        addDBModelClass(ReadQuranLogModel.class);

        APIConstant.setApiAppid("3");
        APIConstant.setApiKey("ewrwerasdf242341234asfsdf");
        APIConstant.setApiVersion("v1");


        super.onCreate();


        MobileAds.initialize(this,
                "ca-app-pub-3647411985348830~8833470581");
    }
}
