package pl.charmas.android.reactivelocation2.sample.utils;

import android.location.Location;

import io.reactivex.functions.Function;

public class LocationToStringFunc implements Function<Location, String> {
    @Override
    public String apply(Location location) {
        if (location != null)
            return location.getLatitude() + " " + location.getLongitude() + " (" + location.getAccuracy() + ")";
        return "no location available";
    }
}
