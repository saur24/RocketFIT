package com.rocketfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import projects.rocketfit.R;

public class TabOneFragment extends android.support.v4.app.Fragment {
    private ListView recentWorkouts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View recentItems = inflater.inflate(R.layout.fragment_tab_one, container, false);

        // Get ListView object from xml
        recentWorkouts = (ListView) recentItems.findViewById(R.id.recentWorkouts);

        // Defined Array values to show in ListView
        String[] values = new String[]{"Workout 1",
                "Workout 2",
                "Workout 3",
                "Workout 4",
                "Workout 5",
                "Workout 6",
                "Workout 7",
                "Workout 8"
        };

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

                // Show Alert
                Toast.makeText(getActivity().getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();
            }

        });
        // TextView tv = (TextView) v.findViewById(R.id.text);
        //  tv.setText(this.getTag() + " Content");
        return recentItems;
    }

}
