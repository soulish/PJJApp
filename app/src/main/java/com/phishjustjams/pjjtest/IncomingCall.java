package com.phishjustjams.pjjtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.MediaController;

public class IncomingCall extends BroadcastReceiver {

    private MediaController.MediaPlayerControl mPlayer;

    public IncomingCall() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            TelephonyManager tmgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener PhoneListener = new MyPhoneStateListener();
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception e){
            Log.e("Phone Receive Error", "" + e);
        }
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        public MyPhoneStateListener(){
        }

        public void onCallStateChanged(int state, String incomingNumber){
            if (state == TelephonyManager.CALL_STATE_RINGING){
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
            }
        }
    }

}

