package pl.charmas.android.reactivelocation2.observables.geofence;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.StatusException;


class RemoveGeofenceRequestIdsObservableOnSubscribe extends RemoveGeofenceObservableOnSubscribe<Status> {
    private final List<String> geofenceRequestIds;

    RemoveGeofenceRequestIdsObservableOnSubscribe(ObservableContext ctx, List<String> geofenceRequestIds) {
        super(ctx);
        this.geofenceRequestIds = geofenceRequestIds;
    }

    @Override
    protected void removeGeofences(GoogleApiClient locationClient, final ObservableEmitter<? super Status> emitter) {
        LocationServices.GeofencingApi.removeGeofences(locationClient, geofenceRequestIds)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (emitter.isDisposed()) return;
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
