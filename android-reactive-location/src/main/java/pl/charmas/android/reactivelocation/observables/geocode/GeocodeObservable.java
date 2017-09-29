package pl.charmas.android.reactivelocation.observables.geocode;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;

public class GeocodeObservable implements Observable.OnSubscribe<List<Address>> {
    private final Context ctx;
    private final String locationName;
    private final int maxResults;
    private final LatLngBounds bounds;
    private final Locale locale;

    public static Observable<List<Address>> createObservable(Context ctx, String locationName, int maxResults, LatLngBounds bounds, Locale locale) {
        return Observable.create(new GeocodeObservable(ctx, locationName, maxResults, bounds, locale));
    }

    private GeocodeObservable(Context ctx, String locationName, int maxResults, LatLngBounds bounds, Locale locale) {
        this.ctx = ctx;
        this.locationName = locationName;
        this.maxResults = maxResults;
        this.bounds = bounds;
        this.locale = locale;
    }

    @Override
    public void call(Subscriber<? super List<Address>> subscriber) {
        Geocoder geocoder = createGeocoder();
        List<Address> result;

        try {
            if (bounds != null) {
                result = geocoder.getFromLocationName(locationName, maxResults, bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude);
            } else {
                result = geocoder.getFromLocationName(locationName, maxResults);
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        } catch (IOException e) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        }
    }

    @NonNull
    private Geocoder createGeocoder() {
        if (locale != null) return new Geocoder(ctx, locale);
        return new Geocoder(ctx);
    }
}
