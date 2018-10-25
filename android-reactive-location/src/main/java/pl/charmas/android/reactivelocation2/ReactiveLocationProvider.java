package pl.charmas.android.reactivelocation2;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import pl.charmas.android.reactivelocation2.observables.GoogleAPIClientObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;
import pl.charmas.android.reactivelocation2.observables.TaskSingleOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.activity.ActivityUpdatesObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.geocode.GeocodeObservable;
import pl.charmas.android.reactivelocation2.observables.geocode.ReverseGeocodeObservable;
import pl.charmas.android.reactivelocation2.observables.geofence.AddGeofenceObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.geofence.RemoveGeofenceObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.location.AddLocationIntentUpdatesObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.location.LastKnownLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.location.LocationUpdatesObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.location.MockLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.location.RemoveLocationIntentUpdatesObservableOnSubscribe;


/**
 * Factory of streams that can manipulate location
 * delivered by Google Play Services.
 */
public class ReactiveLocationProvider {
    private final ObservableContext ctx;
    private final ObservableFactory factory;

    /**
     * Creates location provider instance with default configuration.
     *
     * @param ctx preferably application context
     */
    public ReactiveLocationProvider(Context ctx) {
        this(ctx, ReactiveLocationProviderConfiguration.builder().build());
    }

    /**
     * Create location provider with given {@link ReactiveLocationProviderConfiguration}.
     *
     * @param ctx           preferably application context
     * @param configuration configuration instance
     */
    public ReactiveLocationProvider(Context ctx, ReactiveLocationProviderConfiguration configuration) {
        this.ctx = new ObservableContext(ctx, configuration);
        this.factory = new ObservableFactory(this.ctx);
    }

    /**
     * Creates location provider with custom handler in which all GooglePlayServices callbacks are called.
     *
     * @param ctx     preferably application context
     * @param handler on which all GooglePlayServices callbacks are called
     * @see com.google.android.gms.common.api.GoogleApiClient.Builder#setHandler(android.os.Handler)
     * @deprecated please use {@link ReactiveLocationProvider#ReactiveLocationProvider(Context, ReactiveLocationProviderConfiguration)}
     */
    @Deprecated
    public ReactiveLocationProvider(Context ctx, Handler handler) {
        this(ctx, ReactiveLocationProviderConfiguration.builder().setCustomCallbackHandler(handler).build());
    }

    /**
     * Creates single that obtains last known location.
     * Delivered location is never null - when it is unavailable Observable completes without emitting
     * any value.
     * <p/>
     * Single can report {@link pl.charmas.android.reactivelocation2.observables.GoogleAPIConnectionException}
     * when there are trouble connecting with Google Play Services and other exceptions that can be thrown by the api.
     * <p/>
     * Everything is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @return single that serves last know location
     */
    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public Single<Location> getLastKnownLocation() {
        return LastKnownLocationObservableOnSubscribe.createObservable(ctx, factory).singleOrError();
    }

    /**
     * Creates observable that allows to observe infinite stream of location updates.
     * To stop the stream you have to unsubscribe from observable - location updates are
     * then disconnected.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation2.observables.GoogleAPIConnectionException}
     * when there are trouble connecting with Google Play Services and other exceptions that can be thrown by the api.
     * <p/>
     * Everything is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @param locationRequest request object with info about what kind of location you need
     * @return observable that serves infinite stream of location updates
     */
    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public Observable<Location> getUpdatedLocation(LocationRequest locationRequest) {
        return LocationUpdatesObservableOnSubscribe.createObservable(ctx, factory, locationRequest);
    }

    /**
     * Returns an completable which activates mock location mode when subscribed to, using the
     * supplied observable as a source of mock locations. Mock locations will replace normal
     * location information for all users of the FusedLocationProvider API on the device while this
     * completable is subscribed to.
     * <p/>
     * To use this method, mock locations must be enabled in developer options and your application
     * must hold the android.permission.ACCESS_MOCK_LOCATION permission, or a {@link java.lang.SecurityException}
     * will be thrown.
     * <p/>
     * In case of any issue error is delivered.
     * <p/>
     * Every exception is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @param sourceLocationObservable observable that emits {@link android.location.Location} instances suitable to use as mock locations
     * @return completable that waits until location observable completes
     */
    @RequiresPermission(
            allOf = {"android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.ACCESS_MOCK_LOCATION"}
    )
    public Completable mockLocation(Observable<Location> sourceLocationObservable) {
        return MockLocationObservableOnSubscribe.createObservable(ctx, factory, sourceLocationObservable).ignoreElements();
    }

