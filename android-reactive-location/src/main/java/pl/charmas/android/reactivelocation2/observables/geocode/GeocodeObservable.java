package pl.charmas.android.reactivelocation2.observables.geocode;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;

public class GeocodeObservable implements ObservableOnSubscribe<List<Address>> {
    private final Context ctx;
    private final String locationName;
    private final int maxResults;
    private final LatLngBounds bounds;
    private final Locale locale;

    public static Observable<List<Address>> createObservable(Context ctx, ObservableFactory factory, String locationName, int maxResults, LatLngBounds bounds, Locale locale) {
        return factory.createObservable(new GeocodeObservable(ctx, locationName, maxResults, bounds, locale));
    }

    private GeocodeObservable(Context ctx, String locationName, int maxResults, LatLngBounds bounds, Locale locale) {
        this.ctx = ctx;
        this.locationName = locationName;
        this.maxResults = maxResults;
        this.bounds = bounds;
        this.locale = locale;
    }

    @Override
    public void subscribe(ObservableEmitter<List<Address>> emitter) throws Exception {
        Geocoder geocoder = createGeocoder();
        try {
            List<Address> result = getAddresses(geocoder);
            if (!emitter.isDisposed()) {
                emitter.onNext(result);
                emitter.onComplete();
            }
        } catch (IOException e) {
            if (!emitter.isDisposed()) {
                emitter.onError(e);
            }
        }
    }

    private List<Address> getAddresses(Geocoder geocoder) throws IOException {
        List<Address> result;
        if (bounds != null) {
            result = geocoder.getFromLocationName(locationName, maxResults, bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude);
        } else {
            result = geocoder.getFromLocationName(locationName, maxResults);
        }
        return result;
    }

    @NonNull
    private Geocoder createGeocoder() {
        if (locale != null) return new Geocoder(ctx, locale);
        return new Geocoder(ctx);
    }
}
