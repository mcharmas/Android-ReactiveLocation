package pl.charmas.android.reactivelocation.observables.location;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class LastKnownLocationObservable extends BaseLocationObservable<Location> {

    public static Observable<Location> createObservable(Context ctx) {
        return Observable.create(new LastKnownLocationObservable(ctx));
    }

    private LastKnownLocationObservable(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super Location> observer) {
        observer.onNext(LocationServices.FusedLocationApi.getLastLocation(apiClient));
        observer.onCompleted();
    }
}
