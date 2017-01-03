package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation.observables.StatusException;


class RemoveGeofenceByPendingIntentObservable extends RemoveGeofenceObservable<Status> {
    private final PendingIntent pendingIntent;

    RemoveGeofenceByPendingIntentObservable(Context ctx, PendingIntent pendingIntent) {
        super(ctx);
        this.pendingIntent = pendingIntent;
    }

    @Override
    protected void removeGeofences(GoogleApiClient locationClient, final ObservableEmitter<Status> observer) {
        LocationServices.GeofencingApi.removeGeofences(locationClient, pendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            observer.onNext(status);
                            observer.onComplete();
                        } else {
                            observer.onError(new StatusException(status));
                        }
                    }
                });
    }
}
