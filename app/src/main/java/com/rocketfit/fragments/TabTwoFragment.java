package com.rocketfit.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rocketfit.activities.FriendActivity;
import com.rocketfit.activities.SearchableActivity;

import java.util.ArrayList;
import java.util.List;

import projects.rocketfit.R;

public class TabTwoFragment extends android.support.v4.app.Fragment {

    private ListView listOfFriends;
    private View recentItems;
    private TextView eMessage;
    private boolean onStopCalled = false;
    public ParseUser userToQuery;
    public String friendUsername;

    ArrayList<String> friends = new ArrayList<String>();
    ArrayList<String> values = new ArrayList<String>();

    private ProgressBar mLoading;
    // Defined Array values to show in ListView

    @Override
    public void onCreate(Bundle savedInstanceState) {
        onStopCalled = false;

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        onStopCalled = false;

        super.onResume();

        // Do nothing.. keep it the same
    }

    @Override
    public void onStop() {
        onStopCalled = true;


        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String user = getArguments().getString("user");
        //       Toast.makeText(getActivity(), user, Toast.LENGTH_SHORT).show();

        if (recentItems == null) {

            recentItems = inflater.inflate(R.layout.fragment_tab_two, container, false);

            mLoading = (ProgressBar) recentItems.findViewById(R.id.progressbar_loading);
            mLoading.setVisibility(View.VISIBLE);
            eMessage=(TextView) recentItems.findViewById(R.id.errorMessage);

            // Handler is called when thread is complete that sends data to parse below
            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    mLoading.setVisibility(View.GONE);

                    if (values.size() == 0) {
                        eMessage.setVisibility(View.VISIBLE);
                        eMessage.setText("\n\nNo friends to display!");
                    }
                    // Get ListView object from xml
                    listOfFriends = (ListView) recentItems.findViewById(R.id.listOfFriends);
                    // Define a new Adapter
                    // First parameter - Context
                    // Second parameter - Layout for the row
                    // Third parameter - ID of the TextView to which the data is written
                    // Forth - the Array of data

                    //  ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.custom, R.id.listTextView, values);
                    if (!onStopCalled) {

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.custom2, R.id.listTextView, values);

                        // Assign adapter to ListView
                        listOfFriends.setAdapter(adapter);

                    }

                    // ListView Item Click Listener
                    listOfFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {

                            // ListView Clicked item index
                            int itemPosition = position;

                            // ListView Clicked item value
                            String itemValue = (String) listOfFriends.getItemAtPosition(position);

                            Intent i = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                            i.putExtra("friend", friendUsername);
                            startActivity(i);

                        }

                    });
                }
            };

            // creates thread and gets user workout data to Parse
            Thread mThread = new Thread() {
                @Override
                public void run() {

                    // Submit workout
                    ParseUser currentUser = ParseUser.getCurrentUser();

                    if (user == currentUser.getUsername()) {
                        userToQuery = currentUser;
                    } else {
                        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
                        query.whereEqualTo("username", user);
                        try {
                            userToQuery = query.getFirst();

                        } catch(ParseException e1) {

                        }
                    }

                    ParseQuery<ParseObject> friendQuery = userToQuery.getRelation("friends").getQuery();
                    friendQuery.orderByDescending("username");
                    try {
                        List<ParseObject> fObjects = friendQuery.find();

                        for (int i = 0; i < fObjects.size(); i++) {
                            ParseObject oneFriend = fObjects.get(i);
                            friendUsername = oneFriend.get("username").toString();
                            String message = "     " + oneFriend.get("fullname").toString() + "    (" + friendUsername + ")";

                            values.add(i, message);
                            friends.add(i, friendUsername);
                        }
                    } catch (ParseException e2) {

                    }
                    handler.sendEmptyMessage(0); // sends message to handle after comple
                }
            };
            mThread.start();
            // TextView tv = (TextView) v.findViewById(R.id.text);
            //  tv.setText(this.getTag() + " Content");
        } else {
            ((ViewGroup) recentItems.getParent()).removeView(recentItems);
        }
        return recentItems;
    }
}


