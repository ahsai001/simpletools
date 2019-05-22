package com.ahsailabs.simpletools.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ahsailabs.simpletools.R;
import com.ahsailabs.simpletools.fragments.SendWAActivityFragment;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.utils.CommonUtils;

import java.io.UnsupportedEncodingException;

public class SendWAActivity extends BaseActivity {
    SendWAActivityFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_wa);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableUpNavigation();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragment != null) {
                    try {
                        fragment.sendWA();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        CommonUtils.showSnackBar(SendWAActivity.this, "please edit your message, some words not supported");
                    }
                }
            }
        });

        fragment = showFragment(R.id.fragment,SendWAActivityFragment.class,null, savedInstanceState, "sendwa");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void start(Context context){
        Intent intent = new Intent(context,SendWAActivity.class);
        context.startActivity(intent);
    }

}
