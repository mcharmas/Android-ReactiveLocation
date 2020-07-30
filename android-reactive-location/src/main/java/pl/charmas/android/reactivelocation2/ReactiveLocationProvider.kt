package pl.charmas.android.reactivelocation2

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Location
import androidx.annotation.IntRange
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import io.reactivex.Maybe
import io.reactivex.Observable
import pl.charmas.android.reactivelocation2.ext.fromSuccessFailureToMaybe
import pl.charmas.android.reactivelocation2.ext.toMaybe
import pl.charmas.android.reactivelocation2.observables.GoogleAPIClientMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.MaybeContext
import pl.charmas.android.reactivelocation2.observables.MaybeFactory
import pl.charmas.android.reactivelocation2.observables.ObservableContext
import pl.charmas.android.reactivelocation2.observables.ObservableFactory
import pl.charmas.android.reactivelocation2.observables.activity.ActivityUpdatesObservableOnSubscribe
import pl.charmas.android.reactivelocation2.observables.geocode.GeocodeMaybe
import pl.charmas.android.reactivelocation2.observables.geocode.ReverseGeocodeObservable
import pl.charmas.android.reactivelocation2.observables.geofence.AddGeofenceMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.geofence.RemoveGeofenceMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.location.AddLocationIntentUpdatesMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.location.LocationUpdatesObservableOnSubscribe
import pl.charmas.android.reactivelocation2.observables.location.MockLocationObservableOnSubscribe
import pl.charmas.android.reactivelocation2.observables.location.RemoveLocationIntentUpdatesObservableOnSubscribe
import java.util.Locale

/**
 * Factory of observables that can manipulate location
 * delivered by Google Play Services.
 */
class ReactiveLocationProvider
/**
 * Create location provider with default configuration or with given [ReactiveLocationProviderConfiguration]
 *
 * @param context           preferably application context
 * @param configuration configuration instance
 */
