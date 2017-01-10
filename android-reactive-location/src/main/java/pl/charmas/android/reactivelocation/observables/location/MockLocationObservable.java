package pl.charmas.android.reactivelocation.observables.location;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import pl.charmas.android.reactivelocation.observables.StatusException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.ContextCompat.checkSelfPermission;


public class MockLocationObservable extends BaseLocationObservable<Status> {
    private Observable<Location> locationObservable;
    private Disposable mockLocationSubscription;

    public static Observable<Status> createObservable(Context context, Observable<Location> locationObservable) {
        return Observable.create(new MockLocationObservable(context, locationObservable));
    }

    protected MockLocationObservable(Context ctx, Observable<Location> locationObservable) {
        super(ctx);
        this.locationObservable = locationObservable;
    }

    @Override
    protected void onGoogleApiClientReady(final GoogleApiClient apiClient, final ObservableEmitter<Status> observer) {
        // this throws SecurityException if permissions are bad or mock locations are not enabled,
        // which is passed to observer's onError by BaseObservable
        if ((checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) &&
                (checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED)) {
            return;
        }

        LocationServices.FusedLocationApi.setMockMode(apiClient, true)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            startLocationMocking(apiClient, observer);
                        } else {
                            observer.onError(new StatusException(status));
                        }
                    }
                });
    }

    private void startLocationMocking(final GoogleApiClient apiClient, final ObservableEmitter<Status> observer) {
        mockLocationSubscription = locationObservable.subscribe(new Consumer<Location>() {
                                                                    @Override
                                                                    public void accept(Location location) throws Exception {
                                                                        if ((checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) &&
                                                                                (checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED)) {
                                                                            return;
                                                                        }

                                                                        LocationServices.FusedLocationApi.setMockLocation(apiClient, location)
                                                                                .setResultCallback(new ResultCallback<Status>() {
                                                                                    @Override
                                                                                    public void onResult(Status status) {
                                                                                        if (status.isSuccess()) {
                                                                                            observer.onNext(status);
                                                                                        } else {
                                                                                            observer.onError(new StatusException(status));
                                                                                        }
                                                                                    }
                                                                                });
                    }
                },

                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        observer.onError(throwable);
                    }

                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        observer.onComplete();
                    }
                });

    }

    @Override
    protected void onUnsubscribed(GoogleApiClient locationClient) {
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
