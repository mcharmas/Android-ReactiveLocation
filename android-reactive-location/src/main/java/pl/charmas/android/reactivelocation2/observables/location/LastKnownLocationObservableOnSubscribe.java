package pl.charmas.android.reactivelocation2.observables.location;

import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.BaseFailureListener;
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
    protected void onLocationProviderClientReady(FusedLocationProviderClient locationProviderClient,
                                                 final ObservableEmitter<? super Location> emitter) {
        locationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (emitter.isDisposed()) return;
                        if (location != null) {
                            emitter.onNext(location);
                        }
                        emitter.onComplete();
                    }
                })
                .addOnFailureListener(new BaseFailureListener<>(emitter));
    }
}
