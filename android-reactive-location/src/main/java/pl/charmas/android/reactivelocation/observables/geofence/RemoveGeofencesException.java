package pl.charmas.android.reactivelocation.observables.geofence;

/**
 * Exception that is delivered only od {@link com.google.android.gms.location.GeofenceStatusCodes}
 * when removing geofences.
 */
public class RemoveGeofencesException extends Throwable {
    private final int statusCode;

    RemoveGeofencesException(int statusCode) {
        super("Error removing geofences.");
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
