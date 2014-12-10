package com.rocketfit.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import projects.rocketfit.R;

public class RunActivity extends Activity {

    private static final String TAG = "BEACON";
    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
    private static final double LANE_1 = 0.164205;
    private static final double LANE_2 = 0.167424;
    private static final double LANE_3 = 0.171023;
    private static final double LANE_4 = 0.174811;

    Chronometer chrono;
    TextView debug, defaultMsg, ble_not_supported;
    Button btnStart;
    Button btnPause;
    Button btnSubmit;
    long lastStop = 0;
    Boolean resume = false;
    short lapCount = 0;
    int beaconHits = -1;
    double accuracyLevel = 0.5;
    double totalRunDistance = 0;
    long currentLapTime = 0, totalTime = 0, previousLapTimes = 0, minutes, seconds;
    String currentLap = "";
    Boolean inBeaconRange = false;
    private ParseObject workout;
    public List<String> allLapTimes = new ArrayList<String>();
    public List<Short> allLapNums = new ArrayList<Short>();
    private Spinner mSelectLane;
    private ProgressDialog mLoading;
    int lane;
    public StringBuilder wSum = new StringBuilder();

    private BeaconManager beaconManager = new BeaconManager(this);

    private void isNewLap() {

        if (beaconHits % 4 == 0) {
            if(beaconHits != 0) {
                lapCount++;

                previousLapTimes = totalTime;
                totalTime = SystemClock.elapsedRealtime() - chrono.getBase();
                currentLapTime = totalTime - previousLapTimes;

                minutes = ((currentLapTime) / 1000) / 60;
                seconds = ((currentLapTime) / 1000) % 60;
                currentLap = minutes + ":" + seconds;

                addLap();
            }
        }
    }

    public void addLap() {
        //add new set
//            //create a new row to add
            TableRow row = new TableRow(RunActivity.this);

            //add Layouts to your new row
            TextView lapNum = new TextView(RunActivity.this);
            TextView lapTime = new TextView(RunActivity.this);

            // add reps/weights to your table
            row.addView(lapNum);
            row.addView(lapTime);

            lapNum.setLayoutParams(new TableRow.LayoutParams(0, TableLayout.LayoutParams.MATCH_PARENT, 1f));                                // Center reps and weights on screen
            lapNum.setGravity(Gravity.CENTER);                                                                                              // Center user input
            lapNum.setTextSize(28);
            lapNum.setText("Lap " + lapCount);


            lapTime.setLayoutParams(new TableRow.LayoutParams(0, TableLayout.LayoutParams.MATCH_PARENT, 1f));
            lapTime.setGravity(Gravity.CENTER);
            lapTime.setTextSize(40);
            lapTime.setText(currentLap);

        // add data to array
            allLapNums.add(lapCount);                                                                                                              // Add to the list of reps
            allLapTimes.add(lapTime.getText().toString());

            //add your new row to the TableLayout:
            TableLayout table = (TableLayout) findViewById(R.id.runTable);
            table.addView(row, 0);
       }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnStart:

