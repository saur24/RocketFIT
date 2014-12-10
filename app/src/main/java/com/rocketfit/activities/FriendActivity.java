package com.rocketfit.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.Freezable;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.rocketfit.fragments.TabOneFragment;
import com.rocketfit.fragments.TabThreeFragment;
import com.rocketfit.fragments.TabTwoFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import projects.rocketfit.R;

public class FriendActivity extends FragmentActivity {
    public static final int KITKAT_VALUE = 1002;
    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;
    private TextView memberSinceView;
    private TextView userNameView;
    private FragmentTabHost mTabHost;
    private String mName;
    private Uri profileImgUri;
    private ImageView profileImage;
    private ParseObject parseFriend;
    private Boolean isFriend;
    private Menu newMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage    = (ImageView) findViewById(R.id.profile_image);
        userNameView    = (TextView)  findViewById(R.id.userNameView);
        memberSinceView = (TextView)  findViewById(R.id.memberSinceView);

        String friend = "";
        Bundle extras= getIntent().getExtras();
        if(extras!=null)
        {
            friend = extras.getString("friend"); // get the value based on the key
            isFriend = extras.getBoolean("isFriend");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
            query.whereEqualTo("username", friend);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        Toast.makeText(FriendActivity.this, "Could not find user", Toast.LENGTH_LONG).show();
                    } else {
                        parseFriend = object;

                        // Set the date format
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        Date date = object.getCreatedAt();
                        String sDate = df.format(date);

                        memberSinceView.setText("Member since " + sDate);

                        if (object.getString("fullname") == null) {
                            userNameView.setText(object.get("username").toString());
                        } else {
                            userNameView.setText(object.get("fullname").toString());
                        }

                        ParseFile fileObject = (ParseFile) object.get("fileImage");

                        try {
                            if (fileObject != null) {
                                byte[] data = fileObject.getData();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                profileImage.setImageBitmap(bitmap);
                            } else {
                                profileImage.setImageDrawable(getResources().getDrawable(R.drawable.default_profile));
                            }
                        } catch (ParseException e1) {
                            e1.getCode();
                        }
                    }
                }
            });
        }

        // attempt to send current user to tab Fragment
        Bundle b = new Bundle();
        b.putString("user", friend);
        //

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator("Recent", null),
                TabOneFragment.class, b);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator("Friends", null),
                TabTwoFragment.class, b);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator("Awards", null),
                TabThreeFragment.class, b);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator("Stats", null),
                TabThreeFragment.class, b);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        if(isFriend) {
            getMenuInflater().inflate(R.menu.menu_isfriend, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_isnotfriend, menu);
        }
//
//        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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

        if(id == R.id.action_addFriend) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendActivity.this);
            TextView myMsg = new TextView(getApplicationContext());
            myMsg.setText("\nAre you sure you would like to add this person?");
            myMsg.setTextColor(Color.BLACK);
            myMsg.setPadding(10,10,10,10);
            myMsg.setTextSize(14);
            builder1.setTitle("Add Friend");
            builder1.setView(myMsg);
            builder1.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // make the connection to the Parse User

                            ParseRelation<ParseObject> friendRelation = ParseUser.getCurrentUser().getRelation("friends");
                            friendRelation.add(parseFriend);

                            Toast.makeText(getApplicationContext(), "Friend successfully added!", Toast.LENGTH_SHORT).show();

                            ParseUser.getCurrentUser().saveInBackground();
                        }
                    });
            builder1.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert1 = builder1.create();
            alert1.show();

            return true;
        }

        if(id == R.id.action_removeFriend) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendActivity.this);
            TextView myMsg = new TextView(getApplicationContext());
            myMsg.setText("\nAre you sure you would like to remove this person?");
            myMsg.setTextColor(Color.BLACK);
            myMsg.setPadding(10,10,10,10);
            myMsg.setTextSize(14);
            builder1.setTitle("Remove Friend");
            builder1.setView(myMsg);
            builder1.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // make the connection to the Parse User

                            ParseRelation<ParseObject> friendRelation = ParseUser.getCurrentUser().getRelation("friends");
                            friendRelation.remove(parseFriend);

                            Toast.makeText(getApplicationContext(), "Friend successfully removed!", Toast.LENGTH_SHORT).show();

                            ParseUser.getCurrentUser().saveInBackground();
                        }
                    });
            builder1.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert1 = builder1.create();
            alert1.show();

            return true;
        }


        if (id == R.id.action_logout) {
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

            finish();
            //return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
