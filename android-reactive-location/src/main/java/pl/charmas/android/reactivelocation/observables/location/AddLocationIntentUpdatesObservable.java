package pl.charmas.android.reactivelocation.observables.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import pl.charmas.android.reactivelocation.observables.StatusException;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class AddLocationIntentUpdatesObservable extends BaseLocationObservable<Status> {
    private final LocationRequest locationRequest;
    private final PendingIntent intent;

    public static Observable<Status> createObservable(Context ctx, LocationRequest locationRequest, PendingIntent intent) {
        return Observable.create(new AddLocationIntentUpdatesObservable(ctx, locationRequest, intent));
    }

    private AddLocationIntentUpdatesObservable(Context ctx, LocationRequest locationRequest, PendingIntent intent) {
        super(ctx);

        this.locationRequest = locationRequest;
        this.intent = intent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<Status> observer) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, intent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            observer.onError(new StatusException(status));
                        } else {
                            observer.onNext(status);
                            observer.onComplete();
                        }
                    }
                });

    }
}
