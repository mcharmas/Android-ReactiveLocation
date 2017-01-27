package pl.charmas.android.reactivelocation2.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.StatusException;


class RemoveGeofenceByPendingIntentObservableOnSubscribe extends RemoveGeofenceObservableOnSubscribe<Status> {
    private final PendingIntent pendingIntent;

    RemoveGeofenceByPendingIntentObservableOnSubscribe(Context ctx, PendingIntent pendingIntent) {
        super(ctx);
        this.pendingIntent = pendingIntent;
    }

    @Override
    protected void removeGeofences(GoogleApiClient locationClient, final ObservableEmitter<Status> emitter) {
        LocationServices.GeofencingApi.removeGeofences(locationClient, pendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            emitter.onNext(status);
                            emitter.onComplete();
                        } else {
                            emitter.onError(new StatusException(status));
                        }
                    }
                });
    }
}
