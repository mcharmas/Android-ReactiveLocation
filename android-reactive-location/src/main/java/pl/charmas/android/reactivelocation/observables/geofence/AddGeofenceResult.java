package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Result of add geofence action. Contains operation status code and geofence request ids.
 * See @ref com.google.android.gms.location.GeofenceStatusCodes
 */
public final class AddGeofenceResult {
    private final int statusCode;

    AddGeofenceResult(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getName() {
        return GeofenceStatusCodes.getStatusCodeString(this.statusCode);
    }

    public boolean isSuccess() {
        return (this.statusCode == CommonStatusCodes.SUCCESS) ||
            (this.statusCode == CommonStatusCodes.SUCCESS_CACHE);
    }
}
