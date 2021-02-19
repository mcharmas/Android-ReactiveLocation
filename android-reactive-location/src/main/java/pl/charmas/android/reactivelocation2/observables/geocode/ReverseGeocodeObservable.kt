package pl.charmas.android.reactivelocation2.observables.geocode

import android.content.Context
import android.location.Address
import android.location.Geocoder
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe
import io.reactivex.schedulers.Schedulers
import pl.charmas.android.reactivelocation2.observables.MaybeEmitterWrapper
import pl.charmas.android.reactivelocation2.observables.MaybeFactory
import java.io.IOException
import java.util.Locale

class ReverseGeocodeObservable private constructor(
    private val ctx: Context,
    private val locale: Locale,
    private val apiKey: String,
    private val latitude: Double,
    private val longitude: Double,
    private val maxResults: Int
) : MaybeOnSubscribe<List<Address>> {

    override fun subscribe(emitter: MaybeEmitter<List<Address>>) {
        val geoCoder = Geocoder(ctx, locale)
        try {
            val addresses = geoCoder.getFromLocation(latitude, longitude, maxResults)
            if (!emitter.isDisposed) {
                emitter.onSuccess(addresses)
            }
        } catch (e: IOException) {
            // If it's a service not available error try a different approach using google web api
            if (!emitter.isDisposed) {
                Maybe.create(
                    FallbackReverseGeocodeObservable(
                        apiKey,
                        locale,
                        latitude,
                        longitude,
                        maxResults
                    )
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe(MaybeEmitterWrapper(emitter))
            }
        }
    }

    companion object {
        fun create(
            ctx: Context,
            apiKey: String,
            factory: MaybeFactory,
            locale: Locale,
            latitude: Double,
            longitude: Double,
            maxResults: Int
        ): Maybe<List<Address>> {
            return factory.create(
                ReverseGeocodeObservable(ctx, locale, apiKey, latitude, longitude, maxResults)
            )
        }
    }
}