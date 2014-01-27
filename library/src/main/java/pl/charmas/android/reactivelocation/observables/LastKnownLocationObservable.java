package pl.charmas.android.reactivelocation.observables;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationClient;

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
    protected void onLocationClientReady(LocationClient locationClient, Observer<? super Location> observer) {
        observer.onNext(locationClient.getLastLocation());
        observer.onCompleted();
    }

    @Override
    protected void onLocationClientDisconnected(Observer<? super Location> observer) {
    }
}
