package com.ahsailabs.simpletools.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ahsailabs.simpletools.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.zaitunlabs.zlcore.activities.AppListActivity;
import com.zaitunlabs.zlcore.activities.MessageListActivity;
import com.zaitunlabs.zlcore.activities.StoreActivity;
import com.zaitunlabs.zlcore.events.InfoCounterEvent;
import com.zaitunlabs.zlcore.modules.about.AboutUs;
import com.zaitunlabs.zlcore.services.FCMIntentService;
import com.zaitunlabs.zlcore.tables.InformationModel;
import com.zaitunlabs.zlcore.utils.EventsUtil;
import com.zaitunlabs.zlcore.utils.PermissionUtil;

import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView messageItemView;
    private PermissionUtil permissionUtils;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(null);

        permissionUtils = PermissionUtil.checkPermissionAndGo(this, 1003, new Runnable() {
            @Override
            public void run() {

            }
        }, android.Manifest.permission.READ_PHONE_STATE);


        messageItemView = (TextView) navigationView.getMenu().
                findItem(R.id.nav_message).getActionView();

        EventsUtil.register(this);
        reCountMessage();


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FCMIntentService.startSending(this,"3", false, true);
    }

    @Subscribe
    public void onEvent(InfoCounterEvent event){
        reCountMessage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventsUtil.unregister(this);
    }

    private void reCountMessage(){
        if(messageItemView != null) {
            messageItemView.setGravity(Gravity.CENTER_VERTICAL);
            messageItemView.setTypeface(null, Typeface.BOLD);
            messageItemView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            messageItemView.setText(""+ InformationModel.unreadInfoCount());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_linkwa) {
            LinkWAActivity.start(this);
        } else if (id == R.id.nav_sendwa) {
            SendWAActivity.start(this);
        } else if (id == R.id.nav_readquranlog) {
            ReadQuranLogActivity.start(this);
        } else   if (id == R.id.nav_about) {
            AboutUs.start(this,R.mipmap.ic_launcher,0,R.string.share_title,R.string.share_body_template,
                    0,R.string.feedback_mail_to, R.string.feedback_title, R.string.feedback_body_template,
                    0,R.raw.version_change_history, true, "https://www.ahsai001.com",
                    false, "ahsai001", "https://www.ahsai001.com", getString(R.string.feedback_mail_to),R.mipmap.ic_launcher,"2018\nAll right reserved",
                    R.color.colorPrimary, ContextCompat.getColor(this,android.R.color.white),ContextCompat.getColor(this,android.R.color.white), null, true);
        } else if (id == R.id.nav_app_list) {
            AppListActivity.start(this, true);
        } else if (id == R.id.nav_store) {
            StoreActivity.start(this, true);
        } else if (id == R.id.nav_message) {
            MessageListActivity.start(this, true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void start(Context context){
        Intent intent = new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }
}
