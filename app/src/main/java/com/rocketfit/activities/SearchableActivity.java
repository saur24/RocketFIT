package com.rocketfit.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.support.v4.view.MenuItemCompat;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import projects.rocketfit.R;

public class SearchableActivity extends Activity {
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        } else {
            String friend = "";
            Bundle extras= getIntent().getExtras();
            if(extras!=null)
            {
                friend = extras.getString("friend"); // get the value based on the key
                doMySearch(friend);
            }
        }
    }

    public void doMySearch(final String q) {
        spinner.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("username", q);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SearchableActivity.this);
                    TextView myMsg = new TextView(getApplicationContext());
                    myMsg.setText("\nClick 'Ok' to proceed back to your profile.\n\n" +
                            "Click 'Help' for instructions on how to optimally search for a user\n");
                    myMsg.setTextColor(Color.BLACK);
                    myMsg.setPadding(10,10,10,10);
                    myMsg.setTextSize(14);
                    builder1.setTitle("Couldn't Find User");
                    builder1.setView(myMsg);
                    builder1.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            });
                    builder1.setNegativeButton("HELP",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(i);
                                    finish();
                        }
                    });

                    AlertDialog alert1 = builder1.create();
                    alert1.show();

                } else {
                    Boolean isFriend = false;
                    ParseObject friend;

                    // check if user is a friend
                    ParseQuery<ParseObject> friendQuery = ParseUser.getCurrentUser().getRelation("friends").getQuery();
                    friendQuery.whereEqualTo("username", q);
                    try {
                        friendQuery.getFirst();
                        isFriend = true;
                    } catch (ParseException e1) {

                    }

                    Intent i = new Intent(getApplicationContext(), FriendActivity.class);
                    i.putExtra("friend", q);
                    i.putExtra("isFriend", isFriend);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);

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
