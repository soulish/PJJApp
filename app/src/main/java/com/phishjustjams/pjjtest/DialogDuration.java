package com.phishjustjams.pjjtest;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.Collections;

public class DialogDuration extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener {
    public Activity c;
    public static Button yes, no;
    private static ArrayList<String> durList;
    private static ArrayList<Integer> chosenDurations = new ArrayList<>();
    private static String msg ="";
    private static DialogAdapter durAdt;
    private static ListView durView;

    public DialogDuration(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.options_q);
        durList = new ArrayList<>();
        durList.add("0-5");durList.add("5-10");durList.add("10-15");
        durList.add("15-20");durList.add("20+");
        durView = (ListView) findViewById(R.id.options_q_list);
        TextView title = (TextView) findViewById(R.id.options_q_text);
        title.setText("Select duration preference");
        chosenDurations = MainActivity.getMusicService().getChosenDurations();
        ArrayList<Integer> chosenDursIds = new ArrayList<>();
        if (chosenDurations.size() == durList.size()) {
            //if all of the durs are chosen, then we don't want start with none chosen
            chosenDurations = new ArrayList<>();
        }
        else{
            for (int i = 0; i < durList.size(); i++){
                if (chosenDurations.contains(i)) {
                    chosenDursIds.add(i);
                }
            }
        }
        durAdt = new DialogAdapter(this.getContext(),durList,chosenDursIds);
        durView.setAdapter(durAdt);
        durView.setOnItemClickListener(this);
        yes = (Button) findViewById(R.id.options_btn_ok);
        no = (Button) findViewById(R.id.options_btn_cancel);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.options_btn_ok:
                if (chosenDurations.size() == 0){
                    for (int i = 0; i < durList.size(); i++){
                        chosenDurations.add(i);
                    }
                }
                ArrayList<Integer> oldChosenDurations = MainActivity.getMusicService().getChosenDurations();
                MainActivity.getMusicService().setChosenDurations(chosenDurations);
                Boolean result = MainActivity.updateSongList(view,true);

                if (result) {
                    msg = "";
                    ArrayList<Integer> origOrder = chosenDurations;
                    Collections.sort(chosenDurations);
                    for (int i = 0; i < chosenDurations.size(); i++) {
                        msg = msg + "\n" + (i + 1) + " : " + durList.get(chosenDurations.get(i));
                    }
                    Toast.makeText(this.getContext(),
                            "Total " + chosenDurations.size() + " Durations Selected.\n" + msg, Toast.LENGTH_LONG)
                            .show();

                    TextView durationsChosenText = (TextView) c.findViewById(R.id.durations_chosen);
                    ArrayList<String> tempDurationList_b = MainActivity.getMusicService().getDurations();
                    String durationsChosenString = "";
                    for (int i = 0; i < chosenDurations.size(); i++) {
                        durationsChosenString += tempDurationList_b.get(chosenDurations.get(i)) + ", ";
                    }
                    durationsChosenText.setText(durationsChosenString.substring(0, durationsChosenString.length() - 2));
                    chosenDurations = origOrder;
                    MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                            .setCategory("Filters")
                            .setAction("Duration")
                            .build());
                }
                else{
                    MainActivity.getMusicService().setChosenDurations(oldChosenDurations);
                    MainActivity.updateSongList(view,true);

                    Toast.makeText(this.getContext(),
                            "No tracks meet this set of filters. Please try a different set of filters.", Toast.LENGTH_LONG)
                            .show();
                }
                //c.recreate();
                break;
            case R.id.options_btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        if (chosenDurations.contains(position)) {
            chosenDurations.remove(chosenDurations.indexOf(position));
            durAdt.removeChosenOption(position);
        }
        else {
            chosenDurations.add(position);
            durAdt.addChosenOption(position);
        }
    }
}
