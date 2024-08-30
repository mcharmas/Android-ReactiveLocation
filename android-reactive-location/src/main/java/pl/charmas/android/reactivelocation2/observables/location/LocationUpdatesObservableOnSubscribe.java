package pl.charmas.android.reactivelocation2.observables.location;

import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.Task;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.BaseFailureListener;
import pl.charmas.android.reactivelocation2.LocationNotAvailableException;
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
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback listener;

    private LocationUpdatesObservableOnSubscribe(ObservableContext ctx, LocationRequest locationRequest) {
        super(ctx);
        this.locationRequest = locationRequest;
    }

    @Override
    protected void onLocationProviderClientReady(FusedLocationProviderClient locationProviderClient,
                                                 final ObservableEmitter<? super Location> emitter) {
        fusedLocationProviderClient = locationProviderClient;
        listener = new LocationUpdatesLocationListener(emitter);
        Task<Void> task = locationProviderClient.requestLocationUpdates(locationRequest, listener, null);
        task.addOnFailureListener(new BaseFailureListener(emitter));
    }

    @Override
    protected void onDisposed() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(listener);
        }
    }

    private static class LocationUpdatesLocationListener extends LocationCallback {
        private final WeakReference<ObservableEmitter<? super Location>> weakRef;

        LocationUpdatesLocationListener(ObservableEmitter<? super Location> emitter) {
            this.weakRef = new WeakReference<ObservableEmitter<? super Location>>(emitter);
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            final ObservableEmitter<? super Location> observer = weakRef.get();
            if (observer != null && !observer.isDisposed()) {
                for (Location location : locationResult.getLocations()) {
                    observer.onNext(location);
                }
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            if (!locationAvailability.isLocationAvailable()) {
                final ObservableEmitter<? super Location> observer = weakRef.get();
                if (observer != null && !observer.isDisposed()) {
                    observer.onError(new LocationNotAvailableException());
                }
            }
        }
    }
}
