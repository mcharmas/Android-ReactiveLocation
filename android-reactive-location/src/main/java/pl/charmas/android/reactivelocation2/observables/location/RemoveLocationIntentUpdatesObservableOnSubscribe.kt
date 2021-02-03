package pl.charmas.android.reactivelocation2.observables.location

import android.app.PendingIntent
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import pl.charmas.android.reactivelocation2.observables.BaseLocationMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.MaybeContext
import pl.charmas.android.reactivelocation2.observables.MaybeFactory

class RemoveLocationIntentUpdatesObservableOnSubscribe private constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    ctx: MaybeContext,
    private val intent: PendingIntent
) : BaseLocationMaybeOnSubscribe<Boolean>(ctx) {
    override fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: MaybeEmitter<in Boolean>
    ) {
        fusedLocationProviderClient.removeLocationUpdates(intent)
            .addOnCompleteListener {
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
            intent: PendingIntent
        ): Maybe<Boolean> {
            return factory.create(
                RemoveLocationIntentUpdatesObservableOnSubscribe(
                    fusedLocationProviderClient,
                    ctx,
                    intent
                )
            )
        }
    }
}