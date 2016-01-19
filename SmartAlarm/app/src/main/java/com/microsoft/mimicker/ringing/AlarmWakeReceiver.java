package com.microsoft.mimicker.ringing;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * This class is a special BroadcastReceiver that receives the PendingIntent from the AlarmManager
 * while holding the wakelock.  It forward the intent to the AlarmRingingService to dispath the
 * alarm. The service calls AlarmWakeReceiver.completeWakefulIntent() once it has acquired the
 * wakelock to ensure the system does not kill the service.
 */
public class AlarmWakeReceiver extends WakefulBroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    // We use a broadcast receiver with the PendingIntent for the AlarmManager, as this approach
    // is more reliable that using a service. See for details: http://hiqes.com/android-alarm-ins-outs/
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast from AlarmManager received!");
        Intent serviceIntent = new Intent(AlarmRingingService.ACTION_DISPATCH_ALARM);
        serviceIntent.setClass(context, AlarmRingingService.class);
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
