package pl.charmas.android.reactivelocation2.observables.location;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;

@SuppressWarnings("MissingPermission")
public class LastKnownLocationObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Location> {
    public static Observable<Location> createObservable(ObservableContext ctx, ObservableFactory factory) {
        return factory.createObservable(new LastKnownLocationObservableOnSubscribe(ctx));
    }

    private LastKnownLocationObservableOnSubscribe(ObservableContext ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, ObservableEmitter<? super Location> emitter) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if (emitter.isDisposed()) return;
        if (location != null) {
            emitter.onNext(location);
        }
        emitter.onComplete();
    }
}
