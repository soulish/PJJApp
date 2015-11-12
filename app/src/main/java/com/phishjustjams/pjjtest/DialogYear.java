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

public class DialogYear extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener   {
    public Activity c;
    public static Button yes, no;
    private static ArrayList<String> yearList;
    private static ArrayList<Integer> chosenYears = new ArrayList<>();
    private static String msg ="";
    private static DialogAdapter yearAdt;
    private static ListView yearView;

    public DialogYear(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.options_q);
        yearList = new ArrayList<>();
        yearList = MainActivity.getYearList();
        yearView = (ListView) findViewById(R.id.options_q_list);
        TextView title = (TextView) findViewById(R.id.options_q_text);
        title.setText("Select year preference");
        chosenYears = MainActivity.getMusicService().getChosenYears();
        ArrayList<Integer> chosenYearsIds = new ArrayList<>();
        if (chosenYears.size() == yearList.size()) {
            //if all of the years are chosen, then we don't want start with none chosen
            chosenYears = new ArrayList<>();
        }
        else{
            for (int i = 0; i < yearList.size(); i++){
                if (chosenYears.contains(Integer.parseInt(yearList.get(i)))) {
                    chosenYearsIds.add(i);
                }
            }
        }
        yearAdt = new DialogAdapter(this.getContext(),yearList,chosenYearsIds);
        yearView.setAdapter(yearAdt);
        yearView.setOnItemClickListener(this);
        yes = (Button) findViewById(R.id.options_btn_ok);
        no = (Button) findViewById(R.id.options_btn_cancel);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.options_btn_ok:
                if (chosenYears.size() == 0){
                    for (int i = 0; i < yearList.size(); i++){
                        chosenYears.add(Integer.parseInt(yearList.get(i)));
                    }
                }
                ArrayList<Integer> oldChosenYears = MainActivity.getMusicService().getChosenYears();
                MainActivity.getMusicService().setChosenYears(chosenYears);
                Boolean result = MainActivity.updateSongList(view, true);

                if (result){
                    msg = "";
                    ArrayList<Integer> origOrder = chosenYears;
                    Collections.sort(chosenYears);
                    for (int i = 0; i < chosenYears.size(); i++) {
                        msg=msg+"\n"+(i+1)+" : "+ chosenYears.get(i);
                    }
                    Toast.makeText(this.getContext(),
                            "Total " + chosenYears.size() + " Years Selected.\n" + msg, Toast.LENGTH_LONG)
                            .show();

                    TextView yearsChosenText_a = (TextView) c.findViewById(R.id.years_chosen);
                    String yearsChosenString_a = "";
                    for (int i=0; i < chosenYears.size(); i++)
                        yearsChosenString_a += chosenYears.get(i).toString() + ", ";
                    yearsChosenText_a.setText(yearsChosenString_a.substring(0, yearsChosenString_a.length() - 2));
                    chosenYears = origOrder;
                    MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                            .setCategory("Filters")
                            .setAction("Year")
                            .build());
                }
                else{
                    MainActivity.getMusicService().setChosenYears(oldChosenYears);
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
        if (chosenYears.contains(Integer.parseInt(yearList.get(position)))) {
            chosenYears.remove(chosenYears.indexOf(Integer.parseInt(yearList.get(position))));
            yearAdt.removeChosenOption(position);
        }
        else {
            chosenYears.add(Integer.parseInt(yearList.get(position)));
            yearAdt.addChosenOption(position);
        }
    }
}