@JvmOverloads
constructor(
    val context: Context,
    private val apiKey: String,
    configuration: ReactiveLocationProviderConfiguration = ReactiveLocationProviderConfiguration.builder()
        .build()
) {
    private val ctxObservable: ObservableContext = ObservableContext(context, configuration)
    private val ctxMaybe: MaybeContext = MaybeContext(context, configuration)
    private val factoryObservable: ObservableFactory = ObservableFactory(ctxObservable)
    private val factoryMaybe: MaybeFactory = MaybeFactory(ctxMaybe)

    private val settingsClient = LocationServices.getSettingsClient(context)
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Creates observable that obtains last known location and than completes.
     * Delivered location is never null - when it is unavailable Observable completes without emitting
     * any value.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionException}
     * when there are trouble connecting with Google Play Services and other exceptions that
     * can be thrown on {@link com.google.android.gms.location.FusedLocationProviderApi#getLastLocation(com.google.android.gms.common.api.GoogleApiClient)}.
     * Everything is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @return observable that serves last know location
     */
    @get:RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    val lastKnownLocation: Maybe<Location>
        get() {
            return fusedLocationProviderClient
                .lastLocation
                .toMaybe()
        }

    /**
     * Creates observable that allows to observe infinite stream of location updates.
     * To stop the stream you have to unsubscribe from observable - location updates are
     * then disconnected.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionException}
     * when there are trouble connecting with Google Play Services and other exceptions that
     * can be thrown on {@link com.google.android.gms.location.FusedLocationProviderApi#requestLocationUpdates(com.google.android.gms.common.api.GoogleApiClient, com.google.android.gms.location.LocationRequest, com.google.android.gms.location.LocationListener)}.
     * Everything is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @param locationRequest request object with info about what kind of location you need
     * @return observable that serves infinite stream of location updates
     */
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    fun getUpdatedLocation(locationRequest: LocationRequest): Observable<Location> {
        return LocationUpdatesObservableOnSubscribe.createObservable(
            fusedLocationProviderClient,
            ctxObservable,
            factoryObservable,
            locationRequest
        )
    }

    /**
     * Returns an observable which activates mock location mode when subscribed to, using the
     * supplied observable as a source of mock locations. Mock locations will replace normal
     * location information for all users of the FusedLocationProvider API on the device while this
     * observable is subscribed to.
     * <p/>
     * To use this method, mock locations must be enabled in developer options and your application
     * must hold the android.permission.ACCESS_MOCK_LOCATION permission, or a {@link java.lang.SecurityException}
     * will be thrown.
     * <p/>
     * All statuses that are not successful will be reported as {@link pl.charmas.android.reactivelocation2.observables.StatusException}.
     * <p/>
     * Every exception is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @param sourceLocationObservable observable that emits {@link android.location.Location} instances suitable to use as mock locations
     * @return observable that emits {@link com.google.android.gms.common.api.Status}
     */
    @RequiresPermission(allOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_MOCK_LOCATION"])
    fun mockLocation(sourceLocationObservable: Observable<Location>): Observable<Boolean> {
        return MockLocationObservableOnSubscribe.create(
            fusedLocationProviderClient,
            ctxObservable,
            factoryObservable,
            sourceLocationObservable
        )
    }

    /**
     * Creates an observable that adds a {@link android.app.PendingIntent} as a location listener.
     * <p/>
     * This invokes {@link com.google.android.gms.location.FusedLocationProviderApi#requestLocationUpdates(com.google.android.gms.common.api.GoogleApiClient, com.google.android.gms.location.LocationRequest, android.app.PendingIntent)}.
     * <p/>
     * When location updates are no longer required, a call to {@link #removeLocationUpdates(android.app.PendingIntent)}
     * should be made.
     * <p/>
     * In case of unsuccessful status {@link pl.charmas.android.reactivelocation2.observables.StatusException} is delivered.
     *
     * @param locationRequest request object with info about what kind of location you need
     * @param intent          PendingIntent that will be called with location updates
     * @return observable that adds the request and PendingIntent
     */
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    fun requestLocationUpdates(
        locationRequest: LocationRequest,
        intent: PendingIntent
    ): Maybe<Boolean> {
        return AddLocationIntentUpdatesMaybeOnSubscribe.create(
            fusedLocationProviderClient,
            ctxMaybe,
            factoryMaybe,
            locationRequest,
            intent
        )
    }

    /**
     * Observable that can be used to remove {@link android.app.PendingIntent} location updates.
     * <p/>
     * In case of unsuccessful status {@link pl.charmas.android.reactivelocation2.observables.StatusException} is delivered.
     *
     * @param intent PendingIntent to remove location updates for
     * @return observable that removes the PendingIntent
     */
    fun removeLocationUpdates(intent: PendingIntent): Maybe<Boolean> {
        return RemoveLocationIntentUpdatesObservableOnSubscribe.create(
            fusedLocationProviderClient,
            ctxMaybe,
            factoryMaybe,
            intent
        )
    }

    /**
     * Creates observable that translates latitude and longitude to list of possible addresses using
     * included Geocoder class. In case geocoder fails with IOException("Service not Available") fallback
     * decoder is used using google web api. You should subscribe for this observable on I/O thread.
     * The stream finishes after address list is available.
     *
     * @param locale     locale for address language
     * @param lat        latitude
     * @param lng        longitude
     * @param maxResults maximal number of results you are interested in
     * @return observable that serves list of address based on location
     */
    fun getReverseGeocodeMaybe(
        locale: Locale = Locale.getDefault(),
        lat: Double,
        lng: Double,
        maxResults: Int
    ): Maybe<List<Address>> {
        return ReverseGeocodeObservable.create(
            ctxObservable.context,
            apiKey,
            factoryMaybe,
            locale,
            lat,
            lng,
            maxResults
        )
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
     * @param maxResults   max number of results you are interested in
     * @param bounds       restricts the results to geographical bounds. May be null
     * @param locale       locale passed to geocoder
     * @return observable that serves list of address based on location name
     */
    fun getGeocodeObservable(
        locationName: String,
        maxResults: Int,
        bounds: LatLngBounds? = null,
        locale: Locale? = null
    ): Maybe<List<Address>> {
        return GeocodeMaybe.create(
            ctxObservable.context,
            factoryMaybe,
            locationName,
            maxResults,
            bounds,
            locale ?: Locale.getDefault()
        )
    }

    /**
     * Creates observable that adds request and completes when the action is done.
     * <p/>
     * Observable can report {@link pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionException}
     * when there are trouble connecting with Google Play Services.
     * <p/>
     * In case of unsuccessful status {@link pl.charmas.android.reactivelocation2.observables.StatusException} is delivered.
     * <p/>
     * Other exceptions will be reported that can be thrown on {@link com.google.android.gms.location.GeofencingApi#addGeofences(com.google.android.gms.common.api.GoogleApiClient, com.google.android.gms.location.GeofencingRequest, android.app.PendingIntent)}
     *
     * @param geofenceTransitionPendingIntent pending intent to register on geofence transition
     * @param request                         list of request to add
     * @return observable that adds request
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    fun addGeofences(
        geofenceTransitionPendingIntent: PendingIntent,
        request: GeofencingRequest
    ): Maybe<Boolean> {
        return AddGeofenceMaybeOnSubscribe.createMaybe(
            geofencingClient,
            ctxMaybe,
            factoryMaybe,
            request,
            geofenceTransitionPendingIntent
        )
    }

    /**
     * Observable that can be used to remove geofences from LocationClient.
     * <p/>
     * In case of unsuccessful status {@link pl.charmas.android.reactivelocation2.observables.StatusException} is delivered.
     * <p/>
     * Other exceptions will be reported that can be thrown on {@link com.google.android.gms.location.GeofencingApi#removeGeofences(com.google.android.gms.common.api.GoogleApiClient, android.app.PendingIntent)}.
     * <p/>
     * Every exception is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @param pendingIntent key of registered geofences
     * @return observable that removed geofences
     */
    fun removeGeofences(pendingIntent: PendingIntent): Maybe<Boolean> {
        return RemoveGeofenceMaybeOnSubscribe.createMaybe(
            ctxMaybe,
            factoryMaybe,
            pendingIntent, geofencingClient
        )
    }

    /**
     * Observable that can be used to remove geofences from LocationClient.
     * <p/>
     * In case of unsuccessful status {@link pl.charmas.android.reactivelocation2.observables.StatusException} is delivered.
     * <p/>
     * Other exceptions will be reported that can be thrown on {@link com.google.android.gms.location.GeofencingApi#removeGeofences(com.google.android.gms.common.api.GoogleApiClient, java.util.List)}.
     * <p/>
     * Every exception is delivered by {@link io.reactivex.Observer#onError(Throwable)}.
     *
     * @param requestIds geofences to remove
     * @return observable that removed geofences
     */
    fun removeGeofences(requestIds: List<String>): Maybe<Boolean> {
        return RemoveGeofenceMaybeOnSubscribe.createMaybe(
            ctxMaybe,
            factoryMaybe,
            requestIds,
            geofencingClient
        )
    }

    /**
     * Observable that can be used to observe activity provided by Activity Recognition mechanism.
     *
     * @param detectIntervalMiliseconds detecion interval
     * @return observable that provides activity recognition
     */
    fun getDetectedActivity(detectIntervalMiliseconds: Int): Observable<ActivityRecognitionResult> {
        return ActivityUpdatesObservableOnSubscribe.createObservable(
            ctxObservable,
            factoryObservable,
            detectIntervalMiliseconds
        )
    }

    /**
     * Observable that can be used to check settings state for given location request.
     *
     * @param locationRequest location request
     * @return observable that emits check result of location settings
     * @see com.google.android.gms.location.SettingsApi
     */
    fun checkLocationSettings(
        locationRequest: LocationSettingsRequest
    ): Maybe<LocationSettingsResponse> {
        return settingsClient
            .checkLocationSettings(locationRequest)
            .fromSuccessFailureToMaybe()
    }

    /**
     * Returns observable that fetches current place from Places API. To flatmap and auto release
     * buffer to {@link com.google.android.gms.location.places.PlaceLikelihood} observable use
     * {@link DataBufferObservable}.
     *
     * @param placeFilter filter
     * @return observable that emits current places buffer and completes
     */
    @RequiresPermission(allOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_WIFI_STATE"])
    fun getCurrentPlace(placeFilter: FindCurrentPlaceRequest): Maybe<FindCurrentPlaceResponse> {
        return Places.createClient(
            ctxObservable.context
        ).findCurrentPlace(placeFilter)
            .toMaybe()
    }

    /**
     * Returns observable that fetches a place from the Places API using the place ID.
     *
     * @param placeId id for place
     * @return observable that emits places buffer and completes
     *
     * @deprecated use {@link ReactiveLocationProvider#getPlaceCompatById(java.lang.String)}
     */
    fun getPlaceById(placeId: String): Maybe<FetchPlaceResponse> {
        return Places.createClient(ctxObservable.context)
            .fetchPlace(
                FetchPlaceRequest.builder(
                    placeId,
                    listOf(Place.Field.ID)
                ).build()
            )
            .toMaybe()
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
     *
     * @deprecated use {@link ReactiveLocationProvider#getPlaceCompatAutocompletePredictions(java.lang.String, com.google.android.gms.maps.model.LatLngBounds, com.google.android.libraries.places.compat.AutocompleteFilter)}
     */
    fun getPlaceAutocompletePredictions(result: FindAutocompletePredictionsRequest): Maybe<List<AutocompletePrediction>> {
        return Places.createClient(ctxObservable.context)
            .findAutocompletePredictions(result)
            .toMaybe()
            .map { obj: FindAutocompletePredictionsResponse -> obj.autocompletePredictions }
    }

    /**
     * Returns observable that fetches photo metadata from the Places API using the place ID.
     *
     * @param placeId id for place
     * @return observable that emits metadata buffer and completes
     */
    fun getPhotoMetadataById(
        placeId: String,
        @IntRange(from = 0L) height: Int,
        @IntRange(from = 0L) width: Int
    ): Maybe<Bitmap> {
        return Places.createClient(ctxObservable.context)
            .fetchPlace(
                FetchPlaceRequest.builder(
                    placeId,
                    listOf(Place.Field.PHOTO_METADATAS)
                ).build()
            )
            .toMaybe()
            .flatMap { res ->
                val photoMetadata = res.place.photoMetadatas?.firstOrNull()
                if (photoMetadata == null) {
                    Maybe.empty<Bitmap>()
                } else {
                    Places.createClient(ctxObservable.context)
                        .fetchPhoto(
                            FetchPhotoRequest.builder(
                                PhotoMetadata.builder(placeId)
                                    .setHeight(height)
                                    .setWidth(width)
                                    .setAttributions(photoMetadata.attributions)
                                    .build()
                            )
                                .build()
                        )
                        .toMaybe()
                        .map { it.bitmap }
                }
            }
    }

    /**
     * Returns observable that fetches a placePhotoMetadata from the Places API using the place placePhotoMetadata metadata.
     * Use after fetching the place placePhotoMetadata metadata with [ReactiveLocationProvider.getPhotoMetadataById]
     *
     * @param placePhotoMetadata the place photo meta data
     * @return observable that emits the photo result and completes
     */
    fun getPhotoForMetadata(placePhotoMetadata: PhotoMetadata): Maybe<Bitmap> {
        return Places.createClient(ctxObservable.context)
            .fetchPhoto(
                FetchPhotoRequest.builder(placePhotoMetadata)
                    .build()
            )
            .toMaybe()
            .map { it.bitmap }
    }

    /**
     * Observable that emits {@link com.google.android.gms.common.api.GoogleApiClient} object after connection.
     * In case of error {@link pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionException} is emmited.
     * When connection to Google Play Services is suspended {@link pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionSuspendedException}
     * is emitted as error.
     * Do not disconnect from apis client manually - just unsubscribe.
     *
     * @param apis collection of apis to connect to
     * @return observable that emits apis client after successful connection
     */
    fun <T : Api.ApiOptions.NotRequiredOptions> getGoogleApiClientMaybe(vararg apis: Api<T>): Maybe<GoogleApiClient> {
        return GoogleAPIClientMaybeOnSubscribe.create(ctxMaybe, factoryMaybe, *apis)
    }
}
