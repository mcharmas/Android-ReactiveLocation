package pl.charmas.android.reactivelocation2.observables.geocode

import android.location.Address
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import java.util.Locale

internal class FallbackReverseGeocodeObservable(
    private val apiKey: String,
    private val locale: Locale,
    private val latitude: Double,
    private val longitude: Double,
    private val maxResults: Int
) : MaybeOnSubscribe<List<Address>> {

    override fun subscribe(emitter: MaybeEmitter<List<Address>?>) {
        try {
            val addresses = alternativeReverseGeocodeQuery()
            if (!emitter.isDisposed) {
                emitter.onSuccess(addresses)
            }
        } catch (exception: Exception) {
            if (!emitter.isDisposed) {
                emitter.onError(exception)
            }
        }
    }

    /**
     * This function fetches a list of addresses for the set latitude, longitude and maxResults properties from the
     * Google Geocode API (http://maps.googleapis.com/maps/api/geocode).
     *
     * @return List of addresses
     * @throws IOException   In case of network problems
     * @throws JSONException In case of problems while parsing the json response from google geocode API servers
     */
    @Throws(IOException::class, JSONException::class)
    private fun alternativeReverseGeocodeQuery(): List<Address> {
        val url = URL(
            "http://maps.googleapis.com/maps/api/geocode/json?"
                + "latlng=$latitude,${longitude}&sensor=true&key=$apiKey&language=${locale.language}"
        )
        val urlConnection = url.openConnection() as HttpURLConnection
        val stringBuilder = StringBuilder()
        val outResult = mutableListOf<Address>()
        try {
            val reader =
                BufferedReader(InputStreamReader(urlConnection.inputStream, Charsets.UTF_8))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            // Root json response object
            val jsonRootObject = JSONObject(stringBuilder.toString())

            // No results status
            if ("ZERO_RESULTS".equals(jsonRootObject.getString("status"), ignoreCase = true)) {
                return emptyList()
            }

            // Other non-OK responses status
            if (!"OK".equals(jsonRootObject.getString("status"), ignoreCase = true)) {
                throw RuntimeException("Wrong API response")
            }

            // Process results
            val results = jsonRootObject.getJSONArray("results")
            var i = 0
            while (i < results.length() && i < maxResults) {
                val address = Address(Locale.getDefault())
                var addressLineString = ""
                val sourceResult = results.getJSONObject(i)
                val addressComponents = sourceResult.getJSONArray("address_components")

                // Assemble address by various components
                for (ac in 0 until addressComponents.length()) {
                    val jsonObject = addressComponents.getJSONObject(ac)
                    val longNameVal = jsonObject.getString("long_name")
                    val shortNameVal = jsonObject.getString("short_name")
                    val acTypes = jsonObject.getJSONArray("types")
                    val acType = acTypes.getString(0)
                    if (longNameVal.isNotEmpty()) {
                        if (acType.equals("street_number", ignoreCase = true)) {
                            if (addressLineString.isEmpty()) {
                                addressLineString = longNameVal
                            } else {
                                addressLineString += " $longNameVal"
                            }
                        } else if (acType.equals("route", ignoreCase = true)) {
                            addressLineString = if (addressLineString.isEmpty()) {
                                longNameVal
                            } else {
                                "$longNameVal $addressLineString"
                            }
                        } else if (acType.equals("sublocality", ignoreCase = true)) {
                            address.subLocality = longNameVal
                        } else if (acType.equals("locality", ignoreCase = true)) {
                            address.locality = longNameVal
                        } else if (acType.equals(
                                "administrative_area_level_2",
                                ignoreCase = true
                            )
                        ) {
                            address.subAdminArea = longNameVal
                        } else if (acType.equals(
                                "administrative_area_level_1",
                                ignoreCase = true
                            )
                        ) {
                            address.adminArea = longNameVal
                        } else if (acType.equals("country", ignoreCase = true)) {
                            address.countryName = longNameVal
                            address.countryCode = shortNameVal
                        } else if (acType.equals("postal_code", ignoreCase = true)) {
                            address.postalCode = longNameVal
                        }
                    }
                }

                // Try to get the already formatted address
                val formattedAddress = sourceResult.getString("formatted_address")
                if (formattedAddress.isNotEmpty()) {
                    val formattedAddressLines =
                        formattedAddress.split(",".toRegex()).toTypedArray()
                    for (ia in formattedAddressLines.indices) {
                        address.setAddressLine(
                            ia,
                            formattedAddressLines[ia].trim { it <= ' ' }
                        )
                    }
                } else if (addressLineString.isNotEmpty()) {
                    // If that fails use our manually assembled formatted address
                    address.setAddressLine(0, addressLineString)
                }

                // Finally add address to resulting set
                outResult.add(address)
                i++
            }
        } finally {
            urlConnection.disconnect()
        }
        return Collections.unmodifiableList(outResult)
    }
}