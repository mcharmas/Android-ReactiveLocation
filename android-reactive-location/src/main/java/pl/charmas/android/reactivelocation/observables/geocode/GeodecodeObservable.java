package pl.charmas.android.reactivelocation.observables.geocode;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class GeodecodeObservable implements Observable.OnSubscribeFunc<List<Address>> {
    private final Context ctx;
    private final double latitude;
    private final double longitude;
    private final int maxResults;

    public static Observable<List<Address>> createObservable(Context ctx, double latitude, double longitude, int maxResults) {
        return Observable.create(new GeodecodeObservable(ctx, latitude, longitude, maxResults));
    }

    private GeodecodeObservable(Context ctx, double latitude, double longitude, int maxResults) {
        this.ctx = ctx;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxResults = maxResults;
    }

    @Override
    public Subscription onSubscribe(Observer<? super List<Address>> observer) {
        Geocoder geocoder = new Geocoder(ctx);
        try {
            observer.onNext(geocoder.getFromLocation(latitude, longitude, maxResults));
            observer.onCompleted();
        } catch (IOException e) {
            observer.onError(e);
        }
        return Subscriptions.empty();
    }
}
