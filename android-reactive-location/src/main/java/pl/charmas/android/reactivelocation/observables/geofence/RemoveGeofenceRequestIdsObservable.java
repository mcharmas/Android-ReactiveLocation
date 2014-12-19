package pl.charmas.android.reactivelocation.observables.geofence;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import rx.Observer;

class RemoveGeofenceRequestIdsObservable extends
        RemoveGeofenceObservable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> {
    private final List<String> geofenceRequestIds;

    RemoveGeofenceRequestIdsObservable(Context ctx, List<String> geofenceRequestIds) {
        super(ctx);
        this.geofenceRequestIds = geofenceRequestIds;
    }

    @Override
    protected void deliverResultToObserver(RemoveGeofencesResult result,
                                           Observer<? super RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> observer) {
        observer.onNext((RemoveGeofencesResult.RequestIdsRemoveGeofenceResult) result);
    }

    @Override
    protected void removeGeofences(GoogleApiClient locationClient,
                                   final Observer<? super RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> observer) {
        LocationServices.GeofencingApi.removeGeofences(locationClient, geofenceRequestIds)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        RemoveGeofencesResult.RequestIdsRemoveGeofenceResult result =
                                new RemoveGeofencesResult.RequestIdsRemoveGeofenceResult(status.getStatusCode(), geofenceRequestIds);

                        if (result.isSuccess()) {
                            deliverResultToObserver(result, observer);
                            observer.onCompleted();
                        } else {
                            observer.onError(new RemoveGeofencesException(result.getStatusCode()));
                        }
                    }
                });
    }
}
