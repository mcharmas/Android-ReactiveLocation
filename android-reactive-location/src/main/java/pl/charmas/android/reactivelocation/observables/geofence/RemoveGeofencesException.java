package pl.charmas.android.reactivelocation.observables.geofence;

public class RemoveGeofencesException extends Throwable {
    private final LocationStatusCode statusCode;

    public RemoveGeofencesException(LocationStatusCode statusCode) {
        super("Error removing geofences.");
        this.statusCode = statusCode;
    }
}
