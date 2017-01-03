package pl.charmas.android.reactivelocation.observables.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class LastKnownLocationObservable extends BaseLocationObservable<Location> {

    public static Observable<Location> createObservable(Context ctx) {
        return Observable.create(new LastKnownLocationObservable(ctx));
    }

    private LastKnownLocationObservable(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, ObservableEmitter<Location> observer) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if (location != null) {
            observer.onNext(location);
        }
        observer.onComplete();
    }
}
