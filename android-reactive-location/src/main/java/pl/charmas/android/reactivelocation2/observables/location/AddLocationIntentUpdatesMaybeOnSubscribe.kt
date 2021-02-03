package pl.charmas.android.reactivelocation2.observables.location

import android.app.PendingIntent
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import pl.charmas.android.reactivelocation2.observables.BaseLocationMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.MaybeContext
import pl.charmas.android.reactivelocation2.observables.MaybeFactory

class AddLocationIntentUpdatesMaybeOnSubscribe private constructor(
    ctx: MaybeContext,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRequest: LocationRequest,
    private val intent: PendingIntent
) : BaseLocationMaybeOnSubscribe<Boolean>(ctx) {

     @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
     override fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: MaybeEmitter<in Boolean>
    ) {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, intent)
            .addOnSuccessListener {
                if (!emitter.isDisposed) {
                    emitter.onSuccess(true)
                }
            }
            .addOnFailureListener { error ->
                if (!emitter.isDisposed) {
                    emitter.onError(error)
                }
            }
    }

    companion object {
        fun create(
            fusedLocationProviderClient: FusedLocationProviderClient,
            ctx: MaybeContext,
            factory: MaybeFactory,
            locationRequest: LocationRequest,
            intent: PendingIntent
        ): Maybe<Boolean> {
            return factory.create(
                AddLocationIntentUpdatesMaybeOnSubscribe(
                    ctx,
                    fusedLocationProviderClient,
                    locationRequest,
                    intent
                )
            )
        }
    }
}