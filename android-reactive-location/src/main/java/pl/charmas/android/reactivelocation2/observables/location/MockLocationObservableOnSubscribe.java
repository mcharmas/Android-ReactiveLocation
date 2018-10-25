package pl.charmas.android.reactivelocation2.observables.location;

import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import pl.charmas.android.reactivelocation2.BaseFailureListener;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;

@SuppressWarnings("MissingPermission")
public class MockLocationObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Void> {
    private final Observable<Location> locationObservable;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Disposable mockLocationSubscription;

    public static Observable<Void> createObservable(ObservableContext context, ObservableFactory factory, Observable<Location> locationObservable) {
        return factory.createObservable(new MockLocationObservableOnSubscribe(context, locationObservable));
    }

    private MockLocationObservableOnSubscribe(ObservableContext ctx, Observable<Location> locationObservable) {
        super(ctx);
        this.locationObservable = locationObservable;
    }

    @Override
    protected void onLocationProviderClientReady(final FusedLocationProviderClient locationProviderClient,
                                                 final ObservableEmitter<? super Void> emitter) {
        fusedLocationProviderClient = locationProviderClient;
        locationProviderClient.setMockMode(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startLocationMocking(locationProviderClient, emitter);
                    }
                })
                .addOnFailureListener(new BaseFailureListener<>(emitter));
    }

    private void startLocationMocking(final FusedLocationProviderClient locationProviderClient, final ObservableEmitter<? super Void> emitter) {
        mockLocationSubscription = locationObservable
                .subscribe(new Consumer<Location>() {
                               @Override
                               public void accept(Location location) {
                                   locationProviderClient.setMockLocation(location)
                                           .addOnFailureListener(new BaseFailureListener<>(emitter));
                               }
                           },

                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                emitter.onError(throwable);
                            }

                        }, new Action() {
                            @Override
                            public void run() {
                                emitter.onComplete();
                            }
                        });

    }

    @Override
    protected void onDisposed() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.setMockMode(false);
        }
        if (mockLocationSubscription != null && !mockLocationSubscription.isDisposed()) {
            mockLocationSubscription.dispose();
        }
    }
}
