package com.rocketfit.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;


import projects.rocketfit.R;

public class TabTwoFragment extends android.support.v4.app.Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_two, container, false);
        TextView tv = (TextView) v.findViewById(R.id.text);
        //tv.setText(this.getTag() + " Content");
        tv.setText("Coming soon...");
        return v;
    }

}
