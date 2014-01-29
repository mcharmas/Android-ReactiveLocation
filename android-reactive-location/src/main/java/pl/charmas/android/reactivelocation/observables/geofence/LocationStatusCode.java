package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.location.LocationStatusCodes;

/**
 * {@link com.google.android.gms.location.LocationStatusCodes} wrapper in proper enum.
 */
public enum LocationStatusCode {
    SUCCESS(LocationStatusCodes.SUCCESS, "SUCCESS"),
    ERROR(LocationStatusCodes.ERROR, "ERROR"),
    GEOFENCE_NOT_AVAILABLE(LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, "GEOFENCE_NOT_AVAILABLE"),
    GEOFENCE_TOO_MANY_PENDING_INTENTS(LocationStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS, "GEOFENCE_TOO_MANY_PENDING_INTENTS"),
    GEOFENCE_TOO_MANY_GEOFENCES(LocationStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS, "GEOFENCE_TOO_MANY_PENDING_INTENTS"),
    UNKNOWN(-1, "STATUS_CODE_UNKNOWN");

    private final int statusCode;
    private final String name;

    private LocationStatusCode(int statusCode, String name) {
        this.statusCode = statusCode;
        this.name = name;
    }

    /**
     * @return orginal status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return status name for debugging purposes
     */
    public String getName() {
        return name;
    }

    static LocationStatusCode fromCode(int statusCode) {
        for (LocationStatusCode code : LocationStatusCode.values()) {
            if (code.statusCode == statusCode)
                return code;
        }
        return UNKNOWN;
    }
}
