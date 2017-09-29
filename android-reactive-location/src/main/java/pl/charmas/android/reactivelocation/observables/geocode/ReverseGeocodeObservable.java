package pl.charmas.android.reactivelocation.observables.geocode;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class ReverseGeocodeObservable implements Observable.OnSubscribe<List<Address>> {
    private final Context ctx;
    private final Locale locale;
    private final double latitude;
    private final double longitude;
    private final int maxResults;

    public static Observable<List<Address>> createObservable(Context ctx, Locale locale, double latitude, double longitude, int maxResults) {
        return Observable.create(new ReverseGeocodeObservable(ctx, locale, latitude, longitude, maxResults));
    }

    private ReverseGeocodeObservable(Context ctx, Locale locale, double latitude, double longitude, int maxResults) {
        this.ctx = ctx;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxResults = maxResults;
        this.locale = locale;
    }

    @Override
    public void call(final Subscriber<? super List<Address>> subscriber) {
        Geocoder geocoder = new Geocoder(ctx, locale);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, maxResults);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(addresses);
                subscriber.onCompleted();
            }
        } catch (IOException e) {
            // If it's a service not available error try a different approach using google web api
            if (!subscriber.isUnsubscribed()) {
                Observable
                        .create(new FallbackReverseGeocodeObservable(locale, latitude, longitude, maxResults))
                        .subscribeOn(Schedulers.io())
                        .subscribe(subscriber);
            } else if (!subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        }
    }
}
