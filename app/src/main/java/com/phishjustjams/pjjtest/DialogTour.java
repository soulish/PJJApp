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

public class DialogTour extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener   {
    public Activity c;
    public static Button yes, no;
    private static ArrayList<String> tourList;
    private static ArrayList<String> chosenTours = new ArrayList<>();
    private static String msg = "";
    private static DialogAdapter tourAdt;
    private static ListView tourView;

    public DialogTour(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.options_q);
        tourList = new ArrayList<>();
        tourList = MainActivity.getTourList();
        tourView = (ListView) findViewById(R.id.options_q_list);
        TextView title = (TextView) findViewById(R.id.options_q_text);
        title.setText("Select tour preference");
        chosenTours = MainActivity.getMusicService().getChosenTours();
        ArrayList<Integer> chosenToursIds = new ArrayList<>();
        if (chosenTours.size() == tourList.size()) {
            //if all of the tours are chosen, then we don't want start with none chosen
            chosenTours = new ArrayList<>();
        }
        else{
            for (int i = 0; i < tourList.size(); i++){
                if (chosenTours.contains(tourList.get(i))) {
                    chosenToursIds.add(i);
                }
            }
        }
        tourAdt = new DialogAdapter(this.getContext(),tourList,chosenToursIds);
        tourView.setAdapter(tourAdt);
        tourView.setOnItemClickListener(this);
        yes = (Button) findViewById(R.id.options_btn_ok);
        no = (Button) findViewById(R.id.options_btn_cancel);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.options_btn_ok:
                if (chosenTours.size() == 0){
                    for (int i = 0; i < tourList.size(); i++){
                        chosenTours.add(tourList.get(i));
                    }
                }
                ArrayList<String> oldChosenTours = MainActivity.getMusicService().getChosenTours();
                MainActivity.getMusicService().setChosenTours(chosenTours);
                Boolean result = MainActivity.updateSongList(view, true);

                if (result) {
                    msg = "";
                    ArrayList<String> origOrder = chosenTours;
                    Collections.sort(chosenTours);
                    for (int i = 0; i < chosenTours.size(); i++) {
                        msg = msg + "\n" + (i + 1) + " : " + chosenTours.get(i);
                    }
                    Toast.makeText(this.getContext(),
                            "Total " + chosenTours.size() + " Tours Selected.\n" + msg, Toast.LENGTH_LONG)
                            .show();

                    TextView toursChosenText_a = (TextView) c.findViewById(R.id.tours_chosen);
                    String toursChosenString_a = "";
                    for (int i = 0; i < chosenTours.size(); i++)
                        toursChosenString_a += chosenTours.get(i) + ", ";
                    toursChosenText_a.setText(toursChosenString_a.substring(0, toursChosenString_a.length() - 2));
                    chosenTours = origOrder;
                    MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                            .setCategory("Filters")
                            .setAction("Tour")
                            .build());
                } else {
                    MainActivity.getMusicService().setChosenTours(oldChosenTours);
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
        if (chosenTours.contains(tourList.get(position))) {
            chosenTours.remove(tourList.get(position));
            tourAdt.removeChosenOption(position);
        }
        else {
            chosenTours.add(tourList.get(position));
            tourAdt.addChosenOption(position);
        }
    }
}
