package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import android.app.PendingIntent;
import android.content.Context;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class AddGeofenceObservable extends BaseLocationObservable<Result> {

    private final List<Geofence> gefences;

    private final PendingIntent geofenceTransitionPendingIntent;


    private AddGeofenceObservable(Context ctx, List<Geofence> geofences,
            PendingIntent geofenceTransitionPendingIntent) {
        super(ctx);
        this.gefences = geofences;
        this.geofenceTransitionPendingIntent = geofenceTransitionPendingIntent;
    }

    public static Observable<Result> createObservable(Context ctx,
            List<Geofence> geofences, PendingIntent geofenceTransitionPendingIntent) {
        return Observable.create(
                new AddGeofenceObservable(ctx, geofences, geofenceTransitionPendingIntent));
    }

    @Override
    protected void onLocationClientReady(GoogleApiClient locationClient,
            final  Observer<? super Result> observer) {

       final PendingResult<Status> result  = LocationServices.GeofencingApi
                .addGeofences(locationClient, gefences, geofenceTransitionPendingIntent);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (!status.isSuccess()) {
                    observer.onError(new AddGeofenceException(status));
                } else {
                    observer.onNext(status);
                    observer.onCompleted();
                }
            }
        });

    }

    @Override
    protected void onLocationClientDisconnected(Observer<? super Result> observer) {

    }
}
