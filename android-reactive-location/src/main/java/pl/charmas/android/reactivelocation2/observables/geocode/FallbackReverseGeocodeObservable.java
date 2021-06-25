package pl.charmas.android.reactivelocation2.observables.geocode;

import android.location.Address;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;


class FallbackReverseGeocodeObservable implements ObservableOnSubscribe<List<Address>> {
    private final Locale locale;
    private final double latitude;
    private final double longitude;
    private final int maxResults;

    FallbackReverseGeocodeObservable(Locale locale, double latitude, double longitude, int maxResults) {
        this.locale = locale;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxResults = maxResults;
    }

    @Override
    public void subscribe(ObservableEmitter<List<Address>> emitter) throws Exception {
        try {
            List<Address> addresses = alternativeReverseGeocodeQuery();
            if (!emitter.isDisposed()) {
                emitter.onNext(addresses);
                emitter.onComplete();
            }
        } catch (Exception ex) {
            if (!emitter.isDisposed()) {
                emitter.onError(ex);
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
    private List<Address> alternativeReverseGeocodeQuery() throws IOException, JSONException {
        URL url = new URL(String.format(Locale.ENGLISH,
                "http://maps.googleapis.com/maps/api/geocode/json?"
                        + "latlng=%1$f,%2$f&sensor=true&language=%3$s",
                latitude, longitude, locale.getLanguage()
        ));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        StringBuilder stringBuilder = new StringBuilder();
        List<Address> outResult = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Root json response object
            JSONObject jsonRootObject = new JSONObject(stringBuilder.toString());

            // No results status
            if ("ZERO_RESULTS".equalsIgnoreCase(jsonRootObject.getString("status"))) {
                return Collections.emptyList();
            }

            // Other non-OK responses status
            if (!"OK".equalsIgnoreCase(jsonRootObject.getString("status"))) {
                throw new RuntimeException("Wrong API response");
            }

            // Process results
            JSONArray results = jsonRootObject.getJSONArray("results");
            for (int i = 0; i < results.length() && i < maxResults; i++) {
                Address address = new Address(Locale.getDefault());
                String addressLineString = "";
                JSONObject sourceResult = results.getJSONObject(i);
                JSONArray addressComponents = sourceResult.getJSONArray("address_components");

                // Assemble address by various components
                for (int ac = 0; ac < addressComponents.length(); ac++) {
                    String longNameVal = addressComponents.getJSONObject(ac).getString("long_name");
                    String shortNameVal = addressComponents.getJSONObject(ac).getString("short_name");
                    JSONArray acTypes = addressComponents.getJSONObject(ac).getJSONArray("types");
                    String acType = acTypes.getString(0);

                    if (!TextUtils.isEmpty(longNameVal)) {
                        if (acType.equalsIgnoreCase("street_number")) {
                            if (TextUtils.isEmpty(addressLineString)) {
                                addressLineString = longNameVal;
                            } else {
                                addressLineString += " " + longNameVal;
                            }
                        } else if (acType.equalsIgnoreCase("route")) {
                            if (TextUtils.isEmpty(addressLineString)) {
                                addressLineString = longNameVal;
                            } else {
                                addressLineString = longNameVal + " " + addressLineString;
                            }
                        } else if (acType.equalsIgnoreCase("sublocality")) {
                            address.setSubLocality(longNameVal);
                        } else if (acType.equalsIgnoreCase("locality")) {
                            address.setLocality(longNameVal);
                        } else if (acType.equalsIgnoreCase("administrative_area_level_2")) {
                            address.setSubAdminArea(longNameVal);
                        } else if (acType.equalsIgnoreCase("administrative_area_level_1")) {
                            address.setAdminArea(longNameVal);
                        } else if (acType.equalsIgnoreCase("country")) {
                            address.setCountryName(longNameVal);
                            address.setCountryCode(shortNameVal);
                        } else if (acType.equalsIgnoreCase("postal_code")) {
                            address.setPostalCode(longNameVal);
                        }
                    }
                }

                // Try to get the already formatted address
                String formattedAddress = sourceResult.getString("formatted_address");
                if (!TextUtils.isEmpty(formattedAddress)) {
                    String[] formattedAddressLines = formattedAddress.split(",");

                    for (int ia = 0; ia < formattedAddressLines.length; ia++) {
                        address.setAddressLine(ia, formattedAddressLines[ia].trim());
                    }
                } else if (!TextUtils.isEmpty(addressLineString)) {
                    // If that fails use our manually assembled formatted address
                    address.setAddressLine(0, addressLineString);
                }

                // Finally add address to resulting set
                outResult.add(address);
            }

        } finally {
            urlConnection.disconnect();
        }

        return Collections.unmodifiableList(outResult);
    }
}
