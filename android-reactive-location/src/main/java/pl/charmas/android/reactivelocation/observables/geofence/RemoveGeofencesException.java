package pl.charmas.android.reactivelocation.observables.geofence;

/**
 * Exception that is delivered only od {@link com.google.android.gms.location.LocationStatusCodes#ERROR}
 * when removing geofences.
 */
public class RemoveGeofencesException extends Throwable {
    private final LocationStatusCode statusCode;

    RemoveGeofencesException(LocationStatusCode statusCode) {
        super("Error removing geofences.");
        this.statusCode = statusCode;
    }

    public LocationStatusCode getStatusCode() {
        return statusCode;
    }
}
