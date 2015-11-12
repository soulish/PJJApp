package com.phishjustjams.pjjtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private static ArrayList<Song> songs;
    private ArrayList<String> durations;
    private static int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private String songDur="";
    private static final int NOTIFY_ID=1;
    private boolean shuffle = true;
    private Random rand;
    private ArrayList<Integer> chosenYears = new ArrayList<>();
    private ArrayList<String> chosenTours = new ArrayList<>();
    private ArrayList<String> chosenSongs = new ArrayList<>();
    private ArrayList<Integer> chosenDurations = new ArrayList<>();
    private Song prevSong = null;
    private Song nextSong = null;
    private static NotificationManager notificationManager;
    private static Notification.Builder builder;
    private static Boolean fromError = false;

    public void onCreate() {
        super.onCreate();
        songPosn=0;
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        rand = new Random();

        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setShuffle(){shuffle = !shuffle;}
    public void setShuffle(Boolean tf){shuffle = tf;}

    public boolean getShuffle(){return shuffle;}

    public void setChosenYears(ArrayList<Integer> years){ chosenYears = years; }

    public void setChosenTours(ArrayList<String> tours2){ chosenTours = tours2; }

    public void setChosenSongs(ArrayList<String> songs){
        chosenSongs = songs;
    }

    public void setChosenDurations(ArrayList<Integer> durations){
        chosenDurations = durations;
    }

    public ArrayList getChosenYears(){return chosenYears;}

    public ArrayList getChosenSongs(){return chosenSongs;}

    public ArrayList getChosenDurations(){return chosenDurations;}
    public ArrayList getChosenTours(){return chosenTours;}
    public ArrayList getDurations(){return durations;}

    public void setNextSong(Song thisSong){
        nextSong = thisSong;
    }

    public void setList(ArrayList<Song> theSongs, ArrayList<String> theYears, ArrayList<String> theSongNames,
                        ArrayList<String> theDurations, ArrayList<String> theTours){
        songs = theSongs;
        durations = theDurations;
        for (int i = 0; i < theYears.size(); i++)
            chosenYears.add(Integer.parseInt(theYears.get(i)));
        for (int i = 0; i < theSongNames.size(); i++)
            chosenSongs.add(theSongNames.get(i));
        for (int i = 0; i < theDurations.size(); i++)
            chosenDurations.add(i);
        for (int i = 0; i < theTours.size(); i++)
            chosenTours.add(theTours.get(i));
    }

    public void updateList(ArrayList<Song> theSongs, Integer position){
        songs = theSongs;
        songPosn = position;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return musicBind; }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!fromError) {
            if (player.getCurrentPosition() > 0) {
                MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Player")
                        .setAction("Finished Track")
                        .build());
                mp.reset();
                playNext();
            }
        }
        else{
            fromError = false;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Player")
                .setAction("Error")
                .build());
        MainActivity.setNPBanner("Error:");
        MainActivity.setBanner("Error connecting to data source", "Try another track or try again later", "");
        if (notificationManager != null) notificationManager.cancel(NOTIFY_ID);
        fromError = true;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt).setSmallIcon(R.drawable.pjj_icon24x24).setTicker(songTitle).setOngoing(true)
                .setContentTitle("PJJ Playing  ("+songDur+")").setContentText(songTitle);

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, builder.build());
        MainActivity.getMusicController().show(0);
        MainActivity.setNPBanner("Now Playing:");
    }

    public void playSong(){
        player.reset();
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle()+" - "+playSong.getDate()+" "+playSong.getCity()+", "+playSong.getState();
        songDur = playSong.getDuration();

        NetworkInfo activeNetwork = MainActivity.getConnectivityManager().getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            try {
                MainActivity.setNPBanner("Loading:");
                player.setDataSource(getApplicationContext(), Uri.parse("http://www.phishjustjams.com/" + playSong.getUrl()));
                String extras = playSong.getExtras();
                if (extras.equals("")){
                    MainActivity.setBanner(playSong.getTitle()+ " - " +
                                    playSong.getDate() + " " + playSong.getCity() + ", " + playSong.getState(),
                            playSong.getVenue(), playSong.getDuration());
                }
                else if (extras.contains(">")){
                    MainActivity.setBanner(playSong.getTitle()+ " " + playSong.getExtras() + " - " +
                                    playSong.getDate() + " " + playSong.getCity() + ", " + playSong.getState(),
                            playSong.getVenue(), playSong.getDuration());
                }
                else{
                    MainActivity.setBanner(playSong.getTitle()+ " (" + playSong.getExtras() + ") - " +
                                    playSong.getDate() + " " + playSong.getCity() + ", " + playSong.getState(),
                            playSong.getVenue(), playSong.getDuration());
                }

                MainActivity.setSongViewPosition(songPosn);
                MainActivity.getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Player")
                        .setAction("Play Song")
                        .setLabel(playSong.getTitle() + " - " + playSong.getDate())
                        .build());
                player.prepareAsync();
            } catch (Exception e) {
                MainActivity.setNPBanner("Error:");
                Log.e("MUSIC SERVICE", "Error setting data source", e);

                MainActivity.setBanner("Error connecting to data source","Try again later", "");
                notificationManager.cancel(NOTIFY_ID);
                player.stop();
            }
        }
        else{
            MainActivity.setBanner("Not Connected to the Internet","Try again when connected","");
            notificationManager.cancel(NOTIFY_ID);
            player.stop();
        }
    }

    public static Song getCurrSong(){
        return songs.get(songPosn);
    }

    public void setSong(int songIndex){
        songPosn = songIndex;
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){return player.getDuration();}

    public boolean isPng(){return player.isPlaying();}

    public void pausePlayer(){
        player.pause();
        notificationManager.cancel(NOTIFY_ID);
    }

    public void cancelNotification(){
        notificationManager.cancel(NOTIFY_ID);
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    public void stop(){
        player.stop();
        notificationManager.cancel(NOTIFY_ID);
    }

    //plays the previously played track, or simply the track preceeding the current one 
    //Only one previous track is stored, not the entire list of previously played tracks.
    public void playPrev(){
        if (player.getCurrentPosition() <= 5000) {
            if (prevSong == null) {
                songPosn--;
                if (songPosn < 0) songPosn = songs.size() - 1;
            } else {
                for (int i = 0; i < songs.size(); i++) {
                    if (songs.get(i) == prevSong) {
                        songPosn = i;
                        prevSong = null;
                        break;
                    }
                }
            }
        }
        playSong();
    }

    public void setPrevSong(){
        prevSong = songs.get(songPosn);
    }

    //complicated routine to figure out the next track to play based on current filter setup
    public void playNext(){
        prevSong = songs.get(songPosn);
        if (nextSong == null) {
            if (shuffle) {
                int newSong = songPosn;
                int playYear = 1900;
                String playTour = "None";
                int durationRank = -1;
                int playDur;
                String playSongName = "";
                if (chosenSongs.size() == MainActivity.getSongNameList().size()) {
                    Integer counter = 0;
                    while ((newSong == songPosn || !chosenYears.contains(playYear) || !chosenDurations.contains(durationRank) || !chosenTours.contains(playTour))
                            && counter < songs.size()) {
                        newSong = rand.nextInt(songs.size());
                        Song playSong = songs.get(newSong);
                        playYear = Integer.parseInt(playSong.getYear());
                        playDur = playSong.getDuration_minutes();
                        playTour = playSong.getTour();
                        durationRank = playDur / 5;
                        if (durationRank > 4)
                            durationRank = 4;
                        counter++;
                    }
                    if (counter >= songs.size() - 1) {
                        Toast.makeText(getApplicationContext(),
                                "No songs available. Change your options", Toast.LENGTH_LONG).show();
                    }
                    songPosn = newSong;
                } else {
                    Integer counter = 0;
                    while ((newSong == songPosn || !chosenYears.contains(playYear) ||
                            !chosenSongs.contains(playSongName) || !chosenDurations.contains(durationRank)) || !chosenTours.contains(playTour)
                            && counter < songs.size()) {
                        newSong = rand.nextInt(songs.size());
                        Song playSong = songs.get(newSong);
                        playYear = Integer.parseInt(playSong.getYear());
                        playSongName = playSong.getTitle().split(" - ")[0].split(" \\(")[0].split(" >")[0];
                        playDur = playSong.getDuration_minutes();
                        playTour = playSong.getTour();
                        durationRank = playDur / 5;
                        if (durationRank > 4)
                            durationRank = 4;
                        counter++;
                    }
                    if (counter >= songs.size() - 1) {
                        Toast.makeText(getApplicationContext(),
                                "No songs available. Change your options. Enjoy this random selection!", Toast.LENGTH_LONG).show();
                    }
                    songPosn = newSong;
                }
            } else {
                int newSong = songPosn;
                int playYear = 1900;
                String playTour = "None";
                int durationRank = -1;
                int playDur;
                String playSongName = "";
                if (chosenSongs.size() == MainActivity.getSongNameList().size()) {
                    Integer counter = 0;
                    while ((newSong == songPosn || !chosenYears.contains(playYear) || !chosenDurations.contains(durationRank) || !chosenTours.contains(playTour))
                            && counter < songs.size()) {
                        newSong++;
                        if (newSong >= songs.size()) newSong = 0;
                        Song playSong = songs.get(newSong);
                        playYear = Integer.parseInt(playSong.getYear());
                        playDur = playSong.getDuration_minutes();
                        playTour = playSong.getTour();
                        durationRank = playDur / 5;
                        if (durationRank > 4)
                            durationRank = 4;
                        counter++;
                    }
                    if (counter >= songs.size() - 1) {
                        Toast.makeText(getApplicationContext(),
                                "No songs available. Change your options", Toast.LENGTH_LONG).show();
                    }
                    songPosn = newSong;
                } else {
                    Integer counter = 0;
                    while ((newSong == songPosn || !chosenYears.contains(playYear) ||
                            !chosenSongs.contains(playSongName) || !chosenDurations.contains(durationRank)) || !chosenTours.contains(playTour)
                            && counter < songs.size()) {
                        newSong++;
                        if (newSong >= songs.size()) newSong = 0;
                        Song playSong = songs.get(newSong);
                        playYear = Integer.parseInt(playSong.getYear());
                        playSongName = playSong.getTitle().split(" - ")[0].split(" \\(")[0].split(" >")[0];
                        playDur = playSong.getDuration_minutes();
                        playTour = playSong.getTour();
                        durationRank = playDur / 5;
                        if (durationRank > 4)
                            durationRank = 4;
                        counter++;
                    }
                    if (counter >= songs.size() - 1) {
                        Toast.makeText(getApplicationContext(),
                                "No songs available. Change your options. Enjoy this random selection!", Toast.LENGTH_LONG).show();
                    }
                    songPosn = newSong;
                }
                //songPosn++;
                //if (songPosn >= songs.size()) songPosn = 0;
            }
        }
        else{
            for (int i = 0; i < songs.size(); i++){
                if (songs.get(i) == nextSong){
                    songPosn = i;
                    nextSong = null;
                    break;
                }
            }
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

}
