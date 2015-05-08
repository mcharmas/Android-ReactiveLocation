package pl.charmas.android.reactivelocation.observables.location;

import com.google.android.gms.common.api.CommonStatusCodes;

public final class LocationUpdatesResult {
    private final int statusCode;

    public LocationUpdatesResult(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Status code of GMS operation. Possible values are
     * defined in {@link com.google.android.gms.common.api.CommonStatusCodes}.
     *
     * @return status code of GMS operation
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return name of status code
     */
    public String getStatusCodeName() {
        return CommonStatusCodes.getStatusCodeString(this.statusCode);
    }

    public boolean isSuccess() {
        return (this.statusCode == CommonStatusCodes.SUCCESS) ||
                (this.statusCode == CommonStatusCodes.SUCCESS_CACHE);
    }
}
