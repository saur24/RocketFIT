package com.rocketfit.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.rocketfit.fragments.TabOneFragment;
import com.rocketfit.fragments.TabThreeFragment;
import com.rocketfit.fragments.TabTwoFragment;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import projects.rocketfit.R;

public class ProfileActivity extends FragmentActivity {
    // Attempt to pull facebook shit
    private ProfilePictureView profilePictureView;
    private TextView memberSinceView;
    private TextView userNameView;
    private ImageView profileImage;
    private FragmentTabHost mTabHost;
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage    = (ImageView) findViewById(R.id.profile_image);
        userNameView    = (TextView)  findViewById(R.id.userNameView);
        memberSinceView = (TextView)  findViewById(R.id.memberSinceView);

        // Get current parse users
        ParseUser currentUser = ParseUser.getCurrentUser();

        // Set the date format
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date date = currentUser.getCreatedAt();
        String sDate = df.format(date);

        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            userNameView.setText(currentUser.get("fullname").toString());
        }

        if (currentUser.get("fullname") != null) {
            userNameView.setText(currentUser.get("fullname").toString());
        }

        // Set member since ..... view
        memberSinceView.setText("Member since " + sDate);

        // ProfilePictureView profilePicture = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator("Recent", null),
                TabOneFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator("Friends", null),
                TabTwoFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator("Awards", null),
                TabThreeFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator("Stats", null),
                TabThreeFragment.class, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            getMenuInflater().inflate(R.menu.menu_profilefb, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_profile, menu);
        }

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
       // searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

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

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        if (id == R.id.action_search) {

            return true;
        }

        if (id == R.id.action_edit) {

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Edit Name")
                    .setMessage("Enter name below")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable value = input.getText();
                            mName = value.toString();
                            userNameView.setText(mName);
                            String[] names = {};

                            ParseUser.getCurrentUser().put("fullname", mName);
                            if (mName.contains(" ")) {
                                names = mName.split(" ");
                                ParseUser.getCurrentUser().put("firstname", names[0]);
                                ParseUser.getCurrentUser().put("lastname", names[1]);

                            } else {
                                names[0] = mName;
                                ParseUser.getCurrentUser().put("firstname", names[0]);
                            }

                            ParseUser.getCurrentUser().saveEventually();

                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}