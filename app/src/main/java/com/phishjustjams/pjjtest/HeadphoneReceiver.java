package com.phishjustjams.pjjtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.MediaController;

public class HeadphoneReceiver extends BroadcastReceiver {
    private Integer headphoneState;
    private MediaController.MediaPlayerControl mPlayer;

    public HeadphoneReceiver() {
        headphoneState = -1;
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            if (headphoneState == -1) {
                headphoneState = state;
            }

            //check if the state has changed, and then if the new state is unplugged=0
            if (state != headphoneState && state == 0) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
            }

            //make sure to update the state
            headphoneState = state;
        }
    }
}

