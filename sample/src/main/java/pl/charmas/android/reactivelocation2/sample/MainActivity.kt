package pl.charmas.android.reactivelocation2.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.LocationSettingsStatusCodes
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import pl.charmas.android.reactivelocation2.ReactiveLocationProviderConfiguration.Companion.builder
import pl.charmas.android.reactivelocation2.sample.ext.addTo
import pl.charmas.android.reactivelocation2.sample.ext.addressToString
import pl.charmas.android.reactivelocation2.sample.ext.text
import pl.charmas.android.reactivelocation2.sample.ext.toast
import pl.charmas.android.reactivelocation2.sample.utils.DetectedActivityToString
import pl.charmas.android.reactivelocation2.sample.utils.DisplayTextOnViewAction
import pl.charmas.android.reactivelocation2.sample.utils.ToMostProbableActivity
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity() {

    private val lastKnownLocationView: TextView by lazy {
        findViewById<TextView>(R.id.last_known_location_view)
    }

    private val lastBestProviderLocationView: TextView by lazy {
        findViewById<TextView>(R.id.last_best_provider_location_view)
    }

    private val locationAllProvidersView: TextView by lazy {
        findViewById<TextView>(R.id.locationAllProviders)
    }
    private val locationNetworkProviderView: TextView by lazy {
        findViewById<TextView>(R.id.locationNetworkProviderView)
    }

    private val locationNetworkProviderUpdatesView: TextView by lazy {
        findViewById<TextView>(R.id.locationNetworkProviderUpdatesView)
    }

    private val updatableLocationView: TextView by lazy {
        findViewById<TextView>(R.id.updated_location_view)
    }

    private val addressLocationView: TextView by lazy {
        findViewById<TextView>(R.id.address_for_location_view)
    }

    private val currentActivityView: TextView by lazy {
        findViewById<TextView>(R.id.activity_recent_view)
    }

    private val rxLocationProvider: ReactiveLocationProvider by lazy {
        ReactiveLocationProvider(
            applicationContext,
            getString(R.string.API_KEY),
            builder()
                .setRetryOnConnectionSuspended(true)
                .build()
        )
    }

    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setInterval(TimeUnit.SECONDS.toMillis(1))

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onLocationPermissionGranted() {
        lastKnownLocationSubscribe()
        locationBestProviderSubscribe()
        locationAllProvidersMaybeSubscribe()
        lastLocationNetworkProviderMaybeSubscribe()
        updatedLocationNetworkProviderSubscribe()
        updatedLocationSubscribe()
        addressObservableSubscribe()
        activityObservableSubscribe()
    }

    private fun activityObservableSubscribe() {
        rxLocationProvider
            .getDetectedActivity(50)
            .observeOn(AndroidSchedulers.mainThread())
            .map(ToMostProbableActivity())
            .map(DetectedActivityToString())
            .subscribe(
                DisplayTextOnViewAction(currentActivityView),
                ErrorHandler("activityObservable")
            )
            .addTo(disposables)
    }

    @SuppressLint("MissingPermission")
    private fun addressObservableSubscribe() {
        rxLocationProvider.getUpdatedLocation(locationRequest)
            .flatMapMaybe { location ->
                rxLocationProvider.getReverseGeocodeMaybe(
                    Locale.getDefault(),
                    location.latitude,
                    location.longitude,
                    1
                )
            }
            .map { addresses ->
                val address = addresses.firstOrNull()
                address?.addressToString() ?: ""
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                DisplayTextOnViewAction(addressLocationView),
                ErrorHandler("addressObservable")
            )
            .addTo(disposables)
    }

    @SuppressLint("MissingPermission")
    private fun updatedLocationSubscribe() {
        var count = 0

        rxLocationProvider
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(true) //Refrence: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                    .build()
            )
            .doOnSuccess { locationSettingsResponse: LocationSettingsResponse ->
                Log.d(
                    "MainActivity",
                    "getLocationSettingsStates isGpsUsable = " + locationSettingsResponse.locationSettingsStates
                        .isGpsUsable
                )
            }
            .doOnError { throwable: Throwable ->
                val statusCode = (throwable as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        val rae = throwable as ResolvableApiException
                        rae.startResolutionForResult(
                            this@MainActivity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sie: SendIntentException) {
                        Log.i(
                            TAG,
                            "PendingIntent unable to execute request."
                        )
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)
                        toast(errorMessage)
                    }
                }
            }
            .flatMapObservable { rxLocationProvider.getUpdatedLocation(locationRequest) }
            .observeOn(AndroidSchedulers.mainThread())
            .map { location -> location.text() + " " + count++ }
            .subscribe(
                DisplayTextOnViewAction(updatableLocationView),
                Consumer { throwable ->
                    if (throwable is ResolvableApiException) {
                        return@Consumer
                    }

                    toast("Error occurred.")
                    Log.d("MainActivity", "Error occurred locationUpdatesObservable:", throwable)
                }
            )
            .addTo(disposables)
    }

    private fun lastKnownLocationSubscribe() {
        Maybe.timer(100, TimeUnit.MILLISECONDS, Schedulers.io())
            .flatMap { rxLocationProvider.lastKnownLocation }
            .subscribeOn(Schedulers.io())
            .map { it.text() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                DisplayTextOnViewAction(lastKnownLocationView),
                ErrorHandler("lastKnownLocationMaybe")
            )
            .addTo(disposables)
    }

    private fun locationBestProviderSubscribe() {
        Maybe.timer(100, TimeUnit.MILLISECONDS, Schedulers.io())
            .flatMap { rxLocationProvider.locationBestProvider }
            .subscribeOn(Schedulers.io())
            .map { it.text() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                DisplayTextOnViewAction(lastBestProviderLocationView),
                ErrorHandler("locationBestProviderMaybe")
            )
            .addTo(disposables)
    }

    @SuppressLint("MissingPermission")
    private fun locationAllProvidersMaybeSubscribe() {
        Maybe.timer(100, TimeUnit.MILLISECONDS, Schedulers.io())
            .flatMap { rxLocationProvider.getLastKnownLocationFromAllProviders(1, TimeUnit.SECONDS, Schedulers.io()) }
            .subscribeOn(Schedulers.io())
            .map { it.text() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                DisplayTextOnViewAction(locationAllProvidersView),
                ErrorHandler("locationBestProviderMaybe")
            )
            .addTo(disposables)
    }

    private fun lastLocationNetworkProviderMaybeSubscribe() {
        Maybe.timer(100, TimeUnit.MILLISECONDS, Schedulers.io())
            .flatMap {
                rxLocationProvider.getLastKnownLocationFromProvider(LocationManager.NETWORK_PROVIDER)
            }
            .observeOn(Schedulers.io())
            .map { it.text() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                DisplayTextOnViewAction(locationNetworkProviderView),
                ErrorHandler("locationNetworkProviderMaybe")
            )
            .addTo(disposables)
    }

    private fun updatedLocationNetworkProviderSubscribe() {
        var count = 0

        Maybe.timer(100, TimeUnit.MILLISECONDS, Schedulers.io())
            .flatMapObservable {
                rxLocationProvider.getLocationUpdatesFromProvider(LocationManager.NETWORK_PROVIDER)
            }
            .observeOn(Schedulers.io())
            .map { location -> location.text() + " " + count++ }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                DisplayTextOnViewAction(locationNetworkProviderUpdatesView),
                ErrorHandler("updatedLocationNetworkProviderSubscribe")
            )
            .addTo(disposables)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Geofencing")
            .setOnMenuItemClickListener {
                startActivity(Intent(this@MainActivity, GeofenceActivity::class.java))
                true
            }
        menu.add("Places")
            .setOnMenuItemClickListener {
                if (getString(R.string.API_KEY).isEmpty()) {
                    toast("First you need to configure your API Key - see README.md")
                } else {
                    startActivity(Intent(this@MainActivity, PlacesActivity::class.java))
                }
                true
            }
        menu.add("Mock Locations")
            .setOnMenuItemClickListener {
                startActivity(Intent(this@MainActivity, MockLocationsActivity::class.java))
                true
            }
        return true
    }

    private inner class ErrorHandler(val source: String) : Consumer<Throwable> {
        override fun accept(throwable: Throwable) {
            toast("Error occurred.")
            Log.d("MainActivity", "Error occurred $source:", throwable)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val states = LocationSettingsStates.fromIntent(data) //intent);
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK ->                         // All required changes were successfully made
                    Log.d(TAG, "User enabled location")
                Activity.RESULT_CANCELED ->                         // The user was asked to change settings, but chose not to
                    Log.d(
                        TAG,
                        "User Cancelled enabling location"
                    )
                else -> {
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0
        private const val TAG = "MainActivity"
    }
}
