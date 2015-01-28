package pl.charmas.android.reactivelocation.sample.utils;

import android.location.Location;

import rx.functions.Func1;

public class LocationToStringFunc implements Func1<Location, String> {
    @Override
    public String call(Location location) {
        if (location != null)
            return location.getLatitude() + " " + location.getLongitude() + " (" + location.getAccuracy() + ")";
        return "no location available";
    }
}
