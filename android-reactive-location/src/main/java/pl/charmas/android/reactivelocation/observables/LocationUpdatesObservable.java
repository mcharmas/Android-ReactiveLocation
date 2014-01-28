package pl.charmas.android.reactivelocation.observables;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import rx.Observable;
import rx.Observer;

public class LocationUpdatesObservable extends BaseLocationObservable<Location> {

    public static Observable<Location> createObservable(Context ctx, LocationRequest locationRequest) {
        return Observable.create(new LocationUpdatesObservable(ctx, locationRequest));
    }

    private final LocationRequest locationRequest;
    private LocationListener listener;

    private LocationUpdatesObservable(Context ctx, LocationRequest locationRequest) {
        super(ctx);
        this.locationRequest = locationRequest;
    }

    @Override
    protected void onLocationClientReady(LocationClient locationClient, final Observer<? super Location> observer) {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                observer.onNext(location);
            }
        };
        locationClient.requestLocationUpdates(locationRequest, listener);
    }

    @Override
    protected void onUnsubscribed(LocationClient locationClient) {
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(listener);
        }
    }

    @Override
    protected void onLocationClientDisconnected(Observer<? super Location> observer) {
        observer.onCompleted();
    }
}
