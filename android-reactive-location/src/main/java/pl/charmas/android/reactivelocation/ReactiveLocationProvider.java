package pl.charmas.android.reactivelocation;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.GeodecodeObservable;
import pl.charmas.android.reactivelocation.observables.LastKnownLocationObservable;
import pl.charmas.android.reactivelocation.observables.LocationUpdatesObservable;
import pl.charmas.android.reactivelocation.observables.geofence.AddGeofenceObservable;
import pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofenceObservable;
import pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofencesResult;
import rx.Observable;

public class ReactiveLocationProvider {

    private final Context ctx;

    public ReactiveLocationProvider(Context ctx) {
        this.ctx = ctx;
    }

    public Observable<Location> getLastKnownLocation() {
        return LastKnownLocationObservable.createObservable(ctx);
    }

    public Observable<Location> getUpdatedLocation(LocationRequest locationRequest) {
        return LocationUpdatesObservable.createObservable(ctx, locationRequest);
    }

    public Observable<List<Address>> getGeocodeObservable(double lat, double lng, int maxResults) {
        return GeodecodeObservable.createObservable(ctx, lat, lng, maxResults);
    }

    public Observable<AddGeofenceObservable.AddGeofenceResult> addGeofences(PendingIntent geofenceTransitionPendingIntent, List<Geofence> geofences) {
        return AddGeofenceObservable.createObservable(ctx, geofences, geofenceTransitionPendingIntent);
    }

    public Observable<RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> removeGeofences(PendingIntent pendingIntent) {
        return RemoveGeofenceObservable.createObservable(ctx, pendingIntent);
    }

    public Observable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> removeGeofences(List<String> requestIds) {
        return RemoveGeofenceObservable.createObservable(ctx, requestIds);
    }


}
