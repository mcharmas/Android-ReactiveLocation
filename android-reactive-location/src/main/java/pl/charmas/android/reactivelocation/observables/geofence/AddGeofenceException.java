package pl.charmas.android.reactivelocation.observables.geofence;

/**
 * Exception that is thrown only on {@link com.google.android.gms.location.LocationStatusCodes#ERROR}
 * when adding geofences. Exception contains whole operation result.
 */
public class AddGeofenceException extends Throwable {
    private final AddGeofenceResult addGeofenceResult;

    AddGeofenceException(AddGeofenceResult addGeofenceResult) {
        super("Error adding geofences. Status code: " + addGeofenceResult.getName());
        this.addGeofenceResult = addGeofenceResult;
    }

    public AddGeofenceResult getAddGeofenceResult() {
        return addGeofenceResult;
    }
}
