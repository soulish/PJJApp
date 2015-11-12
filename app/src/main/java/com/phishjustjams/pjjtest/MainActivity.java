package com.phishjustjams.pjjtest;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {
    private static ArrayList<Song> songList;
    private static ArrayList<String> yearList;
    private static ArrayList<String> songNameList;
    private static ArrayList<String> durationList;
    private static ArrayList<String> tourList;
    private static ArrayList<Integer> intentYearList;
    private static ArrayList<String> intentTourList;
    private static ArrayList<String> intentSongList;
    private static ArrayList<Integer> intentDurationList;
    private static ListView songView;
    private static TextView songNPBanner;
    private static TextView songBannerTitle;
    private static TextView songBannerVenue;
    private static TextView songBannerDuration;
    private static TextView songBannerListStatus;
    private static MusicService musicSrv;
    private static Intent playIntent;
    private boolean musicBound = false;
    private static MediaController controller;
    private boolean paused = false;
    private static boolean playbackPaused = false;
    private boolean needToChangeMenuItem = false;
    private static Integer total_duration = 0;
    private static Integer total_num_tracks = 0;
    private static Drawable drawableId;
    private static boolean fullList = true;
    private static HeadphoneReceiver headphoneReceiver;
    private static IntentFilter headphoneFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    private static ConnectivityManager cm;
    private static RelativeLayout songBannerLay;
    private static Random rand = new Random();
    private static IncomingCall receiver;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    private String queryString = null;
    private static String startSong = null;
    private static HashMap<String, Integer> urlHash;
    private static Boolean setShuffleOffIntent = false;

    //this checks if there is data in the query string and then sets variable accordingly.
    public void checkQuery(String source) {
        intentYearList = new ArrayList<>();
        intentTourList = new ArrayList<>();
        intentSongList = new ArrayList<>();
        intentDurationList = new ArrayList<>();
        if (getIntent().getData() != null) {
            Uri data = getIntent().getData();
            String query = data.getQuery();
            if (!query.equals(queryString)) {
                queryString = query;
                String[] queries = query.split("&");
                for (String query1 : queries) {
                    String action = query1.split("=")[0];
                    String choice = query1.split("=")[1];
                    String[] choices = choice.split(",");
                    switch (action) {
                        case "play":
                            if (choice.length() == 17) {
                                char[] band = new char[2];
                                char[] yr = new char[2];
                                choice.getChars(0, 2, band, 0);
                                choice.getChars(7, 9, yr, 0);
                                int yrr = Integer.valueOf(String.valueOf(yr));
                                String tmp = "";
                                if (String.valueOf(band).equals("ph")){
                                    tmp = "mp3s/Phish/";
                                    if (yrr > 50)
                                        tmp += "19"+yrr;
                                    else if (yrr < 10)
                                        tmp += "200"+yrr;
                                    else
                                        tmp += "20"+yrr;
                                    tmp += "/"+choice;
                                }
                                startSong = tmp;
                            }
                            break;
                        case "years":
                            for (String choice1 : choices)
                                intentYearList.add(Integer.parseInt(choice1));
                            break;
                        case "tours":
                            Collections.addAll(intentTourList, choices);
                            break;
                        case "songs":
                            Collections.addAll(intentSongList, choices);
                            break;
                        case "durations":
                            for (String choice1 : choices){
                                switch (choice1){
                                    case "0-5_mins":
                                        intentDurationList.add(0);
                                    case "5-10_mins":
                                        intentDurationList.add(1);
                                    case "10-15_mins":
                                        intentDurationList.add(2);
                                    case "15-20_mins":
                                        intentDurationList.add(3);
                                    case "20+_mins":
                                        intentDurationList.add(4);
                                }
                            }
                            break;
                        case "locations":
                            //do nothing
                            break;
                        case "random":
                            if (choice.equals("false")) {
                                setShuffleOffIntent = true;
                            }
                            break;
                    }
                }
                if (source.equals("onResume")){
                    if (paused || isPlaying()){
                        if (setShuffleOffIntent) {
                            musicSrv.setShuffle(false);
                            drawableId = ContextCompat.getDrawable(this, R.drawable.play);
                            needToChangeMenuItem = true;
                            invalidateOptionsMenu();
                            setShuffleOffIntent = false;
                        }
                        else{
                            if (!musicSrv.getShuffle()){
                                musicSrv.setShuffle(true);
                                drawableId = ContextCompat.getDrawable(this, R.drawable.rand);
                                needToChangeMenuItem = true;
                                invalidateOptionsMenu();
                            }
                        }

                        if (intentYearList.size() == 0) {//if no years were set, choose all of them
                            ArrayList<String> stringYearList = getYearList();
                            for (int i = 0; i < stringYearList.size(); i++) {
                                intentYearList.add(Integer.parseInt(stringYearList.get(i)));
                            }
                        }
                        ArrayList<String> thisTourList = intentTourList.size() == 0 ? getTourList() : intentTourList;
                        ArrayList<String> thisSongList = intentSongList.size() == 0 ? getSongNameList() : intentSongList;
                        if (intentDurationList.size() == 0) {
                            for (int i = 0; i < 5; i++) {
                                intentDurationList.add(i);
                            }
                        }
                        musicSrv.setChosenYears(intentYearList);
                        musicSrv.setChosenTours(thisTourList);
                        musicSrv.setChosenSongs(thisSongList);
                        musicSrv.setChosenDurations(intentDurationList);

                        updateSongList(findViewById(R.id.song_list), true);

                        if (startSong != null) {
                            Integer songIndex = urlHash.get(startSong);
                            if (songIndex != null && songIndex < urlHash.size())
                                startFromIntent(songIndex);
                            startSong = null;
                        }
                        intentYearList = null;
                        intentTourList = null;
                        intentSongList = null;
                        intentDurationList = null;
                    }
                }
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkQuery("onCreate");

        setUpLists();
        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected)
            new FetchJSONfromServer().execute(this);
        else
            updateStuff(this);

        setController();

        SongAdapter songAdt = new SongAdapter(this,songList);
        songView.setAdapter(songAdt);

        setUpReceivers();

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-XXXXXXXX-X");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(false);
        tracker.enableAutoActivityTracking(true);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart(){
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void startFromIntent(Integer songPos){
        musicSrv.setPrevSong();
        musicSrv.setSong(songPos);
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        Song currentSong = musicSrv.getCurrSong();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("Started from Intent")
                .setLabel(currentSong.getTitle() + " - " + currentSong.getDate())
                .build());
    }

    public void songPicked(View view){
        musicSrv.setPrevSong();
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        Song currentSong = musicSrv.getCurrSong();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("Picked Song")
                .setLabel(currentSong.getTitle() + " - " + currentSong.getDate())
                .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (needToChangeMenuItem){

            menu.findItem(R.id.action_play).setIcon(drawableId);
        }

        needToChangeMenuItem = false;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filters:
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("UX")
                        .setAction("Opened Filters")
                        .build());
                Intent queryIntent = new Intent(this, Query.class);
                startActivity(queryIntent);
                break;
            case R.id.action_play:
                musicSrv.setShuffle();
                boolean tf = musicSrv.getShuffle();
                if (tf) {
                    drawableId = ContextCompat.getDrawable(this, R.drawable.rand);
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("UX")
                            .setAction("Turned Random On")
                            .build());
                }
                else {
                    drawableId = ContextCompat.getDrawable(this, R.drawable.play);
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("UX")
                            .setAction("Turned Random Off")
                            .build());
                }
                needToChangeMenuItem = true;
                invalidateOptionsMenu();
                break;
            case R.id.action_end:
                new AlertDialog.Builder(this)
                        .setIcon(ContextCompat.getDrawable(this, R.drawable.pjj_icon_small))
                        .setTitle("Exit Application")
                        .setMessage("Do you wish to refresh the song list, or close the application?")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isPlaying()) {
                                    musicSrv.cancelNotification();
                                }
                                musicSrv = null;
                                stopService(playIntent);
                                System.exit(0);
                            }
                        })
                        .setNegativeButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new UpdateJSONfromServer().execute();
                            }
                        })
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUpReceivers(){
        if (receiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PHONE_STATE");
            receiver = new IncomingCall();
            receiver.setMediaPlayer(MainActivity.this);
            registerReceiver(receiver, filter);
        }
        if (headphoneReceiver == null) {
            headphoneReceiver = new HeadphoneReceiver();
            headphoneReceiver.setMediaPlayer(MainActivity.this);
            registerReceiver(headphoneReceiver, headphoneFilter);
        }
    }

    public void setUpLists(){
        songView = (ListView)findViewById(R.id.song_list);
        songNPBanner = (TextView)findViewById(R.id.song_banner_main);
        songBannerTitle = (TextView)findViewById(R.id.song_banner_title);
        songBannerVenue = (TextView)findViewById(R.id.song_banner_venue);
        songBannerListStatus = (TextView)findViewById(R.id.song_banner_list_status);
        songBannerDuration = (TextView)findViewById(R.id.song_banner_duration);
        songBannerTitle.setText("Nothing Playing Yet");
        songBannerVenue.setText("");
        songBannerDuration.setText("XX:XX");

        songList = new ArrayList<>();
        yearList = new ArrayList<>();
        songNameList = new ArrayList<>();
        songNameList.add("2001");songNameList.add("46 Days");songNameList.add("AC/DC Bag");
        songNameList.add("Antelope");songNameList.add("Bathtub Gin");songNameList.add("Birds");
        songNameList.add("Boogie On");songNameList.add("Bowie");songNameList.add("Carini");
        songNameList.add("Caspian");songNameList.add("Chalkdust");songNameList.add("Cities");
        songNameList.add("Crosseyed");songNameList.add("Disease");songNameList.add("Drowned");
        songNameList.add("Fee");songNameList.add("Free");songNameList.add("Ghost");
        songNameList.add("Golden Age");songNameList.add("Gumbo");songNameList.add("Halley\'s");
        songNameList.add("Hood");songNameList.add("It\'s Ice");songNameList.add("Jam");
        songNameList.add("Kill Devil Falls");songNameList.add("Light");songNameList.add("Maze");
        songNameList.add("Mike\'s");songNameList.add("Number Line");songNameList.add("Piper");
        songNameList.add("Possum");songNameList.add("Reba");songNameList.add("Rock n\' Roll");
        songNameList.add("Roggae");songNameList.add("Runaway Jim");songNameList.add("Sand");
        songNameList.add("Scents");songNameList.add("Seven Below");songNameList.add("Simple");
        songNameList.add("Slave");songNameList.add("Sneakin Sally");songNameList.add("Split");
        songNameList.add("Stash");songNameList.add("Theme");songNameList.add("Timber");
        songNameList.add("Tube");songNameList.add("Tweezer");songNameList.add("Twist");
        songNameList.add("Waves");songNameList.add("Weekapaug");songNameList.add("Wolfman\'s");
        songNameList.add("Ya Mar");songNameList.add("YEM");

        durationList = new ArrayList<>();
        durationList.add("0-5");durationList.add("5-10");durationList.add("10-15");
        durationList.add("15-20");durationList.add("20+");

        tourList = new ArrayList<>();
        tourList.add("Winter");
        tourList.add("Spring");
        tourList.add("Summer");tourList.add("Fall");
        tourList.add("Holiday");

        songBannerLay = (RelativeLayout) this.findViewById(R.id.song_banner);
        songBannerLay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetSongList(v);
                return true;//returning true means that onClick does not also get called
            }
        });
    }

    private class FetchJSONfromServer extends AsyncTask<MainActivity, Void, MainActivity>{

        @Override
        protected MainActivity doInBackground(MainActivity... params) {
            URL url = null;
            try {
                url = new URL("http://www.phishjustjams.com/files/sorted_tracks_min.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                try {
                    StringBuilder sb = new StringBuilder();
                    InputStream file = new BufferedInputStream(conn.getInputStream());
                    BufferedReader rd = new BufferedReader(new InputStreamReader(file));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    file.close();
                    String test = new String(sb);
                    MainActivity.getSongListFromServerJSON(test);
                } catch (IOException a) {
                    a.printStackTrace();
                } finally {
                    conn.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return params[0];
        }

        protected void onPostExecute(MainActivity temp) {
            MainActivity.updateStuff(temp);
        }
    }

    private class UpdateJSONfromServer extends AsyncTask<Void, Void, Integer>{

        @Override
        protected Integer doInBackground(Void... params) {
            URL url = null;
            try {
                url = new URL("http://www.phishjustjams.com/files/sorted_tracks_min.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                try {
                    StringBuilder sb = new StringBuilder();
                    InputStream file = new BufferedInputStream(conn.getInputStream());
                    BufferedReader rd = new BufferedReader(new InputStreamReader(file));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    file.close();
                    String test = new String(sb);
                    MainActivity.getSongListFromServerJSON(test);
                } catch (IOException a) {
                    a.printStackTrace();
                } finally {
                    conn.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 1;
        }

        protected void onPostExecute(Integer result) {
            MainActivity.updateSongList(findViewById(R.id.song_list),true);
        }

    }

    public static void updateStuff(MainActivity temp) {
        if (total_num_tracks == 0) {
            songBannerListStatus.setText("");
            setBanner("Not Connected to the Internet","Try again when connected","");
        }
        else {
            String total_duration_string = String.valueOf(total_duration / 3600000) + " hrs";
            String total_num_tracks_string = String.valueOf(total_num_tracks) + " tracks";
            songBannerListStatus.setText("Full: " + total_num_tracks_string);
            setBanner("Nothing Playing Yet", total_num_tracks_string, total_duration_string);

            Collections.sort(songList, new Comparator<Song>() {
                @Override
                public int compare(Song a, Song b) {
                    return a.getDate().compareTo(b.getDate());
                }
            });
            urlHash = new HashMap<>();
            for (int i=0; i < songList.size(); i++) {
                urlHash.put(songList.get(i).getUrl(),i);
            }

            if (songList.size() > 0)
                setSongViewPosition(rand.nextInt(songList.size()));

            musicSrv.setList(songList, yearList, songNameList, durationList, tourList);

            if (setShuffleOffIntent) {
                musicSrv.setShuffle(false);
                drawableId = ContextCompat.getDrawable(temp, R.drawable.play);
                temp.needToChangeMenuItem = true;
                temp.invalidateOptionsMenu();
                setShuffleOffIntent = false;
            }

            if (intentYearList.size() == 0) {//if no years were set, choose all of them
                ArrayList<String> stringYearList = getYearList();
                for (int i = 0; i < stringYearList.size(); i++) {
                    intentYearList.add(Integer.parseInt(stringYearList.get(i)));
                }
            }
            ArrayList<String> thisTourList = intentTourList.size() == 0 ? getTourList() : intentTourList;
            ArrayList<String> thisSongList = intentSongList.size() == 0 ? getSongNameList() : intentSongList;
            if (intentDurationList.size() == 0) {
                for (int i = 0; i < 5; i++) {
                    intentDurationList.add(i);
                }
            }
            musicSrv.setChosenYears(intentYearList);
            musicSrv.setChosenTours(thisTourList);
            musicSrv.setChosenSongs(thisSongList);
            musicSrv.setChosenDurations(intentDurationList);

            updateSongList(temp.findViewById(R.id.song_list), true);

            if (startSong != null){
                Integer songIndex = urlHash.get(startSong);
                if (songIndex != null && songIndex < urlHash.size())
                    temp.startFromIntent(songIndex);
                startSong = null;
            }

            intentYearList = null;
            intentTourList = null;
            intentSongList = null;
            intentDurationList = null;
        }
    }

    public static void getSongListFromServerJSON(String test) {
        JSONArray jArray;
        try {
            jArray = new JSONArray(test);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jsonObject = jArray.getJSONObject(i);
                String urlString = jsonObject.getString("url");
                if (!Song.hasUrl(urlString)) {
                    String titleString = jsonObject.getString("title");
                    String dateString = jsonObject.getString("date");
                    String weekdayString = jsonObject.getString("weekday");
                    String countryString = jsonObject.has("country") ? jsonObject.getString("country") : "USA";
                    String stateString = jsonObject.getString("state");
                    String cityString = jsonObject.getString("city");
                    String venueString = jsonObject.getString("venue");
                    String bandString = jsonObject.getString("band");
                    String yearString = jsonObject.getString("year");
                    String tourString = jsonObject.getString("tour");
                    String extrasString = jsonObject.has("extras") ? jsonObject.getString("extras") : "";
                    String durationString = jsonObject.getString("duration");
                    songList.add(new Song(titleString, bandString, yearString,
                            tourString, durationString, dateString,
                            weekdayString, countryString, stateString, cityString,
                            venueString, extrasString, urlString));
                    total_num_tracks++;
                    Integer thisDuration = Integer.parseInt(durationString.split(":")[0]) * 60000 +
                            Integer.parseInt(durationString.split(":")[0]) * 1000;
                    total_duration = total_duration + thisDuration;
                    if (!yearList.contains(yearString)) {
                        yearList.add(yearString);
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void resetSongList(View view) {
        ArrayList<String> stringYearList = getYearList();
        ArrayList<Integer> intYearList = new ArrayList<>();
        for (int i = 0; i < stringYearList.size(); i++) {
            intYearList.add(Integer.parseInt(stringYearList.get(i)));
        }
        musicSrv.setChosenYears(intYearList);
        musicSrv.setChosenTours(getTourList());
        musicSrv.setChosenSongs(getSongNameList());
        ArrayList<Integer> intDurList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            intDurList.add(i);
        }
        musicSrv.setChosenDurations(intDurList);
        updateSongList(view, true);
        Toast.makeText(view.getContext(), "All Filters Reset", Toast.LENGTH_LONG).show();
    }

    public static Boolean updateSongList(View view){
        return updateSongList(view, false);
    }

    public static Boolean updateSongList(View view, Boolean tf){
        ArrayList<Song> newSongList = new ArrayList<>();
        Integer currSongIndex = 0;
        Integer counter = 0;
        Song currSong = MusicService.getCurrSong();
        if (fullList || tf){ //i.e. we already are showing all of the available songs
            ArrayList chosenSongs = MainActivity.getMusicService().getChosenSongs();
            ArrayList chosenYears = MainActivity.getMusicService().getChosenYears();
            ArrayList chosenDurations = MainActivity.getMusicService().getChosenDurations();
            ArrayList chosenTours = MainActivity.getMusicService().getChosenTours();
            for (int i = 0; i < songList.size(); i++) {
                Song thisSong = songList.get(i);
                String playSongName = thisSong.getTitle();
                Integer durationRank;
                durationRank = thisSong.getDuration_minutes() / 5;
                if (durationRank >= 4)
                    durationRank = 4;
                if (chosenYears.contains(Integer.parseInt(thisSong.getYear())) &&
                        (chosenSongs.contains(playSongName) || chosenSongs.size() == MainActivity.getSongNameList().size()) &&
                        chosenDurations.contains(durationRank) &&
                        chosenTours.contains(thisSong.getTour())) {
                    newSongList.add(songList.get(i));
                    if (songList.get(i) == currSong){
                        currSongIndex = counter;
                    }
                    counter += 1;
                }
            }
            fullList = false;
        }
        else { //i.e. we are already showing the smaller list of songs
            for (int i = 0; i < songList.size(); i++) {
                newSongList.add(songList.get(i));
                if (songList.get(i) == currSong){
                    currSongIndex = i;
                }
                counter += 1;
            }
            fullList = true;
        }
        if (counter == 0){
            return false;
        }
        else {
            SongAdapter songAdt = new SongAdapter(view.getContext(), newSongList);
            songView.setAdapter(songAdt);//changes the songs displayed
            musicSrv.updateList(newSongList, currSongIndex);//change the songlist and set the current song index
            if (currSongIndex == 0) {
                currSongIndex = rand.nextInt(counter);
            }
            setSongViewPosition(currSongIndex);//ensures the songview goes to the current song or a random track if this one is no longer in the list
            String total_num_tracks_string = String.valueOf(counter) + " tracks";

            urlHash = new HashMap<>();
            for (int i=0; i < newSongList.size(); i++)
                urlHash.put(newSongList.get(i).getUrl(),i);

            if (fullList)
                songBannerListStatus.setText("Full: " + total_num_tracks_string);
            else
                songBannerListStatus.setText("Filtered: " + total_num_tracks_string);
            return true;
        }
    }

    public static ArrayList getYearList() {return yearList;}

    public static ArrayList getTourList() {return tourList;}

    public static ArrayList getSongNameList() {return songNameList;}

    public static MusicService getMusicService() {return musicSrv;}

    public static ConnectivityManager getConnectivityManager() {return cm;}

    public static Tracker getTracker(){return tracker;}

    public static void setNPBanner(String ban){
        songNPBanner.setText(ban);
    }

    public static void setBanner(String banTitle, String banVenue, String banDuration){
        songBannerTitle.setText(banTitle);
        songBannerVenue.setText(banVenue);
        songBannerDuration.setText(banDuration);
    }

    public static void setSongViewPosition(int pos) {
        songView.setSelection(pos);
    }

    @Override
    protected void onDestroy() {
        if (isPlaying()) {
            musicSrv.cancelNotification();
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        if (headphoneReceiver != null) {
            unregisterReceiver(headphoneReceiver);
            headphoneReceiver = null;
        }
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    private void setController() {
        if (controller == null) {
            controller = new MusicController(MainActivity.this);
            controller.setPrevNextListeners(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playNext();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playPrev();
                }
            });

            controller.setMediaPlayer(MainActivity.this);
            controller.setAnchorView(findViewById(R.id.song_list));
            controller.setEnabled(true);
            controller.setBackgroundColor(Color.parseColor("#000000"));
            controller.setFocusable(true);
        }
    }

    public static MediaController getMusicController(){return controller;}

    private void playNext(){
        musicSrv.playNext();
        if (playbackPaused){
            setController();
            playbackPaused = false;
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("Play Next")
                .build());
    }

    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("Play Prev")
                .build());
    }


    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        return musicSrv != null && musicBound && musicSrv.isPng();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused || isPlaying()) { //i.e. if everything has already been loaded
            updateSongList(this.findViewById(R.id.song_banner), true);
            setUpReceivers();
            checkQuery("onResume");
        }
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        if (!isPlaying()) {
            if (receiver != null) {
                unregisterReceiver(receiver);
                receiver = null;
            }
            if (headphoneReceiver != null) {
                unregisterReceiver(headphoneReceiver);
                headphoneReceiver = null;
            }
        }
        controller.hide();
        super.onStop();
    }
}