    /**
     * Creates an completable that adds a {@link android.app.PendingIntent} as a location listener.
     * <p/>
     * When location updates are no longer required, a call to {@link #removeLocationUpdates(android.app.PendingIntent)}
     * should be made.
     * <p/>
     * In case of any issue error is delivered.
     *
     * @param locationRequest request object with info about what kind of location you need
     * @param intent          PendingIntent that will be called with location updates
     * @return completable that adds the request and PendingIntent
     */
    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public Completable requestLocationUpdates(LocationRequest locationRequest, PendingIntent intent) {
        return AddLocationIntentUpdatesObservableOnSubscribe.createObservable(ctx, factory, locationRequest, intent).ignoreElements();
    }

    /**
     * Completable that can be used to remove {@link android.app.PendingIntent} location updates.
     * <p/>
     * In case of any issue error is delivered.
     *
     * @param intent PendingIntent to remove location updates for
     * @return single that removes the PendingIntent
     */
    public Completable removeLocationUpdates(PendingIntent intent) {
        return RemoveLocationIntentUpdatesObservableOnSubscribe.createObservable(ctx, factory, intent).ignoreElements();
    }

    /**
     * Creates single that translates latitude and longitude to list of possible addresses using
     * included Geocoder class. In case geocoder fails with IOException("Service not Available") fallback
     * decoder is used using google web api. You should subscribe on I/O thread.
     *
     * @param lat        latitude
     * @param lng        longitude
     * @param maxResults maximal number of results you are interested in
     * @return single that serves list of address based on location
     */
    public Single<List<Address>> reverseGeocode(double lat, double lng, int maxResults) {
        return ReverseGeocodeObservable.createObservable(ctx.getContext(), factory, Locale.getDefault(), lat, lng, maxResults).singleOrError();
    }

    /**
     * Creates single that translates latitude and longitude to list of possible addresses using
     * included Geocoder class. In case geocoder fails with IOException("Service not Available") fallback
     * decoder is used using google web api. You should subscribe on I/O thread.
     *
     * @param locale     locale for address language
     * @param lat        latitude
     * @param lng        longitude
     * @param maxResults maximal number of results you are interested in
     * @return single that serves list of address based on location
     */
    public Single<List<Address>> reverseGeocode(Locale locale, double lat, double lng, int maxResults) {
        return ReverseGeocodeObservable.createObservable(ctx.getContext(), factory, locale, lat, lng, maxResults).singleOrError();
    }

    /**
     * Creates single that translates a street address or other description into a list of
     * possible addresses using included Geocoder class. You should subscribe on I/O thread.
     *
     * @param locationName a user-supplied description of a location
     * @param maxResults   max number of results you are interested in
     * @return single that serves list of address based on location name
     */
    public Single<List<Address>> geocode(String locationName, int maxResults) {
        return geocode(locationName, maxResults, null);
    }

    /**
     * Creates geocoder with default Locale.
     *
     * @see ReactiveLocationProvider#geocode(String, int, LatLngBounds, Locale)
     */
    public Single<List<Address>> geocode(String locationName, int maxResults, LatLngBounds bounds) {
        return geocode(locationName, maxResults, bounds, null);
    }

    /**
     * Creates single that translates a street address or other description into a list of
     * possible addresses using included Geocoder class. You should subscribe on I/O thread.
     * <p/>
     * You may specify a bounding box for the search results.
     *
     * @param locationName a user-supplied description of a location
     * @param maxResults   max number of results you are interested in
     * @param bounds       restricts the results to geographical bounds. May be null
     * @param locale       locale passed to geocoder
     * @return single that serves list of address based on location name
     */
    public Single<List<Address>> geocode(String locationName, int maxResults, LatLngBounds bounds, Locale locale) {
        return GeocodeObservable.createObservable(ctx.getContext(), factory, locationName, maxResults, bounds, locale).singleOrError();
    }

    /**
     * Creates completable that adds request and completes when the action is done.
     * <p/>
     * In case of any issue error is delivered.
     * <p/>
     * Every exception is delivered by {@link io.reactivex.CompletableObserver#onError(Throwable)}.
     *
     * @param geofenceTransitionPendingIntent pending intent to register on geofence transition
     * @param request                         list of request to add
     * @return completable that adds request
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public Completable addGeofences(PendingIntent geofenceTransitionPendingIntent, GeofencingRequest request) {
        return AddGeofenceObservableOnSubscribe.createObservable(ctx, factory, request, geofenceTransitionPendingIntent).ignoreElements();
    }

    /**
     * Completable that can be used to remove geofences from LocationClient.
     * <p/>
     * In case of any issue error is delivered.
     * <p/>
     * Every exception is delivered by {@link io.reactivex.CompletableObserver#onError(Throwable)}.
     *
     * @param pendingIntent key of registered geofences
     * @return completable that removed geofences
     */
    public Completable removeGeofences(PendingIntent pendingIntent) {
        return RemoveGeofenceObservableOnSubscribe.createObservable(ctx, factory, pendingIntent).ignoreElements();
    }

