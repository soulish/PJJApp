package com.phishjustjams.pjjtest;

import java.util.ArrayList;

public class Song {
    private String title;
    private String artist;
    private String year;
    private String tour;
    private String duration;
    private Integer duration_minutes;
    private String date;
    private String weekday;
    private String country;
    private String state;
    private String city;
    private String venue;
    private String extras;
    private String url;
    private static ArrayList<String> urls = new ArrayList<>();

    public Song(String songTitle, String songArtist, String songYear, String songTour,
                String songDuration, String songDate, String songWeekday,
                String songCountry, String songState, String songCity, String songVenue,
                String songExtras, String songURL) {
        title = songTitle;
        artist = songArtist;
        year = songYear;
        tour = songTour;
        duration = songDuration;
        date = songDate;
        duration_minutes = Integer.parseInt(songDuration.split(":")[0]);
        weekday = songWeekday;
        country = songCountry;
        state = songState;
        city = songCity;
        venue = songVenue;
        extras = songExtras;
        url = songURL;
        urls.add(songURL);
    }

    public String getTitle() {return title;}
    public String getArtist() {return artist;}
    public String getYear() {return year;}
    public String getTour() {return tour;}
    public String getDuration() {return duration;}
    public Integer getDuration_minutes() {return duration_minutes;}
    public String getDate() {return date;}
    public String getWeekday() {return weekday;}
    public String getCountry() {return country;}
    public String getState() {return state;}
    public String getCity() {return city;}
    public String getVenue() {return venue;}
    public String getExtras() {return extras;}
    public String getUrl() {return url;}
    public static Boolean hasUrl(String testURL){
        return urls.contains(testURL);
    }
}

