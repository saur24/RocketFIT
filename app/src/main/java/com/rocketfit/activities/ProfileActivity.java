package com.rocketfit.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.rocketfit.fragments.TabOneFragment;
import com.rocketfit.fragments.TabThreeFragment;
import com.rocketfit.fragments.TabTwoFragment;
import android.widget.TabHost.OnTabChangeListener;


import projects.rocketfit.R;

public class ProfileActivity extends FragmentActivity {
    // Attempt to pull facebook shit
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    private ImageView profileImage;
    private FragmentTabHost mTabHost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = (ImageView) findViewById(R.id.profile_image);


        // ProfilePictureView profilePicture = (ProfilePictureView) findViewById(R.id.selection_profile_pic);

        // Attempt to pull facebook shit

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator("Recent Workouts", null),
                TabOneFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator("Stats", null),
                TabTwoFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator("Friends", null),
                TabThreeFragment.class, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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
}
