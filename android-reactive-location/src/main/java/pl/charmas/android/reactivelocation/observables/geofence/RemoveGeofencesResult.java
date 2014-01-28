package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;

public abstract class RemoveGeofencesResult {
    private final LocationStatusCode statusCode;

    public RemoveGeofencesResult(int statusCode) {
        this.statusCode = LocationStatusCode.fromCode(statusCode);
    }

    public LocationStatusCode getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return LocationStatusCode.SUCCESS.equals(statusCode);
    }

    public static class PengingIntentRemoveGeofenceResult extends RemoveGeofencesResult {
        private final PendingIntent pendingIntent;

        public PengingIntentRemoveGeofenceResult(int statusCode, PendingIntent pendingIntent) {
            super(statusCode);
            this.pendingIntent = pendingIntent;
        }

        public PendingIntent getPendingIntent() {
            return pendingIntent;
        }
    }

    public static class RequestIdsRemoveGeofenceResult extends RemoveGeofencesResult {
        private final String[] requestIds;

        public RequestIdsRemoveGeofenceResult(int statusCode, String[] requestIds) {
            super(statusCode);
            this.requestIds = requestIds;
        }

        public String[] getRequestIds() {
            return requestIds;
        }
    }

}
