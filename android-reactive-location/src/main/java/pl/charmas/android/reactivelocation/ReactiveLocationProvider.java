package pl.charmas.android.reactivelocation;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.activity.ActivityUpdatesObservable;
import pl.charmas.android.reactivelocation.observables.geocode.GeodecodeObservable;
import pl.charmas.android.reactivelocation.observables.geofence.AddGeofenceObservable;
import pl.charmas.android.reactivelocation.observables.geofence.AddGeofenceResult;
import pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofenceObservable;
import pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofencesResult;
import pl.charmas.android.reactivelocation.observables.location.LastKnownLocationObservable;
import pl.charmas.android.reactivelocation.observables.location.LocationUpdatesObservable;
import rx.Observable;

/**
 * Factory of observables that can manipulate location
 * delivered by Google Play Services.
 */
public class ReactiveLocationProvider {
    private final Context ctx;

    public ReactiveLocationProvider(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Creates observable that obtains last known location and than completes.
     * Delivered location can be null in some cases according to
     * {@link com.google.android.gms.location.FusedLocationProviderApi#getLastLocation(com.google.android.gms.common.api.GoogleApiClient)} ()} docs.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation.observables.LocationConnectionException}
     * when there are trouble connecting with Google Play Services and other exceptions that
     * can be thrown on {@link com.google.android.gms.location.FusedLocationProviderApi#getLastLocation(com.google.android.gms.common.api.GoogleApiClient)}.
     * Everything is delivered by {@link rx.Observer#onError(Throwable)}.
     *
     * @return observable that serves last know location
     */
    public Observable<Location> getLastKnownLocation() {
        return LastKnownLocationObservable.createObservable(ctx);
    }

    /**
     * Creates observable that allows to observe infinite stream of location updates.
     * To stop the stream you have to unsubscribe from observable - location updates are
     * then disconnected.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation.observables.LocationConnectionException}
     * when there are trouble connecting with Google Play Services and other exceptions that
     * can be thrown on {@link com.google.android.gms.location.FusedLocationProviderApi#requestLocationUpdates(com.google.android.gms.common.api.GoogleApiClient, com.google.android.gms.location.LocationRequest, com.google.android.gms.location.LocationListener)}.
     * Everything is delivered by {@link rx.Observer#onError(Throwable)}.
     *
     * @param locationRequest request object with info about what kind of location you need
     * @return observable that serves infinite stream of location updates
     */
    public Observable<Location> getUpdatedLocation(LocationRequest locationRequest) {
        return LocationUpdatesObservable.createObservable(ctx, locationRequest);
    }

    /**
     * Creates obserbable that translates latitude and longitude to list of possible addresses using
     * included Geocoder class. You should subscribe for this observable on I/O thread.
     * The stream finishes after address list is available.
     *
     * @param lat        latitude
     * @param lng        longitude
     * @param maxResults maximal number of results you are interested in
     * @return observable that serves list of address based on location
     */
    public Observable<List<Address>> getGeocodeObservable(double lat, double lng, int maxResults) {
        return GeodecodeObservable.createObservable(ctx, lat, lng, maxResults);
    }

    /**
     * Creates observable that adds request and completes when the action is done.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation.observables.LocationConnectionException}
     * when there are trouble connecting with Google Play Services.
     * <p/>
     * The {@link pl.charmas.android.reactivelocation.observables.geofence.AddGeofenceException} is
     * reported only on {@link com.google.android.gms.location.LocationStatusCodes#ERROR}. Every other
     * status is included in {@link pl.charmas.android.reactivelocation.observables.geofence.AddGeofenceResult}.
     * <p/>
     * Other exceptions will be reported that can be thrown on {@link com.google.android.gms.location.GeofencingApi#addGeofences(com.google.android.gms.common.api.GoogleApiClient, com.google.android.gms.location.GeofencingRequest, android.app.PendingIntent)}
     * <p/>
     * Every exception is delivered by {@link rx.Observer#onError(Throwable)}.
     *
     * @param geofenceTransitionPendingIntent pending intent to register on geofence transition
     * @param request                       list of request to add
     * @return observable that adds request
     */
    public Observable<AddGeofenceResult> addGeofences(PendingIntent geofenceTransitionPendingIntent, GeofencingRequest request) {
        return AddGeofenceObservable.createObservable(ctx, request, geofenceTransitionPendingIntent);
    }

    /**
     * Observable that can be used to remove geofences from LocationClient.
     * <p/>
     * The {@link pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofencesException} is
     * reported only on {@link com.google.android.gms.location.LocationStatusCodes#ERROR}. Every other
     * status is included in {@link pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofencesResult}.
     * <p/>
     * Other exceptions will be reported that can be thrown on {@link com.google.android.gms.location.GeofencingApi#removeGeofences(com.google.android.gms.common.api.GoogleApiClient, android.app.PendingIntent)}.
     * <p/>
     * Every exception is delivered by {@link rx.Observer#onError(Throwable)}.
     *
     * @param pendingIntent key of registered geofences
     * @return observable that removed geofences
     */
    public Observable<RemoveGeofencesResult.PendingIntentRemoveGeofenceResult> removeGeofences(PendingIntent pendingIntent) {
        return RemoveGeofenceObservable.createObservable(ctx, pendingIntent);
    }

    /**
     * Observable that can be used to remove geofences from LocationClient.
     * <p/>
     * The {@link pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofencesException} is
     * reported only on {@link com.google.android.gms.location.LocationStatusCodes#ERROR}. Every other
     * status is included in {@link pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofencesResult}.
     * <p/>
     * Other exceptions will be reported that can be thrown on {@link com.google.android.gms.location.GeofencingApi#removeGeofences(com.google.android.gms.common.api.GoogleApiClient, java.util.List)}.
     * <p/>
     * Every exception is delivered by {@link rx.Observer#onError(Throwable)}.
     *
     * @param requestIds geofences to remove
     * @return observable that removed geofences
     */
    public Observable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> removeGeofences(List<String> requestIds) {
        return RemoveGeofenceObservable.createObservable(ctx, requestIds);
    }


    public Observable<DetectedActivity> getDetectedActivity(int detectIntervalMiliseconds) {
        return ActivityUpdatesObservable.createObservable(ctx, detectIntervalMiliseconds);
    }
}
