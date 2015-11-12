# PJJApp
This is the code for the PhishJustJams Android App, which can be found in the Google Play Store
at https://play.google.com/store/apps/details?id=com.phishjustjams.phishjustjams.
It is a greatly expanded version of the music player developed on tutsplus.com in a series that begins
at this address: http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764.

The app is a fairly basic music player that reads a JSON file from a remote server, this file contains
the list of all of the tracks that the app can play along with various metadata.  This metadata can
then be used to filter the tracks to create playlists on the fly on similar tracks.  When you filter the
playlist, you can either look at only the filtered tracks or the entire list.  The
PlayNext() function is set up to determine a random track to play next from among the filtered tracks.
It is a bit complicated because it is set up to deal with both the case when you are displaying only
the filtered tracks, and the case when you are looking at all of the tracks, but still have filters
applied.  There are receivers set up to listen for phone calls and the headphones being removed, both
of which will stop the playing of the track.

I have not included ALL of the files needed for the app, but these are all of the main files not
dealing with Gradle, etc.  The java files are kept in 'app/src/main/java/com/phishjustjams/pjjtest'.
'MainActivity.java' is the main file, and is called by the app when it starts up.  'HeadphoneReceiver.java'
and 'IncomingCall.java' are receiver files.  'Query.java' describes a new window where the filters
are set using the 'DialogXXX.java' files. 'Song.java' and 'SongAdapter.java' set up backbone for the
songs, while 'MusicController.java' and 'MusicService.java' deal with the music player.

The XML files determining the layout and menu are found in 'app/src/main/res/'.  The main layout file is
'layout/activity_main.xml', and the main menu layout is found in 'menu/menu_main.xml'.


