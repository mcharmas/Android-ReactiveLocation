package pl.charmas.android.reactivelocation2.observables.geocode

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLngBounds
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.MaybeFactory
import pl.charmas.android.reactivelocation2.observables.ObservableFactory
import java.io.IOException
import java.util.Locale

class GeocodeMaybe private constructor(
    private val ctx: Context,
    private val locationName: String,
    private val maxResults: Int,
    private val bounds: LatLngBounds? = null,
    private val locale: Locale
) : MaybeOnSubscribe<List<Address>> {

    @Throws(Exception::class)
    override fun subscribe(emitter: MaybeEmitter<List<Address>>) {
        val geoCoder = createGeocoder()
        try {
            val result = getAddresses(geoCoder)
            if (!emitter.isDisposed) {
                emitter.onSuccess(result)
            }
        } catch (e: IOException) {
            if (!emitter.isDisposed) {
                emitter.onError(e)
            }
        }
    }

    @Throws(IOException::class)
    private fun getAddresses(geocoder: Geocoder): List<Address> {
        return if (bounds != null) {
            geocoder.getFromLocationName(
                locationName,
                maxResults,
                bounds.southwest.latitude,
                bounds.southwest.longitude,
                bounds.northeast.latitude,
                bounds.northeast.longitude
            )
        } else {
            geocoder.getFromLocationName(locationName, maxResults)
        }
    }

    private fun createGeocoder(): Geocoder {
        return Geocoder(ctx, locale)
    }

    companion object {
        fun create(
            ctx: Context,
            factory: MaybeFactory,
            locationName: String,
            maxResults: Int,
            bounds: LatLngBounds? = null,
            locale: Locale
        ): Maybe<List<Address>> {
            return factory.create(
                GeocodeMaybe(ctx, locationName, maxResults, bounds, locale)
            )
        }
    }

}