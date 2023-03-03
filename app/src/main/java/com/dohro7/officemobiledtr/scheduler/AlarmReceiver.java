package com.dohro7.officemobiledtr.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //@TODO: Add notification that it is already 12:45 PM
        Log.e("Alarm","Triggered");
    }
}
