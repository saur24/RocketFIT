package com.rocketfit.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import projects.rocketfit.R;

public class TabOneFragment extends android.support.v4.app.Fragment {
    public static int NUMBER_OF_RECENT_WORKOUTS = 5;
    private ListView recentWorkouts;
    private View recentItems;
    private TextView eMessage;
    private boolean onStopCalled = false;
    public ParseUser userToQuery;

    ArrayList<StringBuilder> wSum = new ArrayList<StringBuilder>();
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

            recentItems = inflater.inflate(R.layout.fragment_tab_one, container, false);

            mLoading = (ProgressBar) recentItems.findViewById(R.id.progressbar_loading);
            mLoading.setVisibility(View.VISIBLE);
            eMessage=(TextView) recentItems.findViewById(R.id.errorMessage);

            // Handler is called when thread is complete that sends data to parse below
            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    mLoading.setVisibility(View.GONE);

                    if (values.size() == 0) {
                        eMessage.setVisibility(View.VISIBLE);
                        eMessage.setText("You have no recent workouts!");
                    }

                    if (!CheckInternet(getActivity().getApplicationContext())) {
                        eMessage.setVisibility(View.VISIBLE);
                        eMessage.setText("Please check your Internet connection!");
                    }

                    // Get ListView object from xml
                    recentWorkouts = (ListView) recentItems.findViewById(R.id.recentWorkouts);
                    // Define a new Adapter
                    // First parameter - Context
                    // Second parameter - Layout for the row
                    // Third parameter - ID of the TextView to which the data is written
                    // Forth - the Array of data

                  //  ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.custom, R.id.listTextView, values);
                    if (!onStopCalled) {

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.custom, R.id.listTextView, values);

                        // Assign adapter to ListView
                        recentWorkouts.setAdapter(adapter);

                    }

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
                            workoutSummary.setMessage(Html.fromHtml(wSum.get(itemPosition).toString()));
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

                    if (user == currentUser.getUsername()) {
                        userToQuery = currentUser;
                    } else {
                        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
                        query.whereEqualTo("username", user);
                        try {
                            ParseUser searchedUser = query.getFirst();
                            userToQuery = searchedUser;

                        } catch(ParseException userException) {

                        }
                    }

                    ParseQuery<ParseObject> workoutQuery = userToQuery.getRelation("workouts").getQuery();
                    workoutQuery.orderByDescending("createdAt");
                    workoutQuery.setLimit(NUMBER_OF_RECENT_WORKOUTS);
                    try {
                        List<ParseObject> wObjects = workoutQuery.find();

                        for (int i = 0; i < wObjects.size(); i++) {
                            ParseObject oneWorkout = wObjects.get(i);
                            String createdAt = oneWorkout.getCreatedAt().toString();
                            String message = "    Workout on " + createdAt;

                            wSum.add(i, new StringBuilder());
                            values.add(i, message);

                            ParseQuery<ParseObject> runQuery = oneWorkout.getRelation("run").getQuery();
                            runQuery.orderByAscending("createdAt");
                            try {
                                List<ParseObject> objects = runQuery.find();

                                if (objects.size() != 0) {
                                    wSum.get(i).append(String.format("<b><u>%s</u></b>", "Run") + "<br>");
                                }

                                for (int k = 0; k < objects.size(); k++) {
                                    ParseObject oneRun = objects.get(k);

                                    if(objects.size() > 1) {

                                        String runCreatedAt = oneRun.getCreatedAt().toString();
                                        String runMessage = "Run on " + runCreatedAt;

                                        wSum.get(i).append(String.format("<b>%s</b>", runMessage) + "<br>");
                                    }
                                    wSum.get(i).append("Time: " + oneRun.get("totalTime").toString() + "<br>");
                                    wSum.get(i).append("Distance: " + oneRun.get("totalDistance").toString() + " miles" + "<br><br>");
                                }

                            } catch (ParseException e2) {

                            }

                            ParseQuery<ParseObject> setsQuery = oneWorkout.getRelation("sets").getQuery();
                            setsQuery.orderByAscending("createdAt");
                            String strMachine = "", lastMachine = "";
                            try {
                                List<ParseObject> objects = setsQuery.find();

                                if (objects.size() != 0) {
                                    wSum.get(i).append(String.format("<b><u>%s</u></b>", "Weights") + "<br>");
                                }

                                for (int z = 0; z < objects.size(); z++) {
                                    ParseObject oneSet = objects.get(z);

                                    ParseObject machine;
                                    ParseQuery<ParseObject> machineQuery = oneSet.getRelation("machineId").getQuery();
                                    try {
                                        machine = machineQuery.getFirst();
                                        strMachine = machine.get("name").toString();
                                        Log.i("MACHINE", strMachine);

                                        if (i == 0) {
                                            wSum.get(i).append(String.format("<i>%s</i>", strMachine) + "<br>");
                                        } else if (!(lastMachine.compareTo(strMachine) == 0)) {
                                            wSum.get(i).append(String.format("<br>" + "<i>%s</i>", strMachine) + "<br>");
                                        }
                                    } catch (ParseException e1) {

                                    }

                                    wSum.get(i).append("Reps: " + oneSet.get("repetitions").toString() + "&nbsp;&nbsp;&nbsp;&nbsp;");
                                    wSum.get(i).append("Weight: " + oneSet.get("resistance").toString() + " lbs" + "<br>");

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

    public boolean CheckInternet(Context ctx) {
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        return wifi.isConnected() || mobile.isConnected();
    }
}
