package pl.charmas.android.reactivelocation.observables.location;

public class IntentUpdatesException extends Throwable {
    private final LocationUpdatesResult locationUpdatesResult;

    public IntentUpdatesException(LocationUpdatesResult locationUpdatesResult) {
        super("Error adding Intent location updates. Status code: " + locationUpdatesResult.getStatusCode());
        this.locationUpdatesResult = locationUpdatesResult;
    }

    public LocationUpdatesResult getLocationUpdatesResult() {
        return locationUpdatesResult;
    }
}
