package com.phishjustjams.pjjtest;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class DialogAdapter extends BaseAdapter {
    private static final int UNSELECTED = 0;
    private static final int SELECTED = 1;
    private static final int NUMBER_OF_LAYOUTS = 2;
    private static ArrayList<String> options;
    private static ArrayList<Integer> chosenOptions;
    private static LayoutInflater optInf;
    private static HashMap<String,LinearLayout> optLays = new HashMap<>();

    public DialogAdapter(Context c, ArrayList<String> theOptions, ArrayList<Integer> theChosen) {
        options = theOptions;
        chosenOptions = theChosen;
        optInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout optLay;
        optLay = (LinearLayout) optInf.inflate(R.layout.options, parent, false);
        TextView optView = (TextView) optLay.findViewById(R.id.options_text);
        optView.setText(options.get(position));
        optLay.setTag(position);
        int type = getItemViewType(position);
        if (type == UNSELECTED) {
            optView.setTextColor(Color.parseColor("#CCFFFF"));
        }
        else if (type == SELECTED) {
            optView.setTextColor(Color.parseColor("#0099CC"));
        }
        else{
            optView.setTextColor(Color.parseColor("#CCFFFF"));
        }
        optLays.put(options.get(position), optLay);
        return optLay;
    }

    public void addChosenOption(int position){
        chosenOptions.add(position);
        TextView optView = (TextView) optLays.get(options.get(position)).findViewById(R.id.options_text);
        optView.setTextColor(Color.parseColor("#0099CC"));
    }

    public void removeChosenOption(int position){
        chosenOptions.remove(chosenOptions.indexOf(position));
        TextView optView = (TextView) optLays.get(options.get(position)).findViewById(R.id.options_text);
        optView.setTextColor(Color.parseColor("#CCFFFF"));
    }

    @Override
    public int getItemViewType(int position) {
        if (chosenOptions.contains(position)){
            return SELECTED;
        }
        else{
            return UNSELECTED;
        }
    }
    @Override
    public int getViewTypeCount() {
        return NUMBER_OF_LAYOUTS;
    }
}

