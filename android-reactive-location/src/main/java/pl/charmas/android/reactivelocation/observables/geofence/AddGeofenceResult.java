package pl.charmas.android.reactivelocation.observables.geofence;

/**
 * Result of add geofence action. Contains operation status code and geofence request ids.
 */
public final class AddGeofenceResult {
    private final LocationStatusCode statusCode;
    private final String[] geofenceRequestIds;

    AddGeofenceResult(int statusCode, String[] geofenceRequestIds) {
        this.statusCode = LocationStatusCode.fromCode(statusCode);
        this.geofenceRequestIds = geofenceRequestIds;
    }

    public LocationStatusCode getStatusCode() {
        return statusCode;
    }

    public String[] getGeofenceRequestIds() {
        return geofenceRequestIds;
    }

    public boolean isSuccess() {
        return statusCode == LocationStatusCode.SUCCESS;
    }
}
