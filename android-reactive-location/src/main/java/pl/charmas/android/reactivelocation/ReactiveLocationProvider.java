package pl.charmas.android.reactivelocation;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.GoogleAPIClientObservable;
import pl.charmas.android.reactivelocation.observables.PendingResultObservable;
import pl.charmas.android.reactivelocation.observables.activity.ActivityUpdatesObservable;
import pl.charmas.android.reactivelocation.observables.geocode.GeocodeObservable;
import pl.charmas.android.reactivelocation.observables.geocode.ReverseGeocodeObservable;
import pl.charmas.android.reactivelocation.observables.geofence.AddGeofenceObservable;
import pl.charmas.android.reactivelocation.observables.geofence.AddGeofenceResult;
import pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofenceObservable;
import pl.charmas.android.reactivelocation.observables.geofence.RemoveGeofencesResult;
import pl.charmas.android.reactivelocation.observables.location.AddLocationIntentUpdatesObservable;
import pl.charmas.android.reactivelocation.observables.location.LastKnownLocationObservable;
import pl.charmas.android.reactivelocation.observables.location.LocationUpdatesObservable;
import pl.charmas.android.reactivelocation.observables.location.LocationUpdatesResult;
import pl.charmas.android.reactivelocation.observables.location.RemoveLocationIntentUpdatesObservable;
import rx.Observable;
import rx.functions.Func1;

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
     * Delivered location is never null - when it is unavailable Observable completes without emitting
     * any value.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation.observables.GoogleAPIConnectionException}
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
     * Observable can report {@link pl.charmas.android.reactivelocation.observables.GoogleAPIConnectionException}
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
     * Creates an observable that adds a {@link android.app.PendingIntent} as a location
     * listener.
     * <p/>
     * This invokes {@link com.google.android.gms.location.FusedLocationProviderApi#requestLocationUpdates(com.google.android.gms.common.api.GoogleApiClient, com.google.android.gms.location.LocationRequest, android.app.PendingIntent)}.
     * <p/>
     * When location updates are no longer required, a call to {@link #removeLocationUpdates(android.app.PendingIntent)}
     * should be made.
     * @param locationRequest request object with info about what kind of location you need
     * @param intent PendingIntent that will be called with location updates
     * @return observable that adds the request and PendingIntent
     */
    public Observable<LocationUpdatesResult> requestLocationUpdates(LocationRequest locationRequest, PendingIntent intent) {
        return AddLocationIntentUpdatesObservable.createObservable(ctx, locationRequest, intent);
    }

    /**
     * Observable that can be used to remove {@link android.app.PendingIntent} location updates.
     * @param intent PendingIntent to remove location updates for
     * @return observable that removes the PendingIntent
     */
    public Observable<LocationUpdatesResult> removeLocationUpdates(PendingIntent intent) {
        return RemoveLocationIntentUpdatesObservable.createObservable(ctx, intent);
    }

    /**
     * Creates observable that translates latitude and longitude to list of possible addresses using
     * included Geocoder class. You should subscribe for this observable on I/O thread.
     * The stream finishes after address list is available.
     *
     * @param lat        latitude
     * @param lng        longitude
     * @param maxResults maximal number of results you are interested in
     * @return observable that serves list of address based on location
     */
    public Observable<List<Address>> getReverseGeocodeObservable(double lat, double lng, int maxResults) {
        return ReverseGeocodeObservable.createObservable(ctx, lat, lng, maxResults);
    }

    /**
     * Creates observable that translates a street address or other description into a list of
     * possible addresses using included Geocoder class. You should subscribe for this
     * observable on I/O thread.
     * The stream finishes after address list is available.
     *
     * @param locationName a user-supplied description of a location
     * @param maxResults max number of results you are interested in
     * @return observable that serves list of address based on location name
     */
    public Observable<List<Address>> getGeocodeObservable(String locationName, int maxResults) {
        return getGeocodeObservable(locationName, maxResults, null);
    }

    /**
     * Creates observable that translates a street address or other description into a list of
     * possible addresses using included Geocoder class. You should subscribe for this
     * observable on I/O thread.
     * The stream finishes after address list is available.
     * <p/>
     * You may specify a bounding box for the search results.
     *
     * @param locationName a user-supplied description of a location
     * @param maxResults max number of results you are interested in
     * @param bounds restricts the results to geographical bounds. May be null
     * @return observable that serves list of address based on location name
     */
    public Observable<List<Address>> getGeocodeObservable(String locationName, int maxResults, LatLngBounds bounds) {
        return GeocodeObservable.createObservable(ctx, locationName, maxResults, bounds);
    }

    /**
     * Creates observable that adds request and completes when the action is done.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation.observables.GoogleAPIConnectionException}
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
     * @param request                         list of request to add
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


    /**
     * Observable that can be used to observe activity provided by Actity Recognition mechanism.
     *
     * @param detectIntervalMiliseconds detecion interval
     * @return observable that provides activity recognition
     */
    public Observable<ActivityRecognitionResult> getDetectedActivity(int detectIntervalMiliseconds) {
        return ActivityUpdatesObservable.createObservable(ctx, detectIntervalMiliseconds);
    }

    /**
     * Observable that can be used to check settings state for given location request.
     *
     * @param locationRequest location request
     * @return observable that emits check result of location settings
     * @see com.google.android.gms.location.SettingsApi
     */
    public Observable<LocationSettingsResult> checkLocationSettings(final LocationSettingsRequest locationRequest) {
        return getGoogleApiClientObservable(LocationServices.API)
                .flatMap(new Func1<GoogleApiClient, Observable<LocationSettingsResult>>() {
                    @Override
                    public Observable<LocationSettingsResult> call(GoogleApiClient googleApiClient) {
                        return fromPendingResult(LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationRequest));
                    }
                });
    }

    /**
     * Returns observable that fetches current place from Places API. To flatmap and auto release
     * buffer to {@link com.google.android.gms.location.places.PlaceLikelihood} observable use
     * {@link DataBufferObservable}.
     *
     * @param placeFilter filter
     * @return observable that emits current places buffer and completes
     */
    public final Observable<PlaceLikelihoodBuffer> getCurrentPlace(@Nullable final PlaceFilter placeFilter) {
        return getGoogleApiClientObservable(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .flatMap(new Func1<GoogleApiClient, Observable<PlaceLikelihoodBuffer>>() {
                    @Override
                    public Observable<PlaceLikelihoodBuffer> call(GoogleApiClient api) {
                        return fromPendingResult(Places.PlaceDetectionApi.getCurrentPlace(api, placeFilter));
                    }
                });
    }

    /**
     * Returns observable that fetches autocomplete predictions from Places API. To flatmap and autorelease
     * {@link com.google.android.gms.location.places.AutocompletePredictionBuffer} you can use
     * {@link DataBufferObservable}.
     *
     * @param query  search query
     * @param bounds bounds where to fetch suggestions from
     * @param filter filter
     * @return observable with suggestions buffer and completes
     */
    public final Observable<AutocompletePredictionBuffer> getPlaceAutocompletePredictions(final String query, final LatLngBounds bounds, final AutocompleteFilter filter) {
        return getGoogleApiClientObservable(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .flatMap(new Func1<GoogleApiClient, Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> call(GoogleApiClient api) {
                        return fromPendingResult(Places.GeoDataApi.getAutocompletePredictions(api, query, bounds, filter));
                    }
                });
    }

    /**
     * Observable that emits {@link com.google.android.gms.common.api.GoogleApiClient} object after connection.
     * In case of error {@link pl.charmas.android.reactivelocation.observables.GoogleAPIConnectionException} is emmited.
     * When connection to Google Play Services is suspended {@link pl.charmas.android.reactivelocation.observables.GoogleAPIConnectionSuspendedException}
     * is emitted as error.
     * Do not disconnect from apis client manually - just unsubscribe.
     *
     * @param apis collection of apis to connect to
     * @return observable that emits apis client after successful connection
     */
    public final Observable<GoogleApiClient> getGoogleApiClientObservable(Api... apis) {
        //noinspection unchecked
        return GoogleAPIClientObservable.create(ctx, apis);
    }

    /**
     * Util method that wraps {@link com.google.android.gms.common.api.PendingResult} in Observable.
     *
     * @param result pending result to wrap
     * @param <T>    parameter type of result
     * @return observable that emits pending result and completes
     */
    public static <T extends Result> Observable<T> fromPendingResult(PendingResult<T> result) {
        return Observable.create(new PendingResultObservable<>(result));
    }
}
