package pl.charmas.android.reactivelocation2

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.Address
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
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
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import pl.charmas.android.reactivelocation2.ext.calldownOrEmpty
import pl.charmas.android.reactivelocation2.ext.onSuccessOrComplete
import pl.charmas.android.reactivelocation2.ext.reduceRightDefault
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
import java.util.concurrent.TimeUnit

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
constructor(
    val context: Context,
    private val apiKey: String,
    configuration: ReactiveLocationProviderConfiguration = ReactiveLocationProviderConfiguration.builder()
        .build(),
) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val ctxObservable: ObservableContext = ObservableContext(context, configuration)
    private val ctxMaybe: MaybeContext = MaybeContext(context, configuration)
    private val factoryObservable: ObservableFactory = ObservableFactory(ctxObservable)
    private val factoryMaybe: MaybeFactory = MaybeFactory(ctxMaybe)

    private val settingsClient = LocationServices.getSettingsClient(context)
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isGpsEnabled(): Single<Boolean> {
        return isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isProviderEnabled(provider: String): Single<Boolean> {
        return Single.create<Boolean> { subscriber ->
            if (subscriber.isDisposed) {
                return@create
            }
            subscriber.onSuccess(locationManager.isProviderEnabled(provider))
        }
    }

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
            return fusedLocationProviderClient.lastLocation
                .toMaybe()
        }

    /**
     * Creates observable that obtains last known location from BestProvider and than completes.
     * */
    @get:RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    val locationBestProvider: Maybe<Location>
        get() {
            return Maybe.create<String> { emitter ->
                val bestProvider = locationManager.getBestProvider(
                    Criteria(), false
                )

                if (!emitter.isDisposed)
                    emitter.onSuccessOrComplete(bestProvider)

            }
                .flatMap { provider ->
                    getLastKnownLocationFromProvider(provider)
                }
        }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocationFromProvider(provider: String): Maybe<Location> {
        return Maybe.create<Location> { emitter ->
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    Log.d(TAG, "onLocationChanged: $location")
                    emitter.onSuccessOrComplete(location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    Log.d(TAG, "onStatusChanged: $provider status = $status")
                }

                override fun onProviderEnabled(provider: String?) {
                    Log.d(TAG, "onProviderEnabled: $provider")
                }

                override fun onProviderDisabled(provider: String?) {
                    Log.d(TAG, "onProviderDisabled: $provider")
                    if (!emitter.isDisposed)
                        emitter.onError(RuntimeException("onProviderDisabled: $provider"))
                }
            }

            emitter.setDisposable(Disposables.fromAction {
                locationManager.removeUpdates(locationListener)
            })

            locationManager.requestLocationUpdates(
                provider,
                1000,
                1000f,
                locationListener,
                null
            )

            val location = locationManager.getLastKnownLocation(provider)

            if (location != null) {
                emitter.onSuccess(location)
            }
        }
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdatesFromProvider(provider: String): Observable<Location> {
        return Observable.create<Location> { emitter ->
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    Log.d(TAG, "onLocationChanged: $location")
                    if (location != null) {
                        if (!emitter.isDisposed) {
                            emitter.onNext(location)
                        }
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    Log.d(TAG, "onStatusChanged: $provider status = $status")
                }

                override fun onProviderEnabled(provider: String?) {
                    Log.d(TAG, "onProviderEnabled: $provider")
                }

                override fun onProviderDisabled(provider: String?) {
                    Log.d(TAG, "onProviderDisabled: $provider")
                }
            }

            emitter.setDisposable(Disposables.fromAction {
                locationManager.removeUpdates(locationListener)
            })

            locationManager.requestLocationUpdates(
                provider,
                1000,
                1000f,
                locationListener,
                null
            )

            val location = locationManager.getLastKnownLocation(provider)

            if (location != null) {
                if (!emitter.isDisposed) {
                    emitter.onNext(location)
                }
            }
        }
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Creates observable that obtains best last known location from Providers and than completes.
     * */
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    fun getLastKnownLocationFromAllProviders(
        time: Int,
        timeUnit: TimeUnit,
        scheduler: Scheduler,
    ): Maybe<Location> {
        return Maybe.create<List<String>> { emitter ->
            emitter.onSuccessOrComplete(locationManager.allProviders)
        }.flatMap { allProviders ->
            Observable.fromIterable(allProviders)
                .flatMapMaybe {
                    getLastKnownLocationFromProvider(it)
                        .onErrorResumeNext { throwable: Throwable -> Maybe.empty() }
                        .calldownOrEmpty(time, timeUnit, scheduler)
                }
                .toList()
                .flatMapMaybe { locations ->
                    val bestLocation =
                        locations.reduceRightDefault(locations.firstOrNull()) { first, second ->
                            when {
                                first == null -> second
                                second == null -> first
                                second.accuracy > first.accuracy -> first
                                else -> second
                            }
                        }
                    if (bestLocation == null) {
                        Maybe.empty<Location>()
                    } else {
                        Maybe.just(bestLocation)
                    }
                }
        }
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
        intent: PendingIntent,
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
        maxResults: Int,
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
        locale: Locale? = null,
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
        request: GeofencingRequest,
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
        locationRequest: LocationSettingsRequest,
    ): Maybe<LocationSettingsResponse> {
        return settingsClient
            .checkLocationSettings(locationRequest)
            .toMaybe()
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
    private fun <T : Api.ApiOptions.NotRequiredOptions> getGoogleApiClientMaybe(vararg apis: Api<T>): Maybe<GoogleApiClient> {
        return GoogleAPIClientMaybeOnSubscribe.create(ctxMaybe, factoryMaybe, *apis)
    }

    companion object {
        const val TAG: String = "ReactiveLocationProvide"
    }
}
