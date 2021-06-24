package pl.charmas.android.reactivelocation2.observables.location;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;


@SuppressWarnings("MissingPermission")
public class LocationUpdatesObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Location> {
    public static Observable<Location> createObservable(ObservableContext ctx, ObservableFactory factory, LocationRequest locationRequest) {
        Observable<Location> observable = factory.createObservable(new LocationUpdatesObservableOnSubscribe(ctx, locationRequest));
        int requestedNumberOfUpdates = locationRequest.getNumUpdates();
        if (requestedNumberOfUpdates > 0 && requestedNumberOfUpdates < Integer.MAX_VALUE) {
            observable = observable.take(requestedNumberOfUpdates);
        }
        return observable;
    }

    private final LocationRequest locationRequest;
    private LocationListener listener;

    private LocationUpdatesObservableOnSubscribe(ObservableContext ctx, LocationRequest locationRequest) {
        super(ctx);
        this.locationRequest = locationRequest;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<? super Location> emitter) {
        listener = new LocationUpdatesLocationListener(emitter);
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, listener);
    }

    @Override
    protected void onDisposed(GoogleApiClient locationClient) {
        if (locationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, listener);
        }
    }

    private static class LocationUpdatesLocationListener implements LocationListener {
        private final WeakReference<ObservableEmitter<? super Location>> weakRef;

        LocationUpdatesLocationListener(ObservableEmitter<? super Location> emitter) {
            this.weakRef = new WeakReference<ObservableEmitter<? super Location>>(emitter);
        }

        @Override
        public void onLocationChanged(Location location) {
            final ObservableEmitter<? super Location> observer = weakRef.get();
            if (observer != null && !observer.isDisposed()) {
                observer.onNext(location);
            }
        }
    }
}
