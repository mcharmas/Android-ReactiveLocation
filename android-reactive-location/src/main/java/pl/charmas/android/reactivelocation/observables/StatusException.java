package pl.charmas.android.reactivelocation.observables;

import com.google.android.gms.common.api.Status;

public class StatusException extends Throwable {
    private final Status status;

    public StatusException(Status status) {
        super(status.getStatusCode() + ": " + status.getStatusMessage());
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
