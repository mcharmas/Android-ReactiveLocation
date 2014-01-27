package pl.charmas.android.reactivelocation.observables.geofence;

public class AddGeofenceException extends Throwable {
    private final AddGeofenceObservable.AddGeofenceResult addGeofenceResult;

    public AddGeofenceException(AddGeofenceObservable.AddGeofenceResult addGeofenceResult) {
        super("Error adding geofences. Status code: " + addGeofenceResult.getStatusCode().getName());
        this.addGeofenceResult = addGeofenceResult;
    }

    public AddGeofenceObservable.AddGeofenceResult getAddGeofenceResult() {
        return addGeofenceResult;
    }
}
