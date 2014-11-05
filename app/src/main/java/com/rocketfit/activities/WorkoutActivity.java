package com.rocketfit.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import projects.rocketfit.R;

public class WorkoutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        //ImageButton exerciseButton = (ImageButton) findViewById(R.id.exerciseButton);

        //handle entering from tapping NFC tag

        //need icons for Run or Exercise
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

    public void onExerciseSelected(View view) {
        //exercise button was pressed
        Toast.makeText(WorkoutActivity.this, "exercise", Toast.LENGTH_SHORT).show();
    }

    public void onRunSelected(View view) {
        //run button was pressed
        Toast.makeText(WorkoutActivity.this, "run", Toast.LENGTH_SHORT).show();
    }
}