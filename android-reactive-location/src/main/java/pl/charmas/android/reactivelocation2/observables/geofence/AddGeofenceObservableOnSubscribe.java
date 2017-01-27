package pl.charmas.android.reactivelocation2.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.StatusException;


public class AddGeofenceObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Status> {
    private final GeofencingRequest request;
    private final PendingIntent geofenceTransitionPendingIntent;

    public static Observable<Status> createObservable(Context ctx, GeofencingRequest request, PendingIntent geofenceTransitionPendingIntent) {
        return Observable.create(new AddGeofenceObservableOnSubscribe(ctx, request, geofenceTransitionPendingIntent));
    }

    private AddGeofenceObservableOnSubscribe(Context ctx, GeofencingRequest request, PendingIntent geofenceTransitionPendingIntent) {
        super(ctx);

        this.request = request;
        this.geofenceTransitionPendingIntent = geofenceTransitionPendingIntent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<Status> emitter) {
        LocationServices.GeofencingApi.addGeofences(apiClient, request, geofenceTransitionPendingIntent)
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
