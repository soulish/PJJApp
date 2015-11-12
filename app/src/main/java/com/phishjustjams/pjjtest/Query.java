package com.phishjustjams.pjjtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class Query extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Dealing with the year choices, setting the button and the text
        final Button btn = (Button) findViewById(R.id.btnQuery);
        TextView yearsChosenText = (TextView) findViewById(R.id.years_chosen);
        ArrayList tempYearList_a = MainActivity.getMusicService().getChosenYears();
        Collections.sort(tempYearList_a);
        String yearsChosenString = "";
        for (int i=0; i < tempYearList_a.size(); i++){
            yearsChosenString += tempYearList_a.get(i).toString() + ", ";
        }
        yearsChosenText.setText(yearsChosenString.substring(0, yearsChosenString.length() - 2));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                displayQueryYear();
            }
        });
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetYear();
                return true;
            }
        });

        //Dealing with the song choices, setting the button and the text
        final Button btnTour = (Button) findViewById(R.id.btnTourQuery);
        TextView toursChosenText = (TextView) findViewById(R.id.tours_chosen);
        ArrayList tempTourList_a = MainActivity.getMusicService().getChosenTours();
        Collections.sort(tempTourList_a);
        String toursChosenString = "";
        for (int i=0; i < tempTourList_a.size(); i++){
            toursChosenString += tempTourList_a.get(i) + ", ";
        }
        toursChosenText.setText(toursChosenString.substring(0, toursChosenString.length() - 2));
        btnTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                displayQueryTour();
            }
        });
        btnTour.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetTour();
                return true;
            }
        });

        //Dealing with the song choices, setting the button and the text
        final Button btnSong = (Button) findViewById(R.id.btnSongQuery);
        TextView songsChosenText = (TextView) findViewById(R.id.songs_chosen);
        ArrayList tempSongList_a = MainActivity.getMusicService().getChosenSongs();
        Collections.sort(tempSongList_a);
        String songsChosenString = "";
        if (tempSongList_a.size() == MainActivity.getSongNameList().size()){
            songsChosenString = "All Songs Chosen  ";
        }
        else {
            for (int i = 0; i < tempSongList_a.size(); i++) {
                songsChosenString += tempSongList_a.get(i) + ", ";
            }
        }
        songsChosenText.setText(songsChosenString.substring(0, songsChosenString.length() - 2));
        btnSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                displayQuerySong();
            }
        });
        btnSong.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetSong();
                return true;
            }
        });

        //Dealing with the song choices, setting the button and the text
        final Button btnDuration = (Button) findViewById(R.id.btnDurationQuery);
        TextView durationsChosenText = (TextView) findViewById(R.id.durations_chosen);
        ArrayList tempDurationList_a = MainActivity.getMusicService().getChosenDurations();
        ArrayList tempDurationList_b = MainActivity.getMusicService().getDurations();
        String durationsChosenString = "";
        for (int i=0; i < tempDurationList_a.size(); i++){
            durationsChosenString += tempDurationList_b.get((Integer) tempDurationList_a.get(i)) + ", ";
        }
        durationsChosenText.setText(durationsChosenString.substring(0, durationsChosenString.length() - 2));
        btnDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                displayQueryDuration();
            }
        });
        btnDuration.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetDuration();
                return true;
            }
        });
    }


    public void displayQueryYear() {
        DialogYear yd = new DialogYear(this);
        yd.show();
    }

    public void displayQueryTour() {
        DialogTour td = new DialogTour(this);
        td.show();
    }

    public void displayQuerySong() {
        DialogSong sd = new DialogSong(this);
        sd.show();
    }

    public void displayQueryDuration() {
        DialogDuration dd = new DialogDuration(this);
        dd.show();
    }

    public void resetYear(){
        ArrayList<String> yearList;
        ArrayList<Integer> chosenYears = new ArrayList<>();
        String msg ="";
        yearList = MainActivity.getYearList();
        if (chosenYears.size() == 0){
            for (int i = 0; i < yearList.size(); i++){
                chosenYears.add(Integer.parseInt(yearList.get(i)));
            }
        }

        Collections.sort(chosenYears);
        for (int i = 0; i < chosenYears.size(); i++) {
            msg=msg+"\n"+(i+1)+" : "+ chosenYears.get(i);
        }
        Toast.makeText(this,
                "Total " + chosenYears.size() + " Years Selected.\n" + msg, Toast.LENGTH_LONG)
                .show();
        MainActivity.getMusicService().setChosenYears(chosenYears);

        TextView yearsChosenText_a = (TextView) this.findViewById(R.id.years_chosen);
        String yearsChosenString_a = "";
        for (int i=0; i < chosenYears.size(); i++)
            yearsChosenString_a += chosenYears.get(i).toString() + ", ";
        yearsChosenText_a.setText(yearsChosenString_a.substring(0, yearsChosenString_a.length() - 2));
    }

    public void resetTour(){
        ArrayList<String> tourList;
        ArrayList<String> chosenTours = new ArrayList<>();
        String msg = "";
        tourList = MainActivity.getTourList();
        for (int i = 0; i < tourList.size(); i++){
            chosenTours.add(tourList.get(i));
        }

        Collections.sort(chosenTours);
        for (int i = 0; i < chosenTours.size(); i++) {
            msg=msg+"\n"+(i+1)+" : "+ chosenTours.get(i);
        }
        Toast.makeText(this,
                "Total " + chosenTours.size() + " Tours Selected.\n" + msg, Toast.LENGTH_LONG)
                .show();
        MainActivity.getMusicService().setChosenTours(chosenTours);

        TextView toursChosenText_a = (TextView) this.findViewById(R.id.tours_chosen);
        String toursChosenString_a = "";
        for (int i=0; i < chosenTours.size(); i++)
            toursChosenString_a += chosenTours.get(i) + ", ";
        toursChosenText_a.setText(toursChosenString_a.substring(0, toursChosenString_a.length() - 2));
    }

    public void resetSong(){
        ArrayList<String> songList;
        ArrayList<String> chosenSongs = new ArrayList<>();
        String msg ="";
        songList = MainActivity.getSongNameList();
        for (int i = 0; i < songList.size(); i++){
            chosenSongs.add(songList.get(i));
        }

        Collections.sort(chosenSongs);

        TextView songsChosenText = (TextView) this.findViewById(R.id.songs_chosen);
        String songsChosenString = "";
        if (chosenSongs.size() == MainActivity.getSongNameList().size()){
            songsChosenString = "All Songs Chosen  ";
            msg = "All Songs Chosen";
        }
        else {
            for (int i = 0; i < chosenSongs.size(); i++){
                songsChosenString += chosenSongs.get(i) + ", ";
                msg=msg+"\n"+(i+1)+" : "+ chosenSongs.get(i);
            }
        }
        MainActivity.getMusicService().setChosenSongs(chosenSongs);
        Toast.makeText(this,
                "Total " + chosenSongs.size() + " Songs Selected.\n" + msg, Toast.LENGTH_LONG)
                .show();
        songsChosenText.setText(songsChosenString.substring(0, songsChosenString.length() - 2));
    }

    public void resetDuration(){
        ArrayList<String> durList = new ArrayList<>();
        durList.add("0-5");durList.add("5-10");durList.add("10-15");
        durList.add("15-20");durList.add("20+");
        ArrayList<Integer> chosenDurations = new ArrayList<>();
        String msg = "";
        for (int i = 0; i < durList.size(); i++){
            chosenDurations.add(i);
        }
        for (int i = 0; i < chosenDurations.size(); i++) {
            msg=msg+"\n"+(i+1)+" : "+ durList.get(chosenDurations.get(i));
        }
        Toast.makeText(this,
                "Total " + chosenDurations.size() + " Durations Selected.\n" + msg, Toast.LENGTH_LONG)
                .show();
        MainActivity.getMusicService().setChosenDurations(chosenDurations);

        TextView durationsChosenText = (TextView) this.findViewById(R.id.durations_chosen);
        ArrayList<String> tempDurationList_b = MainActivity.getMusicService().getDurations();
        String durationsChosenString = "";
        for (int i=0; i < chosenDurations.size(); i++){
            durationsChosenString += tempDurationList_b.get(chosenDurations.get(i)) + ", ";
        }
        durationsChosenText.setText(durationsChosenString.substring(0, durationsChosenString.length() - 2));
    }
}

