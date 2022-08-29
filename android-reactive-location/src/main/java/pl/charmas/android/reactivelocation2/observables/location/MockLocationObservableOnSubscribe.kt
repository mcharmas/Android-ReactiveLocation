package pl.charmas.android.reactivelocation2.observables.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe
import pl.charmas.android.reactivelocation2.observables.ObservableContext
import pl.charmas.android.reactivelocation2.observables.ObservableFactory

class MockLocationObservableOnSubscribe private constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    ctx: ObservableContext,
    private val locationObservable: Observable<Location>,
) : BaseLocationObservableOnSubscribe<Boolean>(ctx) {
    private var mockLocationSubscription: Disposable? = null

    @SuppressLint("MissingPermission")
    protected override fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: ObservableEmitter<in Boolean>,
    ) {
        // this throws SecurityException if permissions are bad or mock locations are not enabled,
        // which is passed to observer's onError by BaseObservable
        fusedLocationProviderClient.setMockMode(true)
            .addOnSuccessListener {
                startLocationMocking(emitter)
            }
            .addOnFailureListener { exception ->
                if (!emitter.isDisposed) {
                    emitter.onError(exception)
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationMocking(emitter: ObservableEmitter<in Boolean>) {
        mockLocationSubscription = locationObservable
            .subscribe({ location ->
                fusedLocationProviderClient.setMockLocation(location)
                    .addOnSuccessListener {
                        if (!emitter.isDisposed) {
                            emitter.onNext(true)
                        }
                    }
                    .addOnFailureListener { error ->
                        if (!emitter.isDisposed) {
                            emitter.onError(error)
                        }
                    }
            },
                { throwable ->
                    if (!emitter.isDisposed) {
                        emitter.onError(throwable)
                    }
                }) {
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }
    }

    @SuppressLint("MissingPermission")
    override fun onDisposed(locationClient: GoogleApiClient) {
        if (locationClient.isConnected) {
            try {
                fusedLocationProviderClient.setMockMode(false)
            } catch (e: SecurityException) {
                // if this happens then we couldn't have switched mock mode on in the first place,
                // and the observer's onError will already have been called
            }
        }
        if (mockLocationSubscription?.isDisposed != true) {
            mockLocationSubscription?.dispose()
        }
    }

    companion object {
        fun create(
            fusedLocationProviderClient: FusedLocationProviderClient,
            context: ObservableContext,
            factory: ObservableFactory,
            locationObservable: Observable<Location>,
        ): Observable<Boolean> {
            return factory.create(
                MockLocationObservableOnSubscribe(
                    fusedLocationProviderClient,
                    context,
                    locationObservable
                )
            )
        }
    }
}