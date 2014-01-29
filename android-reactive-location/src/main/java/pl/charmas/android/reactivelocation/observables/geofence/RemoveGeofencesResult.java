package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;

public abstract class RemoveGeofencesResult {
    private final LocationStatusCode statusCode;

    private RemoveGeofencesResult(int statusCode) {
        this.statusCode = LocationStatusCode.fromCode(statusCode);
    }

    public LocationStatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * If operation was successful. Status code is equal {@link com.google.android.gms.location.LocationStatusCodes#SUCCESS}.
     *
     * @return if operation was successful
     */
    public boolean isSuccess() {
        return LocationStatusCode.SUCCESS.equals(statusCode);
    }

    /**
     * Result of removing geofences operation by PendingIntent.
     */
    public static class PengingIntentRemoveGeofenceResult extends RemoveGeofencesResult {
        private final PendingIntent pendingIntent;

        PengingIntentRemoveGeofenceResult(int statusCode, PendingIntent pendingIntent) {
            super(statusCode);
            this.pendingIntent = pendingIntent;
        }

        public PendingIntent getPendingIntent() {
            return pendingIntent;
        }
    }

    /**
     * Result of removing geofences operation by requestIds.
     */
    public static class RequestIdsRemoveGeofenceResult extends RemoveGeofencesResult {
        private final String[] requestIds;

        RequestIdsRemoveGeofenceResult(int statusCode, String[] requestIds) {
            super(statusCode);
            this.requestIds = requestIds;
        }

        public String[] getRequestIds() {
            return requestIds;
        }
    }
}
