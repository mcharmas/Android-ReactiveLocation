package pl.charmas.android.reactivelocation2.observables.location;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;


public class LocationUpdatesObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Location> {
    public static Observable<Location> createObservable(Context ctx, LocationRequest locationRequest) {
        return Observable.create(new LocationUpdatesObservableOnSubscribe(ctx, locationRequest));
    }

    private final LocationRequest locationRequest;
    private LocationListener listener;

    private LocationUpdatesObservableOnSubscribe(Context ctx, LocationRequest locationRequest) {
        super(ctx);
        this.locationRequest = locationRequest;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<Location> emitter) {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                emitter.onNext(location);
            }
        };
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, listener);
    }

    @Override
    protected void onDisposed(GoogleApiClient locationClient) {
        if (locationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, listener);
        }
    }

}
