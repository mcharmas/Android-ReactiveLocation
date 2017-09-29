package pl.charmas.android.reactivelocation.observables.location;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import pl.charmas.android.reactivelocation.observables.ObservableContext;
import rx.Observable;
import rx.Observer;

public class LocationUpdatesObservable extends BaseLocationObservable<Location> {

    public static Observable<Location> createObservable(ObservableContext ctx, LocationRequest locationRequest) {
        Observable<Location> observable = Observable.create(new LocationUpdatesObservable(ctx, locationRequest));
        int requestedNumberOfUpdates = locationRequest.getNumUpdates();
        if (requestedNumberOfUpdates > 0 && requestedNumberOfUpdates < Integer.MAX_VALUE) {
            observable = observable.take(requestedNumberOfUpdates);
        }
        return observable;
    }

    private final LocationRequest locationRequest;
    private LocationListener listener;

    private LocationUpdatesObservable(ObservableContext ctx, LocationRequest locationRequest) {
        super(ctx);
        this.locationRequest = locationRequest;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super Location> observer) {
        listener = new LocationUpdatesLocationListener(observer);
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, listener);
    }

    @Override
    protected void onUnsubscribed(GoogleApiClient locationClient) {
        if (locationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, listener);
        }
    }

    private static class LocationUpdatesLocationListener implements LocationListener {
        private final WeakReference<Observer<? super Location>> weakRef;

        LocationUpdatesLocationListener(Observer<? super Location> observer) {
            this.weakRef = new WeakReference<Observer<? super Location>>(observer);
        }

        @Override
        public void onLocationChanged(Location location) {
            final Observer<? super Location> observer = weakRef.get();
            if (observer != null) {
                observer.onNext(location);
            }
        }
    }
}
