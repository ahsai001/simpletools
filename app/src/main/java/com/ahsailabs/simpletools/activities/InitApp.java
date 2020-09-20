package com.ahsailabs.simpletools.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ahsailabs.simpletools.R;
import com.zaitunlabs.zlcore.activities.BaseSplashActivity;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.utils.CommonUtil;

/**
 * Created by ahsai on 5/18/2018.
 */

public class InitApp extends BaseSplashActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackgroundPaneColor(R.color.colorPrimary);
        setImageIcon(R.drawable.logo);
        setBottomTextView(getString(R.string.app_name)+" v"+ CommonUtil.getVersionName(this), R.color.colorAccent);
    }

    @Override
    protected String getCheckVersionUrl() {
        return APIConstant.API_CHECK_VERSION;
    }

    @Override
    protected boolean doNextAction() {
        MainActivity.start(this);
        return true;
    }

    @Override
    protected boolean isMeidIncluded() {
        return true;
    }

    @Override
    protected int getMinimumSplashTimeInMS() {
        return 3000;
    }
}
