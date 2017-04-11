package com.solunes.endeappbeni.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


import com.solunes.endeappbeni.R;
import com.solunes.endeappbeni.utils.SingleChoiceItem;

import java.util.ArrayList;

/**
 * Created by jhonlimaster on 19-03-17.
 */

public class SingleChoiceAdapter extends ArrayAdapter<SingleChoiceItem> {

    private Context context;
    private ArrayList<SingleChoiceItem> singleChoiceItems;
    private boolean isImped;

    public SingleChoiceAdapter(Context context, ArrayList<SingleChoiceItem> objects, boolean isImped) {
        super(context, 0, objects);
        this.context = context;
        singleChoiceItems = objects;
        this.isImped = isImped;
    }

    @Override
    public int getCount() {
        return singleChoiceItems.size();
    }

    @Override
    public SingleChoiceItem getItem(int i) {
        return singleChoiceItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return singleChoiceItems.indexOf(singleChoiceItems.get(i));
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_single_choice, parent, false);
        }

        SingleChoiceItem singleChoiceItem = singleChoiceItems.get(position);
        TextView textViewCode = (TextView) convertView.findViewById(R.id.item_code);
        TextView textViewTitle = (TextView) convertView.findViewById(R.id.item_title);
        textViewCode.setText(String.valueOf(singleChoiceItem.getCode()));
        textViewTitle.setText(singleChoiceItem.getTitle());
        if (isImped) {
            textViewCode.setTextColor(Color.parseColor("#ff3d00"));
            textViewTitle.setTextColor(Color.parseColor("#ff3d00"));
        }

        return convertView;
    }
}
