package pl.charmas.android.reactivelocation.observables.location;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import pl.charmas.android.reactivelocation.observables.StatusException;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

public class MockLocationObservable extends BaseLocationObservable<Status> {
    private Observable<Location> locationObservable;
    private Subscription mockLocationSubscription;

    public static Observable<Status> createObservable(Context context, Observable<Location> locationObservable) {
        return Observable.create(new MockLocationObservable(context, locationObservable));
    }

    protected MockLocationObservable(Context ctx, Observable<Location> locationObservable) {
        super(ctx);
        this.locationObservable = locationObservable;
    }

    @Override
    protected void onGoogleApiClientReady(final GoogleApiClient apiClient, final Observer<? super Status> observer) {
        // this throws SecurityException if permissions are bad or mock locations are not enabled,
        // which is passed to observer's onError by BaseObservable
        LocationServices.FusedLocationApi.setMockMode(apiClient, true)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            observer.onError(new StatusException(status));
                        } else {
                            startLocationMocking(apiClient, observer);
                        }
                    }
                });
    }

    private void startLocationMocking(final GoogleApiClient apiClient, final Observer<? super Status> observer) {
        mockLocationSubscription = locationObservable.subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                LocationServices.FusedLocationApi.setMockLocation(apiClient, location)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (!status.isSuccess()) {
                                    observer.onError(new StatusException(status));
                                } else {
                                    observer.onNext(status);
                                }
                            }
                        });
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                observer.onError(throwable);
            }
        }, new Action0() {
            @Override
            public void call() {
                observer.onCompleted();
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
        if (mockLocationSubscription != null && !mockLocationSubscription.isUnsubscribed()) {
            mockLocationSubscription.unsubscribe();
        }
    }
}
