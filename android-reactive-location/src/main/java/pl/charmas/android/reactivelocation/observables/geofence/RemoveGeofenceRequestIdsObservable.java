package pl.charmas.android.reactivelocation.observables.geofence;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation.observables.StatusException;


class RemoveGeofenceRequestIdsObservable extends RemoveGeofenceObservable<Status> {
    private final List<String> geofenceRequestIds;

    RemoveGeofenceRequestIdsObservable(Context ctx, List<String> geofenceRequestIds) {
        super(ctx);
        this.geofenceRequestIds = geofenceRequestIds;
    }

    @Override
    protected void removeGeofences(GoogleApiClient locationClient, final ObservableEmitter<Status> observer) {
        LocationServices.GeofencingApi.removeGeofences(locationClient, geofenceRequestIds)
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
