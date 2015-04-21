package pl.charmas.android.reactivelocation.observables.geocode;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class GeocodeObservable implements Observable.OnSubscribe<List<Address>> {
    private final Context ctx;
    private final String locationName;
    private final int maxResults;
    private final LatLngBounds bounds;

    public static Observable<List<Address>> createObservable(Context ctx, String locationName, int maxResults, LatLngBounds bounds) {
        return Observable.create(new GeocodeObservable(ctx, locationName, maxResults, bounds));
    }

    private GeocodeObservable(Context ctx, String locationName, int maxResults, LatLngBounds bounds) {
        this.ctx = ctx;
        this.locationName = locationName;
        this.maxResults = maxResults;
        this.bounds = bounds;
    }

    @Override
    public void call(Subscriber<? super List<Address>> subscriber) {
        Geocoder geocoder = new Geocoder(ctx);
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
}
