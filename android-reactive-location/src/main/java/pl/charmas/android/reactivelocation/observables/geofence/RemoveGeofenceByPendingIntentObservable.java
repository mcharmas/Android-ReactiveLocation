package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import rx.Observer;

class RemoveGeofenceByPendingIntentObservable extends
        RemoveGeofenceObservable<RemoveGeofencesResult.PendingIntentRemoveGeofenceResult> {
    private final PendingIntent pendingIntent;

    RemoveGeofenceByPendingIntentObservable(Context ctx, PendingIntent pendingIntent) {
        super(ctx);
        this.pendingIntent = pendingIntent;
    }

    @Override
    protected void removeGeofences(GoogleApiClient locationClient,
                                   final Observer<? super RemoveGeofencesResult.PendingIntentRemoveGeofenceResult> observer) {
        LocationServices.GeofencingApi.removeGeofences(locationClient, pendingIntent)
            .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    RemoveGeofencesResult.PendingIntentRemoveGeofenceResult result =
                            new RemoveGeofencesResult.PendingIntentRemoveGeofenceResult(status.getStatusCode(), pendingIntent);

                    if (result.isSuccess()) {
                        observer.onNext(result);
                        observer.onCompleted();
                    } else {
                        observer.onError(new RemoveGeofencesException(result.getStatusCode()));
                    }
                }
            });
    }
}
