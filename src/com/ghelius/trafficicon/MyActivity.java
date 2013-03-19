package com.ghelius.trafficicon;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Calendar;

public class MyActivity extends Activity {

    static final String TAG = "traffic-icon";
    static final int UPDATE_DATA_ALARM = 29;
    static final int RESET_ALARM = 30;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                checkConnection();
            }
        }, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private void checkConnection () {
        if (isDataConnected()) {
            Log.d(TAG,"3g connected");
            scheduleAlarmWatcher(true);
        } else {
            Log.d(TAG,"3g disconnected");
            scheduleAlarmWatcher(false);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }
    }

    private boolean isDataConnected() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getDataState() == TelephonyManager.DATA_CONNECTED) {
                Log.d(TAG,"Data connected");
                return true;
            } else Log.d(TAG,"Data disconnected");

        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void onResume () {
        super.onResume();
    }

    @Override
    public void onPause () {
        super.onPause();
        checkConnection();
        scheduleResetAlarm();
    }

    void scheduleAlarmWatcher (boolean turnOn) {
        Log.d(TAG,"Stoping update-alarm");
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, OnAlarmReceiver.class);
        i.putExtra("type","update");
        PendingIntent pi=PendingIntent.getBroadcast(this, UPDATE_DATA_ALARM, i, 0);
        try {
            am.cancel(pi);
        } catch (Exception e) {
            Log.e(TAG,"cancel pending intent of AlarmManager failed");
            e.getMessage();
        }
        if (turnOn) {
            Log.d(TAG,"Starting update-alarm");
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                 SystemClock.elapsedRealtime() + (1 * 1000), // 1 minute
                 (60000 * 1),
                 pi);
        }
    }

    void scheduleResetAlarm () {
        Log.d(TAG, "Starting reset alarm");
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, OnAlarmReceiver.class);
        i.putExtra("type","reset");
        PendingIntent pi=PendingIntent.getBroadcast(this, RESET_ALARM, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

}
