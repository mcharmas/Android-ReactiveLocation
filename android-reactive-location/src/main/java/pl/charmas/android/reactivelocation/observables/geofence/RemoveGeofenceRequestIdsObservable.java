package pl.charmas.android.reactivelocation.observables.geofence;

import android.content.Context;

import com.google.android.gms.location.LocationClient;

import java.util.List;

import rx.Observer;

class RemoveGeofenceRequestIdsObservable extends RemoveGeofenceObservable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> {
    private final List<String> geofenceRequestId;

    RemoveGeofenceRequestIdsObservable(Context ctx, List<String> geofenceRequestId) {
        super(ctx);
        this.geofenceRequestId = geofenceRequestId;
    }

    @Override
    protected void deliverResultToObserver(RemoveGeofencesResult result, Observer<? super RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> observer) {
        observer.onNext((RemoveGeofencesResult.RequestIdsRemoveGeofenceResult) result);
    }

    @Override
    protected void removeGeofences(LocationClient locationClient, LocationClient.OnRemoveGeofencesResultListener onRemoveGeofencesResultListener) {
        locationClient.removeGeofences(geofenceRequestId, onRemoveGeofencesResultListener);
    }
}
