package pl.charmas.android.reactivelocation.sample;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;

import java.util.Date;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.sample.utils.DisplayTextOnViewAction;
import pl.charmas.android.reactivelocation.sample.utils.LocationToStringFunc;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

import static pl.charmas.android.reactivelocation.sample.utils.UnsubscribeIfPresent.unsubscribe;

public class MockLocationsActivity extends BaseActivity {
    private static final String TAG = "MockLocationsActivity";

    private EditText latitudeInput;
    private EditText longitudeInput;
    private TextView mockLocationView;
    private TextView updatedLocationView;
    private ToggleButton mockModeToggleButton;
    private Button setLocationButton;

    private ReactiveLocationProvider locationProvider;
    private Observable<Location> mockLocationObservable;
    private Subscription mockLocationSubscription;
    private Subscription updatedLocationSubscription;

    private PublishSubject<Location> mockLocationSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mocklocations);

        locationProvider = new ReactiveLocationProvider(this);
        mockLocationSubject = PublishSubject.create();

        mockLocationObservable = mockLocationSubject.asObservable();

        initViews();
    }

    private void initViews() {
        latitudeInput = (EditText) findViewById(R.id.latitude_input);
        longitudeInput = (EditText) findViewById(R.id.longitude_input);
        mockLocationView = (TextView) findViewById(R.id.mock_location_view);
        updatedLocationView = (TextView) findViewById(R.id.updated_location_view);
        mockModeToggleButton = (ToggleButton) findViewById(R.id.toggle_button);
        setLocationButton = (Button) findViewById(R.id.set_location_button);

        mockModeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMockMode(isChecked);
                setLocationButton.setEnabled(isChecked);
            }
        });
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMockLocation();
            }
        });
    }

    @Override
    protected void onLocationPermissionGranted() {
        mockModeToggleButton.setChecked(true);

        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000);
        updatedLocationSubscription = locationProvider
                .getUpdatedLocation(locationRequest)
                .map(new LocationToStringFunc())
                .map(new Func1<String, String>() {
                    int count = 0;

                    @Override
                    public String call(String s) {
                        return s + " " + count++;
                    }
                })
                .subscribe(new DisplayTextOnViewAction(updatedLocationView));
    }

    private void addMockLocation() {
        try {
            mockLocationSubject.onNext(createMockLocation());
        } catch (Throwable e) {
            Toast.makeText(MockLocationsActivity.this, "Error parsing input.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setMockMode(boolean toggle) {
        if (toggle) {
            mockLocationSubscription =
                    Observable.zip(locationProvider.mockLocation(mockLocationObservable),
                            mockLocationObservable, new Func2<Status, Location, String>() {
                                int count = 0;

                                @Override
                                public String call(Status result, Location location) {
                                    return new LocationToStringFunc().call(location) + " " + count++;
                                }
                            })
                            .subscribe(new DisplayTextOnViewAction(mockLocationView), new ErrorHandler());
        } else {
            mockLocationSubscription.unsubscribe();
        }
    }

    private Location createMockLocation() {
        String longitudeString = longitudeInput.getText().toString();
        String latitudeString = latitudeInput.getText().toString();

        if (!longitudeString.isEmpty() && !latitudeString.isEmpty()) {
            double longitude = Location.convert(longitudeString);
            double latitude = Location.convert(latitudeString);

            Location mockLocation = new Location("flp");
            mockLocation.setLatitude(latitude);
            mockLocation.setLongitude(longitude);
            mockLocation.setAccuracy(1.0f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            mockLocation.setTime(new Date().getTime());
            return mockLocation;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unsubscribe(mockLocationSubscription);
        unsubscribe(updatedLocationSubscription);
    }

    private class ErrorHandler implements Action1<Throwable> {
        @Override
        public void call(Throwable throwable) {
            if (throwable instanceof SecurityException) {
                Toast.makeText(MockLocationsActivity.this, "You need to enable mock locations in Developer Options.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MockLocationsActivity.this, "Error occurred.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error occurred", throwable);
            }
        }
    }

}

