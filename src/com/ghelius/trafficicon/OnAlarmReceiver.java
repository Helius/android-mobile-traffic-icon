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
    @Override
    public void onReceive(Context context, Intent intent) {
        //WakefulIntentService.sendWakefulWork(context, WatchService.class);
        Log.d(TAG,"Alarm received");
        if (intent.getExtras().getString("type").equals("reset")) {
            Log.d(TAG,"it's 'reset' alarm");
            //TODO: save traffic value
        } else if (intent.getExtras().getString("type").equals("update")) {
            Log.d(TAG,"it's 'update' alarm");
            Log.d(TAG, "Mobile Rx:" + TrafficStats.getMobileRxBytes());
            notifyConnect(context, TrafficStats.getMobileRxBytes(), TrafficStats.getMobileTxBytes());
        }
    }


    private void notifyConnect(Context context, long rx, long tx) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        Intent intent = new Intent(context, MyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(context, 0,
                intent, 0);

        long totalMb = (rx+tx)/(1024*1024);
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
        long rxd = 0;
        long txd = 0;
        String units = "";
        if (rx > 1024) {
            units = "KB";
            rxd = rx%1024;
            txd = tx%1024;
            rx = rx/1024;
            tx = tx/1024;
        }
        if (rx > 1024) {
            units = "MB";
            rxd = rx%1024;
            txd = tx%1024;
            rx = rx/1024;
            tx = tx/1024;
        }
        if (rx > 1024) {
            units = "GB";
            rxd = rx%1024;
            txd = tx%1024;
            rx = rx/1024;
            tx = tx/1024;
        }
        notification.setLatestEventInfo(context, "Total traffic: " + (rx+tx) +"." + (rxd+txd) + " "+ units , "rx: " + rx +"." + rxd + " " + units + ", tx: " + tx + "." + txd +" " + units , pendIntent);
        notificationManager.notify(1, notification);
    }
}