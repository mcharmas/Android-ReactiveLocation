package pl.charmas.android.reactivelocation2.observables.location;

import android.location.Location;
import androidx.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;
import pl.charmas.android.reactivelocation2.observables.StatusException;

@SuppressWarnings("MissingPermission")
public class MockLocationObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Status> {
    private final Observable<Location> locationObservable;
    private Disposable mockLocationSubscription;

    public static Observable<Status> createObservable(ObservableContext context, ObservableFactory factory, Observable<Location> locationObservable) {
        return factory.createObservable(new MockLocationObservableOnSubscribe(context, locationObservable));
    }

    private MockLocationObservableOnSubscribe(ObservableContext ctx, Observable<Location> locationObservable) {
        super(ctx);
        this.locationObservable = locationObservable;
    }

    @Override
    protected void onGoogleApiClientReady(final GoogleApiClient apiClient, final ObservableEmitter<? super Status> emitter) {
        // this throws SecurityException if permissions are bad or mock locations are not enabled,
        // which is passed to observer's onError by BaseObservable
        LocationServices.FusedLocationApi.setMockMode(apiClient, true)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            startLocationMocking(apiClient, emitter);
                        } else {
                            emitter.onError(new StatusException(status));
                        }
                    }
                });
    }

    private void startLocationMocking(final GoogleApiClient apiClient, final ObservableEmitter<? super Status> emitter) {
        mockLocationSubscription = locationObservable
                .subscribe(new Consumer<Location>() {
                               @Override
                               public void accept(Location location) throws Exception {
                                   LocationServices.FusedLocationApi.setMockLocation(apiClient, location)
                                           .setResultCallback(new ResultCallback<Status>() {
                                               @Override
                                               public void onResult(@NonNull Status status) {
                                                   if (status.isSuccess()) {
                                                       emitter.onNext(status);
                                                   } else {
                                                       emitter.onError(new StatusException(status));
                                                   }
                                               }
                                           });
                               }
                           },

                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                emitter.onError(throwable);
                            }

                        }, new Action() {
                            @Override
                            public void run() throws Exception {
                                emitter.onComplete();
                            }
                        });

    }

    @Override
    protected void onDisposed(GoogleApiClient locationClient) {
        if (locationClient.isConnected()) {
            try {
                LocationServices.FusedLocationApi.setMockMode(locationClient, false);
            } catch (SecurityException e) {
                // if this happens then we couldn't have switched mock mode on in the first place,
                // and the observer's onError will already have been called
            }
        }
        if (mockLocationSubscription != null && !mockLocationSubscription.isDisposed()) {
            mockLocationSubscription.dispose();
        }
    }
}
