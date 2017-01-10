package pl.charmas.android.reactivelocation.observables.geocode;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;


public class ReverseGeocodeObservable implements ObservableOnSubscribe<List<Address>> {
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
    public void subscribe(ObservableEmitter<List<Address>> emitter) throws Exception {
        Geocoder geocoder = new Geocoder(ctx, locale);
        try {
            emitter.onNext(geocoder.getFromLocation(latitude, longitude, maxResults));
            emitter.onComplete();

        } catch (IOException e) {
            // If it's a service not available error try a different approach using google web api
            if (e.getMessage().equalsIgnoreCase("Service not Available")) {
                Observable
                        .create(new FallbackReverseGeocodeObservable(locale, latitude, longitude, maxResults))
                        .subscribeOn(Schedulers.io());
//                        .subscribe(subscriber);
            } else {
                emitter.onError(e);
            }
        }
    }
}
