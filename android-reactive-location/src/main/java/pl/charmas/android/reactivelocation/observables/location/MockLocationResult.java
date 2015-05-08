package pl.charmas.android.reactivelocation.observables.location;

import com.google.android.gms.common.api.CommonStatusCodes;

public class MockLocationResult {
    private final int statusCode;

    MockLocationResult(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getName() {
        return CommonStatusCodes.getStatusCodeString(statusCode);
    }

    public boolean isSuccess() {
        return (this.statusCode == CommonStatusCodes.SUCCESS) ||
                (this.statusCode == CommonStatusCodes.SUCCESS_CACHE);
    }
}
