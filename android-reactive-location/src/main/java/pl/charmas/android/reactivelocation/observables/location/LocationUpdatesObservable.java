package pl.charmas.android.reactivelocation.observables.location;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class LocationUpdatesObservable extends BaseLocationObservable<Location> {

    private static final String TAG = LocationUpdatesObservable.class.getSimpleName();

    public static Observable<Location> createObservable(Context ctx, LocationRequest locationRequest) {
        return Observable.create(new LocationUpdatesObservable(ctx, locationRequest));
    }

    private WeakReference<LocationRequest> locationRequest;
    private LocationListener listener;

    private LocationUpdatesObservable(Context ctx, LocationRequest locationRequest) {
        super(ctx);
        this.locationRequest = new WeakReference<>(locationRequest);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super Location> observer) {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                observer.onNext(location);
            }
        };
        LocationRequest req = locationRequest.get();
        if (req != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, req, listener);
        } else {
            Log.w("LocUpdatesObservable" ,"Null locationRequest");
        }
    }

    @Override
    protected void onUnsubscribed(GoogleApiClient locationClient) {
        if (locationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, listener);
        }
        listener = null;
    }

}
