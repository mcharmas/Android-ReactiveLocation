package pl.charmas.android.reactivelocation.observables.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import pl.charmas.android.reactivelocation.observables.StatusException;


public class AddGeofenceObservable extends BaseLocationObservable<Status> {
    private final GeofencingRequest request;
    private final PendingIntent geofenceTransitionPendingIntent;

    public static Observable<Status> createObservable(Context ctx, GeofencingRequest request, PendingIntent geofenceTransitionPendingIntent) {
        return Observable.create(new AddGeofenceObservable(ctx, request, geofenceTransitionPendingIntent));
    }

    private AddGeofenceObservable(Context ctx, GeofencingRequest request, PendingIntent geofenceTransitionPendingIntent) {
        super(ctx);

        this.request = request;
        this.geofenceTransitionPendingIntent = geofenceTransitionPendingIntent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<Status> observer) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.GeofencingApi.addGeofences(apiClient, request, geofenceTransitionPendingIntent)
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
