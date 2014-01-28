package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.location.LocationClient;

import rx.Observer;

class PendingIntentRemoveGeofenceObservable extends RemoveGeofenceObservable<RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> {
    private final PendingIntent pendingIntent;

    PendingIntentRemoveGeofenceObservable(Context ctx, PendingIntent pendingIntent) {
        super(ctx);
        this.pendingIntent = pendingIntent;
    }

    @Override
    protected void deliverResultToObserver(RemoveGeofencesResult result, Observer<? super RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> observer) {
        observer.onNext((RemoveGeofencesResult.PengingIntentRemoveGeofenceResult) result);
    }

    @Override
    protected void removeGeofences(LocationClient locationClient, LocationClient.OnRemoveGeofencesResultListener onRemoveGeofencesResultListener) {
        locationClient.removeGeofences(pendingIntent, onRemoveGeofencesResultListener);
    }
}
