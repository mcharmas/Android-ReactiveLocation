package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import android.content.Context;

import java.util.List;

import rx.Observer;

class RemoveGeofenceRequestIdsObservable
        extends RemoveGeofenceObservable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> {

    private final List<String> geofenceRequestId;


    RemoveGeofenceRequestIdsObservable(Context ctx, List<String> geofenceRequestId) {
        super(ctx);
        this.geofenceRequestId = geofenceRequestId;
    }

    @Override
    protected void deliverResultToObserver(Result result,
            Observer<? super RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> observer) {
        observer.onNext((RemoveGeofencesResult.RequestIdsRemoveGeofenceResult) result);
    }

    @Override
    protected void removeGeofences(GoogleApiClient googleApiClient,
            final Observer<? super RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> observer) {
        PendingResult<Status> result = LocationServices.GeofencingApi
                .removeGeofences(googleApiClient, geofenceRequestId);

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
