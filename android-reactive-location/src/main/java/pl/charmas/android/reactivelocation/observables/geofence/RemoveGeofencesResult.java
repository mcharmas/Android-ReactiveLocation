package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.GeofenceStatusCodes;

import java.util.List;

public abstract class RemoveGeofencesResult {
    private final int statusCode;

    private RemoveGeofencesResult(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getName() {
        return GeofenceStatusCodes.getStatusCodeString(this.statusCode);
    }
    /**
     * If operation was successful. Status code is equal {@link com.google.android.gms.location.LocationStatusCodes#SUCCESS}.
     *
     * @return if operation was successful
     */
    public boolean isSuccess() {
        return (this.statusCode == CommonStatusCodes.SUCCESS) ||
                (this.statusCode == CommonStatusCodes.SUCCESS_CACHE);
    }

    /**
     * Result of removing geofences operation by PendingIntent.
     */
    public static class PendingIntentRemoveGeofenceResult extends RemoveGeofencesResult {
        private final PendingIntent pendingIntent;

        PendingIntentRemoveGeofenceResult(int statusCode, PendingIntent pendingIntent) {
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
        private final List<String> requestIds;

        RequestIdsRemoveGeofenceResult(int statusCode, List<String> requestIds) {
            super(statusCode);
            this.requestIds = requestIds;
        }

        public List<String> getRequestIds() {
            return requestIds;
        }
    }
}
