package pl.charmas.android.reactivelocation.observables.location;

public class IntentUpdatesException extends Throwable {
    private final IntentUpdatesResult intentUpdatesResult;

    public IntentUpdatesException(IntentUpdatesResult intentUpdatesResult) {
        super("Error adding Intent location updates. Status code: " + intentUpdatesResult.getStatusCode());
        this.intentUpdatesResult = intentUpdatesResult;
    }

    public IntentUpdatesResult getIntentUpdatesResult() {
        return intentUpdatesResult;
    }
}
