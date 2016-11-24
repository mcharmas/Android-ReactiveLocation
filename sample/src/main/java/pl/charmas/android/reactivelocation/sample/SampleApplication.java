package pl.charmas.android.reactivelocation.sample;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;


public class SampleApplication extends Application {


    @Override
    public void onCreate() {

        super.onCreate();

        startLocationService(getApplicationContext());
    }

    public static void startLocationService(Context context) {

        Intent i = new Intent(context, LocationService.class);
        final PendingIntent pi = PendingIntent.getService(context, 2, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Fire off the alarm only once
        //am.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), pi);

        //Fire off the alarm repeatedly
        am.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), 10*1000 ,  pi);
    }

    public static void stopLocationService(Context context) {

        Intent i = new Intent(context, LocationService.class);
        final PendingIntent pi = PendingIntent.getService(context, 2, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        Intent intent = new Intent(context, LocationService.class);
        context.stopService(intent);
    }
}