package pl.charmas.android.reactivelocation.observables;

public class LocationConnectionSuspendedException extends RuntimeException {
    private final int cause;

    public LocationConnectionSuspendedException(int cause) {
        this.cause = cause;
    }

    public int getErrorCause() {
        return cause;
    }
}
