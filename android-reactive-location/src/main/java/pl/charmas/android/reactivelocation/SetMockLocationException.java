package pl.charmas.android.reactivelocation;

public class SetMockLocationException extends Throwable {
    private final int statusCode;

    public SetMockLocationException(int statusCode) {
        super("Error setting mock location.");
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
