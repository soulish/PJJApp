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

public class DialogSong  extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener  {
    public Activity c;
    public static Button yes, no;
    private static ArrayList<String> songList;
    private static ArrayList<String> chosenSongs = new ArrayList<>();
    private static String msg ="";
    private static DialogAdapter songAdt;
    private static ListView songView;

    public DialogSong(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.options_q);
        songList = new ArrayList<>();
        songList = MainActivity.getSongNameList();
        songView = (ListView) findViewById(R.id.options_q_list);
        TextView title = (TextView) findViewById(R.id.options_q_text);
        title.setText("Select song preference");
        chosenSongs = MainActivity.getMusicService().getChosenSongs();
        ArrayList<Integer> chosenSongsIds = new ArrayList<>();
        if (chosenSongs.size() == songList.size()) {
            //if all of the songs are chosen, then we don't want start with none chosen
            chosenSongs = new ArrayList<>();
        }
        else{
            for (int i = 0; i < songList.size(); i++){
                if (chosenSongs.contains(songList.get(i))) {
                    chosenSongsIds.add(i);
                }
            }
        }
        songAdt = new DialogAdapter(this.getContext(),songList,chosenSongsIds);
        songView.setAdapter(songAdt);
        songView.setOnItemClickListener(this);
        yes = (Button) findViewById(R.id.options_btn_ok);
        no = (Button) findViewById(R.id.options_btn_cancel);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.options_btn_ok:
                if (chosenSongs.size() == 0){
                    for (int i = 0; i < songList.size(); i++){
                        chosenSongs.add(songList.get(i));
                    }
                }
                ArrayList<String> oldChosenSongs = MainActivity.getMusicService().getChosenSongs();
                MainActivity.getMusicService().setChosenSongs(chosenSongs);
                Boolean result = MainActivity.updateSongList(view, true);
                if (result) {
                    msg = "";
                    ArrayList<String> origOrder = chosenSongs;
                    Collections.sort(chosenSongs);

                    TextView songsChosenText = (TextView) c.findViewById(R.id.songs_chosen);
                    String songsChosenString = "";
                    if (chosenSongs.size() == MainActivity.getSongNameList().size()) {
                        songsChosenString = "All Songs Chosen  ";
                        msg = "All Songs Chosen";
                    } else {
                        for (int i = 0; i < chosenSongs.size(); i++) {
                            songsChosenString += chosenSongs.get(i) + ", ";
                            msg = msg + "\n" + (i + 1) + " : " + chosenSongs.get(i);
                        }
                    }
                    Toast.makeText(this.getContext(),
                            "Total " + chosenSongs.size() + " Songs Selected.\n" + msg, Toast.LENGTH_LONG)
                            .show();
                    songsChosenText.setText(songsChosenString.substring(0, songsChosenString.length() - 2));
                    chosenSongs = origOrder;
                    MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                            .setCategory("Filters")
                            .setAction("Song")
                            .build());
                }
                else{
                    MainActivity.getMusicService().setChosenSongs(oldChosenSongs);
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
        if (chosenSongs.contains(songList.get(position))) {
            chosenSongs.remove(songList.get(position));
            songAdt.removeChosenOption(position);
        }
        else {
            chosenSongs.add(songList.get(position));
            songAdt.addChosenOption(position);
        }
    }
}
