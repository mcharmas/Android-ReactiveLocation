package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class AddGeofenceObservable extends BaseLocationObservable<AddGeofenceResult> {
    private final GeofencingRequest request;
    private final PendingIntent geofenceTransitionPendingIntent;

    public static Observable<AddGeofenceResult> createObservable(Context ctx, GeofencingRequest request, PendingIntent geofenceTransitionPendingIntent) {
        return Observable.create(new AddGeofenceObservable(ctx, request, geofenceTransitionPendingIntent));
    }

    private AddGeofenceObservable(Context ctx, GeofencingRequest request, PendingIntent geofenceTransitionPendingIntent) {
        super(ctx);
        this.request = request;
        this.geofenceTransitionPendingIntent = geofenceTransitionPendingIntent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super AddGeofenceResult> observer) {
        LocationServices.GeofencingApi.addGeofences(apiClient, request, geofenceTransitionPendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        AddGeofenceResult result = new AddGeofenceResult(status.getStatusCode());
                        if (!result.isSuccess()) {
                            observer.onError(new AddGeofenceException(result));
                        } else {
                            observer.onNext(result);
                            observer.onCompleted();
                        }
                    }
                });
    }

}
