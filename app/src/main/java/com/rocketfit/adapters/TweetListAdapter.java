package com.rocketfit.adapters;

/**
 * Created by Matt on 11/24/2014.
 */
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.app.Activity;
import java.util.List;

import projects.rocketfit.R;
import twitter4j.Status;

public class TweetListAdapter extends ArrayAdapter {
    private Context context;
    private boolean useList = true;
    public TweetListAdapter(Context context, List items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    /** * Holder for the list items. */
    private class ViewHolder{ TextView titleText, idText; }

    /** * * @param position * @param convertView * @param parent * @return */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        Status item = (Status)getItem(position);
        View viewToUse = null;
         // This block exists to inflate the settings list item conditionally based on whether
         // we want to support a grid or list view.
         LayoutInflater mInflater = (LayoutInflater) context .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if(useList){
                viewToUse = mInflater.inflate(R.layout.list_item, null);
            }

            holder = new ViewHolder();
            holder.titleText = (TextView)viewToUse.findViewById(R.id.TitleTextView);
            //holder.idText = (TextView)viewToUse.findViewById(R.id.IDTextView);
            viewToUse.setTag(holder);
        }
        else { viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        holder.titleText.setText(item.getText());
        //holder.idText.setText(""+item.getId());

        return viewToUse;
    }
}

