package com.ahsailabs.simpletools.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.ahsailabs.simpletools.R;
import com.ahsailabs.simpletools.databinding.ActivityReadQuranLogBinding;
import com.ahsailabs.simpletools.fragments.ReadQuranLogActivityFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zaitunlabs.zlcore.core.BaseActivity;

public class ReadQuranLogActivity extends BaseActivity{
    ReadQuranLogActivityFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReadQuranLogBinding binding = ActivityReadQuranLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        enableUpNavigation();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



        fragment = showFragment(R.id.fragment,ReadQuranLogActivityFragment.class,null, savedInstanceState, "readquranlog");
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
        Intent intent = new Intent(context,ReadQuranLogActivity.class);
        context.startActivity(intent);
    }
}
