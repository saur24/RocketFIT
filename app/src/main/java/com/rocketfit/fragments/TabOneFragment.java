package com.rocketfit.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.rocketfit.activities.HomeActivity;
import com.rocketfit.activities.ProfileActivity;
import com.rocketfit.activities.WeightsActivity;

import java.util.Date;
import java.util.List;

import projects.rocketfit.R;

public class TabOneFragment extends android.support.v4.app.Fragment {
    private ListView recentWorkouts;
    private View recentItems;
    public StringBuilder[] wSum = new StringBuilder[5];
    private ProgressBar mLoading;

    // Defined Array values to show in ListView
    private String[] values = new String[5];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        // Do nothing.. keep it the same
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (recentItems == null) {

            recentItems = inflater.inflate(R.layout.fragment_tab_one, container, false);

            mLoading = (ProgressBar) recentItems.findViewById(R.id.progressbar_loading);
            mLoading.setVisibility(View.VISIBLE);

            // create a progress dialog
//            mLoading = new ProgressDialog(getActivity());
//            mLoading.setMessage("Loading Data...");
//            mLoading.setTitle("");
//            mLoading.show();

            // Handler is called when thread is complete that sends data to parse below
            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {
//                    mLoading.dismiss();  // dismiss the dialog
                    mLoading.setVisibility(View.GONE);

                    // Get ListView object from xml
                    recentWorkouts = (ListView) recentItems.findViewById(R.id.recentWorkouts);
                    // Define a new Adapter
                    // First parameter - Context
                    // Second parameter - Layout for the row
                    // Third parameter - ID of the TextView to which the data is written
                    // Forth - the Array of data
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.custom, R.id.listTextView, values);

                    // Assign adapter to ListView
                    recentWorkouts.setAdapter(adapter);

                    // ListView Item Click Listener
                    recentWorkouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {

                            // ListView Clicked item index
                            int itemPosition = position;

                            // ListView Clicked item value
                            String itemValue = (String) recentWorkouts.getItemAtPosition(position);

                            AlertDialog.Builder workoutSummary = new AlertDialog.Builder(getActivity());
                            workoutSummary.setTitle("Your Workout Summary");
                            workoutSummary.setMessage(Html.fromHtml(wSum[itemPosition].toString()));
                            workoutSummary.setCancelable(true);
                            workoutSummary.setPositiveButton("Close Summary",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert2 = workoutSummary.create();
                            alert2.show();
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

                    ParseQuery<ParseObject> workoutQuery = currentUser.getRelation("workouts").getQuery();
                    workoutQuery.orderByDescending("createdAt");
                    workoutQuery.setLimit(5);
                    try {
                        List<ParseObject> wObjects = workoutQuery.find();

                        for (int i = 0; i < wObjects.size(); i++) {
                            ParseObject oneWorkout = wObjects.get(i);
                            String createdAt = oneWorkout.getCreatedAt().toString();
                            values[i] = "    Workout on " + createdAt;
                            wSum[i] = new StringBuilder();

                            ParseQuery<ParseObject> setsQuery = oneWorkout.getRelation("sets").getQuery();
                            setsQuery.orderByAscending("createdAt");
                            String strMachine = "", lastMachine = "";
                            try {
                                List<ParseObject> sObjects = setsQuery.find();

                                for (int k = 0; k < sObjects.size(); k++) {
                                    ParseObject oneSet = sObjects.get(k);

                                    ParseObject machine;
                                    ParseQuery<ParseObject> machineQuery = oneSet.getRelation("machineId").getQuery();
                                    try {
                                        machine = machineQuery.getFirst();
                                        strMachine = machine.get("name").toString();

                                        if (k == 0) {
                                            wSum[i].append(String.format("<b>%s</b>", strMachine) + "<br>");
                                        } else if (!(lastMachine.compareTo(strMachine) == 0)) {
                                            wSum[i].append(String.format("<br>" + "<b>%s</b>", strMachine) + "<br>");
                                        }

                                    } catch (ParseException e1) {

                                    }

                                    wSum[i].append("Reps: " + oneSet.get("repetitions").toString() + "&nbsp;&nbsp;&nbsp;&nbsp;");
                                    wSum[i].append("Weight: " + oneSet.get("resistance").toString() + " lbs" + "<br>");

                                    lastMachine = strMachine;
                                }
                            } catch (ParseException e2) {

                            }
                        }

                    } catch (ParseException eWorkout) {

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
