package com.ghelius.trafficicon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.util.Log;

public class OnAlarmReceiver extends BroadcastReceiver {
    static final String TAG = "traffic-alarm-receiver";
    static long prevRx = 0;
    static long prevTx = 0;
    static long globalRx = 0;
    static long globalTx = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        //WakefulIntentService.sendWakefulWork(context, WatchService.class);
        Log.d(TAG,"Alarm received");
        if (intent.getExtras().getString("type").equals("reset")) {
            Log.d(TAG,"it's 'reset' alarm");
            globalTx = 0;
            globalRx = 0;
            //TODO: save traffic value
        } else if (intent.getExtras().getString("type").equals("update")) {
            Log.d(TAG, "it's 'update' alarm");
        }
        notifyConnect(context, TrafficStats.getMobileRxBytes(), TrafficStats.getMobileTxBytes());
    }


    private void notifyConnect(Context context, long rx, long tx) {
        Log.d(TAG,"context: rx:" + rx + ", tx:" + tx  + ", prevRx:" + prevRx + ", prevTx:" + prevTx + ", globalRx:" + globalRx + ", globalTx:" + globalTx);

        if (prevRx < rx) {
            Log.d(TAG,"rx diff: " + (rx-prevRx));
            globalRx += rx-prevRx;
        }
        if (prevTx < tx) {
            Log.d(TAG,"tx diff: " + (tx-prevTx));
            globalTx += tx-prevTx;
        }
        prevRx = rx;
        prevTx = tx;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        Intent intent = new Intent(context, MyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(context, 0,
                intent, 0);

        long totalMb = (globalRx+globalTx)/(1024*1024);
        if (totalMb > 100) {
            totalMb = 100;
        }
        String iconName = "big" + String.format("%03d", totalMb);
        Log.d(TAG,"iconName: ["+iconName+"]");
        int resID = context.getResources().getIdentifier (iconName , "drawable", context.getPackageName());
        if (resID == 0) {
            resID = R.drawable.ic_launcher;
        }
        notification.icon = resID;
        notification.tickerText = "";
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
        float rxf = globalRx;
        float txf = globalTx;
        String units = "";
        if (rxf > 1024) {
            units = "KB";
            rxf = rxf/1024;
            txf = txf/1024;
        }
        if (rxf > 1024) {
            units = "MB";
            rxf = rxf/1024;
            txf = txf/1024;
        }
        if (rxf > 1024) {
            units = "GB";
            rxf = rxf/1024;
            txf = txf/1024;
        }
        notification.setLatestEventInfo(context, "Total mobile traffic: " + String.format("%.2f",rxf+txf) +" "+ units ,
                                                 "rx: " + String.format("%.2f",rxf) + " " + units + ", tx: " + String.format("%.2f",txf) +" " + units , pendIntent);
        notificationManager.notify(1, notification);
    }
}