    /**
     * Completable that can be used to remove geofences from LocationClient.
     * <p/>
     * In case of any issue error is delivered.
     * <p/>
     * Every exception is delivered by {@link io.reactivex.CompletableObserver#onError(Throwable)}.
     *
     * @param requestIds geofences to remove
     * @return completable that removed geofences
     */
    public Completable removeGeofences(List<String> requestIds) {
        return RemoveGeofenceObservableOnSubscribe.createObservable(ctx, factory, requestIds).ignoreElements();
    }

    /**
     * Observable that can be used to observe activity provided by Actity Recognition mechanism.
     *
     * @param detectIntervalMiliseconds detecion interval
     * @return observable that provides activity recognition
     */
    public Observable<ActivityRecognitionResult> detectedActivity(int detectIntervalMiliseconds) {
        return ActivityUpdatesObservableOnSubscribe.createObservable(ctx, factory, detectIntervalMiliseconds);
    }

    /**
     * Returns single that can be used to check settings state for given location request.
     * To handle {@link com.google.android.gms.location.LocationSettingsResponse} you can use
     * {@link DataBufferObservable}.
     *
     * @param locationRequest location request
     * @return single that emits check result of location settings
     */
    public Single<LocationSettingsResponse> checkLocationSettings(final LocationSettingsRequest locationRequest) {
        return connectGoogleApiClient(LocationServices.API)
                .andThen(fromTask(LocationServices.getSettingsClient(ctx.getContext()).checkLocationSettings(locationRequest)));
    }

    /**
     * Returns single that fetches current place from Places API.
     * To handle {@link com.google.android.gms.location.places.PlaceLikelihoodBufferResponse} you can use
     * {@link DataBufferObservable}.
     *
     * @param placeFilter filter
     * @return single that emits current places buffer
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public Single<PlaceLikelihoodBufferResponse> getCurrentPlace(@Nullable final PlaceFilter placeFilter) {
        return connectGoogleApiClient(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .andThen(fromTask(Places.getPlaceDetectionClient(ctx.getContext()).getCurrentPlace(placeFilter)));
    }

    /**
     * Returns single that fetches a place from the Places API using the place ID.
     * To handle {@link com.google.android.gms.location.places.PlaceBufferResponse} you can use
     * {@link DataBufferObservable}.
     *
     * @param placeId id for place
     * @return single that emits places buffer
     */
    public Single<PlaceBufferResponse> getPlaceById(@Nullable final String placeId) {
        return connectGoogleApiClient(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .andThen(fromTask(Places.getGeoDataClient(ctx.getContext()).getPlaceById(placeId)));
    }

    /**
     * Returns single that fetches autocomplete predictions from Places API.
     * To handle {@link com.google.android.gms.location.places.AutocompletePredictionBufferResponse} you can use
     * {@link DataBufferObservable}.
     *
     * @param query  search query
     * @param bounds bounds where to fetch suggestions from
     * @param filter filter
     * @return single with suggestions buffer
     */
    public Single<AutocompletePredictionBufferResponse> getPlaceAutocompletePredictions(final String query, final LatLngBounds bounds, final
    AutocompleteFilter filter) {
        return connectGoogleApiClient(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .andThen(fromTask(Places.getGeoDataClient(ctx.getContext()).getAutocompletePredictions(query, bounds, filter)));
    }

    /**
     * Returns single that fetches photo metadata from the Places API using the place ID.
     * To handle {@link com.google.android.gms.location.places.PlacePhotoMetadataResponse} you can use
     * {@link DataBufferObservable}.
     *
     * @param placeId id for place
     * @return single that emits metadata buffer
     */
    public Single<PlacePhotoMetadataResponse> getPhotoMetadataById(final String placeId) {
        return connectGoogleApiClient(Places.PLACE_DETECTION_API, Places.GEO_DATA_API)
                .andThen(fromTask(Places.getGeoDataClient(ctx.getContext()).getPlacePhotos(placeId)));
    }

    /**
     * Completable that finishes after connection.
     * In case of error {@link pl.charmas.android.reactivelocation2.observables.GoogleAPIConnectionException} is emmited.
     * When connection to Google Play Services is suspended
     * {@link pl.charmas.android.reactivelocation2.observables.GoogleAPIConnectionSuspendedException} is emitted as error.
     * Do not disconnect from apis client manually - just unsubscribe.
     *
     * @param apis collection of apis to connect to
     * @return completable completes if successful connection
     */
    private Completable connectGoogleApiClient(Api... apis) {
        return GoogleAPIClientObservableOnSubscribe.create(ctx, factory, apis).ignoreElements();
    }

    /**
     * Util method that wraps {@link com.google.android.gms.tasks.Task} in Single.
     *
     * @param result pending result to wrap
     * @param <T>    parameter type of result
     * @return single that emits pending result
     */
    private <T extends Response> Single<T> fromTask(Task<T> result) {
        return Single.create(new TaskSingleOnSubscribe<>(result));
    }
}
