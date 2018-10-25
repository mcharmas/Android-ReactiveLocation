package pl.charmas.android.reactivelocation2.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        String transition = mapTransition(event.getGeofenceTransition());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Geofence action")
                .setContentText(transition)
                .setTicker("Geofence action")
                .build();
        nm.notify(0, notification);
    }

    private String mapTransition(int event) {
        switch (event) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "ENTER";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }
}
