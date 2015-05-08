package pl.charmas.android.reactivelocation.observables.location;

public class MockLocationException extends Throwable {
    private final MockLocationResult mockLocationResult;

    public MockLocationException(MockLocationResult mockLocationResult) {
        super("Error setting mock location.");
        this.mockLocationResult = mockLocationResult;
    }

    public MockLocationResult getMockLocationResult() {
        return mockLocationResult;
    }

}
