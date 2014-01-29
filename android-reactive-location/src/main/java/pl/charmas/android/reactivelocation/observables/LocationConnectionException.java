package pl.charmas.android.reactivelocation.observables;

import com.google.android.gms.common.ConnectionResult;

public class LocationConnectionException extends RuntimeException {
    private final ConnectionResult connectionResult;

    LocationConnectionException(String detailMessage, ConnectionResult connectionResult) {
        super(detailMessage);
        this.connectionResult = connectionResult;
    }

    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }
}
