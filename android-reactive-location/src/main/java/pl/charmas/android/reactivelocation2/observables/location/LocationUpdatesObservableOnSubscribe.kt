package pl.charmas.android.reactivelocation2.observables.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe
import pl.charmas.android.reactivelocation2.observables.ObservableContext
import pl.charmas.android.reactivelocation2.observables.ObservableFactory
import java.lang.ref.WeakReference

class LocationUpdatesObservableOnSubscribe private constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    ctx: ObservableContext,
    private val locationRequest: LocationRequest
) : BaseLocationObservableOnSubscribe<Location>(ctx) {
    private var listener: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: ObservableEmitter<in Location>
    ) {
        listener = LocationUpdatesLocationListener(emitter)
        fusedLocationProviderClient
            .requestLocationUpdates(
                locationRequest,
                listener,
                null
            )
    }

    override fun onDisposed(locationClient: GoogleApiClient) {
        if (locationClient.isConnected && listener != null) {
            fusedLocationProviderClient.removeLocationUpdates(listener)
        }
    }

    private class LocationUpdatesLocationListener internal constructor(emitter: ObservableEmitter<in Location>) :
        LocationCallback() {

        private val weakRef: WeakReference<ObservableEmitter<in Location>> = WeakReference(emitter)

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            val observer = weakRef.get()
            val locations = locationResult?.locations ?: emptyList()
            for (item in locations) {
                if (observer != null && !observer.isDisposed && item != null) {
                    observer.onNext(item)
                }
            }

            locationResult?.lastLocation?.let {item->
                if (observer != null && !observer.isDisposed) {
                    observer.onNext(item)
                }
            }
        }
    }

    companion object {
        fun createObservable(
            fusedLocationProviderClient: FusedLocationProviderClient,
            ctx: ObservableContext,
            factory: ObservableFactory,
            locationRequest: LocationRequest
        ): Observable<Location> {
            var observable =
                factory.create(
                    LocationUpdatesObservableOnSubscribe(fusedLocationProviderClient, ctx, locationRequest)
                )
            val requestedNumberOfUpdates = locationRequest.numUpdates
            if (requestedNumberOfUpdates > 0 && requestedNumberOfUpdates < Int.MAX_VALUE) {
                observable = observable.take(requestedNumberOfUpdates.toLong())
            }
            return observable
        }
    }
}