                if (resume) {
                    chrono.setBase(chrono.getBase() + SystemClock.elapsedRealtime() - lastStop);
                    try {
                        beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                        Log.i(TAG, "Ranging started.");
                    } catch (RemoteException e) {
                        Log.e(TAG, "Cannot start ranging", e);
                    }
                    chrono.start();
                } else {
                    chrono.setBase(SystemClock.elapsedRealtime());

                    // Should be invoked in #onStart.
                    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                        @Override
                        public void onServiceReady() {
                            try {
                                beaconManager.stopMonitoring(ALL_ESTIMOTE_BEACONS);
                                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                                Log.i(TAG, "Ranging started.");
                            } catch (RemoteException e) {
                                Log.e(TAG, "Cannot start ranging", e);
                            }
                        }
                    });

                    chrono.start();
                }

                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                break;

            case R.id.btnPause:

                lastStop = SystemClock.elapsedRealtime();
                chrono.stop();
                try {
                    beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
                } catch (RemoteException e) {

                }
                resume = true;

                btnStart.setText("Resume");
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnSubmit.setEnabled(true);
                break;

            case R.id.btnSubmit:

                final String totalRunTime = chrono.getText().toString();

                if(lane == 1){
                    totalRunDistance = LANE_1 * lapCount;
                } else if (lane == 2) {
                    totalRunDistance = LANE_2 * lapCount;
                } else if (lane == 3) {
                    totalRunDistance = LANE_3 * lapCount;
                } else if (lane == 4) {
                    totalRunDistance = LANE_4 * lapCount;
                }

                chrono.stop();

                TableLayout table = (TableLayout) findViewById(R.id.runTable);           // Used to reset the reps / sets

                // Create a relation between the Run and the Laps
                final ParseObject myRun = new ParseObject("Run");

                myRun.put("totalTime", totalRunTime);
                myRun.put("totalDistance", totalRunDistance);

                for(int i = 0; i < allLapNums.size(); i++) {

                    final ParseObject myLap = new ParseObject("Lap");

                    myLap.put("lapNum", allLapNums.get(i));
                    myLap.put("lapTime", allLapTimes.get(i));
                    myLap.put("lane", lane);


                    // This will save the lap
                    myLap.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                //myObjectSavedSuccessfully();
                                ParseRelation<ParseObject> runRelation = myRun.getRelation("laps");
                                runRelation.add(myLap);

                                // This will save the run
                                myRun.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            //myObjectSavedSuccessfully();
                                            ParseRelation<ParseObject> workoutRelation = workout.getRelation("run");
                                            workoutRelation.add(myRun);

                                            workout.saveInBackground();
                                        } else {
                                            //myObjectSaveDidNotSucceed();
                                        }
                                    }
                                });

                            } else {
                                //myObjectSaveDidNotSucceed();
                            }
                        }
                    });
                }

                if((allLapNums.size() == 0) || (allLapTimes.size() == 0)) {
                    Toast.makeText(getApplicationContext(), "Please finish at least one lap before submitting!", Toast.LENGTH_SHORT).show();
                    allLapNums.clear();
                    allLapTimes.clear();
                } else {
                    Toast.makeText(getApplicationContext(), "Run saved!", Toast.LENGTH_SHORT).show();
                    // Reset the layouts for the user
                    table.removeAllViews();
                    btnStart.setText("Start");
                    btnStart.setEnabled(true);
                    btnPause.setEnabled(false);
                    btnSubmit.setEnabled(false);
                }

                chrono.setText("00:00");
                resume = false;

                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        chrono = (Chronometer) findViewById(R.id.chrono);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        debug = (TextView) findViewById(R.id.debugText);
        defaultMsg = (TextView) findViewById(R.id.defaultMsg);
        ble_not_supported = (TextView) findViewById(R.id.ble_not_supported);
        mSelectLane = (Spinner) findViewById(R.id.laneSpinner);

        btnPause.setEnabled(false);
        btnSubmit.setEnabled(false);
        btnStart.setEnabled(false);
        mSelectLane.setOnItemSelectedListener(new SpinnerOnItemSelectedListener());

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            defaultMsg.setVisibility(View.GONE);
            ble_not_supported.setVisibility(View.VISIBLE);
            finish();
        }

        // check if a workout exists
        ParseQuery<ParseObject> workoutQuery = ParseQuery.getQuery("Workout");
        workoutQuery.whereEqualTo("createdBy", ParseUser.getCurrentUser());
        workoutQuery.whereDoesNotExist("finishedAt");
        workoutQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    // an open workout doesn't exist, create one
                    workout = new ParseObject("Workout");
                    workout.put("createdBy", currentUser);
                } else {
                    // an open workout exists... add stuff to existing workout
                    workout = object;
                }
            }
        });

        // default scanPeriod = 1s, waitTime = 0s
        // setting scanPeriod = 500ms (0.5s), waitTime = 0s
        beaconManager.setForegroundScanPeriod(300, 0);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(TAG, "Ranged beacons: " + beacons);
                if (beacons.size() > 0) {
                    debug.setText(String.valueOf(Utils.computeAccuracy(beacons.get(0)))
                            + "\n" + Utils.computeProximity(beacons.get(0)));

                    if(Utils.computeAccuracy(beacons.get(0)) < accuracyLevel) {
                        inBeaconRange = true;
                    } else if(inBeaconRange){
                        beaconHits++;
                        inBeaconRange = false;
                        isNewLap();
                    }

                    // need to set threshold for logging time

                }
            }
        });

        // testing monitoring vs ranging
        // log time on enter and on exit, take the average?
        beaconManager.setBackgroundScanPeriod(500, 0);
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> beacons) {
                Log.d(TAG, "Entered beacon region: " + beacons);
                btnStart.setEnabled(true);
                defaultMsg.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Click start to begin your run!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.d(TAG, "Exited beacon region.");
                btnStart.setEnabled(false);
                defaultMsg.setVisibility(View.VISIBLE);
              //  Toast.makeText(getApplicationContext(), "Exited beacon region.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Should be invoked in #onStart.
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    //  beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                    beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS);
                    Log.i(TAG, "Monitoring started.");
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start monitoring", e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        // Should be invoked in #onStop.
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
            beaconManager.stopMonitoring(ALL_ESTIMOTE_BEACONS);
            Log.i(TAG, "Ranging & monitoring stopped.");
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot stop but it does not matter now", e);
        }

        // When no longer needed. Should be invoked in #onDestroy.
        beaconManager.disconnect();

        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workout, menu);
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

        if (id == R.id.action_logout) {
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

            finish();
            //return true;
        }

        if (id == R.id.action_save) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(RunActivity.this);
            builder1.setMessage("Are you sure you want to submit your workout?");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            if (((workout.get("sets") == null) && (workout.get("run") == null)) || (workout == null)) {
                                Toast.makeText(getApplicationContext(), "Your workout did not save. Please try again!", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            } else {

                                // dismiss the first dialog on "Yes" click
                                dialog.dismiss();

                                // create a progress dialog
                                mLoading = new ProgressDialog(RunActivity.this);
                                mLoading.setMessage("Saving Data...");
                                mLoading.setTitle("");
                                mLoading.show();

                                // Handler is called when thread is complete that sends data to parse below
                                final Handler handler = new Handler() {
                                    public void handleMessage(Message msg) {
                                        mLoading.dismiss();  // dismiss the dialog
                                        Toast.makeText(getApplicationContext(), "Workout saved!", Toast.LENGTH_SHORT).show();
                                        AlertDialog.Builder workoutSummary = new AlertDialog.Builder(RunActivity.this);
                                        workoutSummary.setTitle("Your Workout Summary");
                                        workoutSummary.setMessage(Html.fromHtml(wSum.toString()));
                                        workoutSummary.setCancelable(true);
                                        workoutSummary.setPositiveButton("Close Summary",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        dialog.cancel();
                                                        wSum.setLength(0);
                                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                        startActivity(intent);
                                                        finish();

                                                    }
                                                });
                                        AlertDialog alert2 = workoutSummary.create();
                                        alert2.show();
                                    }
                                };

                                // creates thread and sends user workout data to Parse
                                Thread mThread = new Thread() {
                                    @Override
                                    public void run() {

                                        // Submit workout
                                        ParseUser currentUser = ParseUser.getCurrentUser();

                                        //get current date time with Date()
                                        Date date = new Date();
                                        workout.put("finishedAt", date);

                                        ParseRelation<ParseObject> userRelation = currentUser.getRelation("workouts");
                                        userRelation.add(workout);

                                        // save the user
                                        currentUser.saveInBackground();
                                        // save the workout
                                        workout.saveInBackground();

                                        ParseQuery<ParseObject> runQuery = workout.getRelation("run").getQuery();
                                        runQuery.orderByAscending("createdAt");
                                        try {
                                            List<ParseObject> objects = runQuery.find();

                                            if (objects.size() != 0) {
                                                wSum.append(String.format("<b><u>%s</u></b>", "Run") + "<br>");
                                            }

                                            for (int i = 0; i < objects.size(); i++) {
                                                ParseObject oneRun = objects.get(i);

                                                if(objects.size() > 1) {

                                                    String createdAt = oneRun.getCreatedAt().toString();
                                                    String message = "Run on " + createdAt;

                                                    wSum.append(String.format("<b>%s</b>", message) + "<br>");
                                                }
                                                wSum.append("Time: " + oneRun.get("totalTime").toString() + "<br>");
                                                wSum.append("Distance: " + oneRun.get("totalDistance").toString() + " miles" + "<br><br>");
                                            }

                                        } catch (ParseException e2) {

                                        }

                                        ParseQuery<ParseObject> setsQuery = workout.getRelation("sets").getQuery();
                                        setsQuery.orderByAscending("createdAt");
                                        String strMachine = "", lastMachine = "";
                                        try {
                                            List<ParseObject> objects = setsQuery.find();

                                            if (objects.size() != 0) {
                                                wSum.append(String.format("<b><u>%s</u></b>", "Weights") + "<br>");
                                            }

                                            for (int i = 0; i < objects.size(); i++) {
                                                ParseObject oneSet = objects.get(i);

                                                ParseObject machine;
                                                ParseQuery<ParseObject> machineQuery = oneSet.getRelation("machineId").getQuery();
                                                try {
                                                    machine = machineQuery.getFirst();
                                                    strMachine = machine.get("name").toString();
                                                    Log.i("MACHINE", strMachine);

                                                    if (i == 0) {
                                                        wSum.append(String.format("<i>%s</i>", strMachine) + "<br>");
                                                    } else if (!(lastMachine.compareTo(strMachine) == 0)) {
                                                        wSum.append(String.format("<br>" + "<i>%s</i>", strMachine) + "<br>");
                                                    }
                                                } catch (ParseException e1) {

                                                }

                                                wSum.append("Reps: " + oneSet.get("repetitions").toString() + "&nbsp;&nbsp;&nbsp;&nbsp;");
                                                wSum.append("Weight: " + oneSet.get("resistance").toString() + " lbs" + "<br>");

                                                lastMachine = strMachine;
                                            }

                                        } catch (ParseException e2) {

                                        }

                                        handler.sendEmptyMessage(0); // sends message to handle after comple
                                    }
                                };
                                mThread.start();
                            }
                        }
                    });
            builder1.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert1 = builder1.create();
            alert1.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {

            lane = Integer.parseInt(parent.getItemAtPosition(pos).toString());

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }
}
