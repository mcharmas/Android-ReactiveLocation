package pl.charmas.android.reactivelocation2.observables;

public class GoogleAPIConnectionSuspendedException extends RuntimeException {
    private final int cause;

    GoogleAPIConnectionSuspendedException(int cause) {
        this.cause = cause;
    }

    public int getErrorCause() {
        return cause;
    }
}
