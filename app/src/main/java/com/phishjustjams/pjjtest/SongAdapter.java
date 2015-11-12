package com.phishjustjams.pjjtest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.Collections;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private Context context;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() { return songs.size(); }

    @Override
    public Object getItem(int position) { return null; }

    @Override
    public long getItemId(int position) { return 0; }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        RelativeLayout songLay = (RelativeLayout)songInf.inflate(R.layout.song, parent, false);
        TextView titleView = (TextView)songLay.findViewById(R.id.song_title);
        TextView venueView = (TextView)songLay.findViewById(R.id.song_venue);
        TextView durationView = (TextView)songLay.findViewById(R.id.song_duration);
        final Song currSong = songs.get(position);
        String extras = currSong.getExtras();
        if (extras.equals("")) {
            titleView.setText(currSong.getTitle() + " - " + currSong.getDate() + "  " +
                    currSong.getCity() + ", " + currSong.getState());
        }
        else if (extras.contains(">")){
            titleView.setText(currSong.getTitle() + " " + currSong.getExtras() + " - " + currSong.getDate() + "  " +
                    currSong.getCity() + ", " + currSong.getState());
        }
        else{
            titleView.setText(currSong.getTitle() + " (" + currSong.getExtras() + ") - " + currSong.getDate() + "  " +
                    currSong.getCity() + ", " + currSong.getState());
        }
        venueView.setText(currSong.getVenue());
        String dur = currSong.getDuration();
        durationView.setText(dur);
        songLay.setTag(position);
        songLay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopup(v, currSong);
                return true;//returning true means that onClick does not also get called
            }
        });
        return songLay;
    }

    public void showPopup(final View v, final Song currSong){
        PopupMenu popup = new PopupMenu(context, v);
        PopupMenu.OnMenuItemClickListener poplistener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_set_year:
                        setYearFromSong(currSong);
                        MainActivity.updateSongList(v, true);
                        MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Set Year from Song")
                                .setLabel(currSong.getYear())
                                .build());
                        return true;
                    case R.id.action_set_song:
                        setSongFromSong(currSong);
                        MainActivity.updateSongList(v, true);
                        MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Set Song from Song")
                                .setLabel(currSong.getTitle())
                                .build());
                        return true;
                    case R.id.action_add_year:
                        addYearFromSong(currSong);
                        MainActivity.updateSongList(v, true);
                        MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Add Year from Song")
                                .setLabel(currSong.getYear())
                                .build());
                        return true;
                    case R.id.action_add_song:
                        addSongFromSong(currSong);
                        MainActivity.updateSongList(v, true);
                        MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Add Song from Song")
                                .setLabel(currSong.getTitle())
                                .build());
                        return true;
                    case R.id.action_set_next_song:
                        setNextSong(currSong);
                        MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("Set Next Song")
                                .setLabel(currSong.getTitle() + " - " + currSong.getDate())
                                .build());
                        return true;
                    case R.id.action_view_setlist:
                        viewSetlist(currSong);
                        MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                                .setCategory("UX")
                                .setAction("View Setlist")
                                .build());
                        return true;
                    default:
                        return false;
                }
            }
        };
        popup.setOnMenuItemClickListener(poplistener);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.actions, popup.getMenu());
        popup.show();
    }

    public void setNextSong(Song thisSong){
        MainActivity.getMusicService().setNextSong(thisSong);
    }

    public void viewSetlist(Song thisSong){
        Intent intent;
        String url;
        if (thisSong.getArtist().contains("Phish")) {
            url = "http://phish.net/setlists/?d=" + thisSong.getDate().split(" ")[0];
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        }
        else if (thisSong.getArtist().contains("Grateful")){
            Toast.makeText(context, "Not available for the Grateful Dead", Toast.LENGTH_LONG).show();
        }
    }

    public void setYearFromSong(Song thisSong){
        ArrayList chosenYears = new ArrayList();
        chosenYears.add(Integer.parseInt(thisSong.getYear()));
        MainActivity.getMusicService().setChosenYears(chosenYears);
        String msg="";
        Collections.sort(chosenYears);
        for (int i = 0; i < chosenYears.size(); i++) {
            msg=msg+"\n"+(i+1)+" : "+ chosenYears.get(i);
        }
        Toast.makeText(context, "Total " + chosenYears.size() + " Years Selected.\n" + msg, Toast.LENGTH_LONG).show();
    }

    public void addYearFromSong(Song thisSong){
        ArrayList<Integer> chosenYears = MainActivity.getMusicService().getChosenYears();
        if (!chosenYears.contains(Integer.parseInt(thisSong.getYear()))) {
            chosenYears.add(Integer.parseInt(thisSong.getYear()));
            MainActivity.getMusicService().setChosenYears(chosenYears);
        }
        String msg="";
        Collections.sort(chosenYears);
        for (int i = 0; i < chosenYears.size(); i++) {
            msg=msg+"\n"+(i+1)+" : "+ chosenYears.get(i);
        }
        Toast.makeText(context, "Total " + chosenYears.size() + " Years Selected.\n" + msg, Toast.LENGTH_LONG).show();
    }

    public void setSongFromSong(Song thisSong){
        ArrayList<String> chosenSongs = new ArrayList<>();
        String playSongName = thisSong.getTitle();
        chosenSongs.add(playSongName);
        MainActivity.getMusicService().setChosenSongs(chosenSongs);
        String msg="";
        Collections.sort(chosenSongs);
        for (int i = 0; i < chosenSongs.size(); i++) {
            msg=msg+"\n"+(i+1)+" : "+ chosenSongs.get(i);
        }
        Toast.makeText(context, "Total " + chosenSongs.size() + " Songs Selected.\n" + msg, Toast.LENGTH_LONG).show();
    }

    public void addSongFromSong(Song thisSong){
        ArrayList<String> chosenSongs = MainActivity.getMusicService().getChosenSongs();
        String playSongName = thisSong.getTitle();
        if (!chosenSongs.contains(playSongName)){
            chosenSongs.add(playSongName);
            MainActivity.getMusicService().setChosenSongs(chosenSongs);
        }
        String msg="";
        Collections.sort(chosenSongs);
        for (int i = 0; i < chosenSongs.size(); i++) {
            msg=msg+"\n"+(i+1)+" : "+ chosenSongs.get(i);
        }
        Toast.makeText(context, "Total " + chosenSongs.size() + " Songs Selected.\n" + msg, Toast.LENGTH_LONG).show();
    }
}
