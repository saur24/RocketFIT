package com.rocketfit.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;

import projects.rocketfit.R;

public class RunActivity extends Activity {

    private static final String TAG = "BEACON";
    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

    Chronometer chrono;
    TextView debug;
    Button btnStart;
    Button btnStop;
    Button btnReset;
    long lastStop = 0;
    Boolean resume = false;
    short lapCount = 0;
    int beaconHits = 0;
    double accuracyLevel = 0.5;
    double currentLapTime = 0, totalTime = 0, previousLapTimes = 0;
    Boolean inBeaconRange = false;

    private BeaconManager beaconManager = new BeaconManager(this);

    private void isNewLap() {
        if (beaconHits % 4 == 0) {
            addLap();
            lapCount++;

            previousLapTimes = totalTime;
            totalTime = SystemClock.elapsedRealtime();
            currentLapTime = totalTime - previousLapTimes;

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
            lapNum.setText("Lap " + lapCount);

            lapTime.setLayoutParams(new TableRow.LayoutParams(0, TableLayout.LayoutParams.MATCH_PARENT, 1f));
            lapTime.setGravity(Gravity.CENTER);
            lapTime.setText("" + currentLapTime);

        // add data to array???
        //    allReps.add(reps);                                                                                                              // Add to the list of reps
        //    allWeights.add(weight);

            //add your new row to the TableLayout:
            TableLayout table = (TableLayout) findViewById(R.id.runTable);
            table.addView(row);
       }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnStart:

                if (resume) {
                    chrono.setBase(chrono.getBase() + SystemClock.elapsedRealtime() - lastStop);
                    chrono.start();
                } else {
                    chrono.setBase(SystemClock.elapsedRealtime());
                    chrono.start();
                }

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                break;

            case R.id.btnStop:

                lastStop = SystemClock.elapsedRealtime();
                chrono.stop();
                resume = true;

                btnStart.setText("Resume");
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnReset.setEnabled(true);
                break;

            case R.id.btnReset:

                chrono.stop();
                chrono.setText("00:00");
                resume = false;

                btnStart.setText("Start");
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnReset.setEnabled(false);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        chrono = (Chronometer) findViewById(R.id.chrono);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnReset = (Button) findViewById(R.id.btnReset);
        debug = (TextView) findViewById(R.id.debugText);

        btnStop.setEnabled(false);
        btnReset.setEnabled(false);

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
                Toast.makeText(getApplicationContext(), "Entered beacon region.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.d(TAG, "Exited beacon region.");
                Toast.makeText(getApplicationContext(), "Exited beacon region.", Toast.LENGTH_SHORT).show();
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
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
//                    beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS);
                    Log.i(TAG, "Ranging/monitoring started.");
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start ranging/monitoring", e);
                }
            }
        });
    }

    @Override
    protected void onStop() {

        // Should be invoked in #onStop.
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
//            beaconManager.stopMonitoring(ALL_ESTIMOTE_BEACONS);
            Log.i(TAG, "Ranging/monitoring stopped.");
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot stop but it does not matter now", e);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {

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

        return super.onOptionsItemSelected(item);
    }
}
