package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class AddGeofenceObservable extends BaseLocationObservable<AddGeofenceResult> {
    private final List<Geofence> geofences;
    private final PendingIntent geofenceTransitionPendingIntent;

    public static Observable<AddGeofenceResult> createObservable(Context ctx, List<Geofence> geofences, PendingIntent geofenceTransitionPendingIntent) {
        return Observable.create(new AddGeofenceObservable(ctx, geofences, geofenceTransitionPendingIntent));
    }

    private AddGeofenceObservable(Context ctx, List<Geofence> geofences, PendingIntent geofenceTransitionPendingIntent) {
        super(ctx);
        this.geofences = geofences;
        this.geofenceTransitionPendingIntent = geofenceTransitionPendingIntent;
    }

    @Override
    protected void onLocationClientReady(GoogleApiClient locationClient, final Observer<? super AddGeofenceResult> observer) {
        LocationServices.GeofencingApi.addGeofences(locationClient, geofences, null).setResultCallback(new ResultCallback<Status>() {
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

    @Override
    protected void onLocationClientDisconnected(Observer<? super AddGeofenceResult> observer) {
    }

}
