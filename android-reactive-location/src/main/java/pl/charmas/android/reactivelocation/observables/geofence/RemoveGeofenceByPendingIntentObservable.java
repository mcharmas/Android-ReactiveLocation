package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import android.app.PendingIntent;
import android.content.Context;

import rx.Observer;

class RemoveGeofenceByPendingIntentObservable
        extends RemoveGeofenceObservable<RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> {

    private final PendingIntent pendingIntent;

    RemoveGeofenceByPendingIntentObservable(Context ctx, PendingIntent pendingIntent) {
        super(ctx);
        this.pendingIntent = pendingIntent;
    }

    @Override
    protected void deliverResultToObserver(Result result,
            Observer<? super RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> observer) {
        observer.onNext((RemoveGeofencesResult.PengingIntentRemoveGeofenceResult) result);
    }

    @Override
    protected void removeGeofences(GoogleApiClient googleApiClient,
            final Observer<? super RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> observer) {
        PendingResult<Status> result = LocationServices.GeofencingApi
                .removeGeofences(googleApiClient, pendingIntent);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (!status.isSuccess()) {
                    observer.onError(new RemoveGeofencesException(status));
                } else {
                    deliverResultToObserver(status, observer);
                    observer.onCompleted();
                }
            }
        });
    }

}
