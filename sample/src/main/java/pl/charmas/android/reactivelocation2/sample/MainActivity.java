package pl.charmas.android.reactivelocation2.sample;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation2.ReactiveLocationProviderConfiguration;
import pl.charmas.android.reactivelocation2.sample.utils.AddressToStringFunc;
import pl.charmas.android.reactivelocation2.sample.utils.DetectedActivityToString;
import pl.charmas.android.reactivelocation2.sample.utils.DisplayTextOnViewAction;
import pl.charmas.android.reactivelocation2.sample.utils.LocationToStringFunc;
import pl.charmas.android.reactivelocation2.sample.utils.ToMostProbableActivity;

import static pl.charmas.android.reactivelocation2.sample.utils.UnsubscribeIfPresent.dispose;

public class MainActivity extends BaseActivity {
    private final static int REQUEST_CHECK_SETTINGS = 0;
    private final static String TAG = "MainActivity";
    private ReactiveLocationProvider locationProvider;

    private TextView lastKnownLocationView;
    private TextView updatableLocationView;
    private TextView addressLocationView;
    private TextView currentActivityView;

    private Single<Location> lastKnownLocationObservable;
    private Observable<Location> locationUpdatesObservable;
    private Observable<ActivityRecognitionResult> activityObservable;

    private Disposable lastKnownLocationDisposable;
    private Disposable updatableLocationDisposable;
    private Disposable addressDisposable;
    private Disposable activityDisposable;
    private Observable<String> addressObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastKnownLocationView = (TextView) findViewById(R.id.last_known_location_view);
        updatableLocationView = (TextView) findViewById(R.id.updated_location_view);
        addressLocationView = (TextView) findViewById(R.id.address_for_location_view);
        currentActivityView = (TextView) findViewById(R.id.activity_recent_view);

        locationProvider = new ReactiveLocationProvider(getApplicationContext(), ReactiveLocationProviderConfiguration
                .builder()
                .setRetryOnConnectionSuspended(true)
                .build()
        );

        lastKnownLocationObservable = locationProvider
                .getLastKnownLocation()
                .observeOn(AndroidSchedulers.mainThread());

        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(5)
                .setInterval(100);
        locationUpdatesObservable = locationProvider
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)  //Refrence: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                                .build()
                )
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (throwable instanceof ResolvableApiException) {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) throwable;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        }
                    }
                })
                .flatMapObservable(new Function<LocationSettingsResponse, Observable<Location>>() {
                    @Override
                    public Observable<Location> apply(LocationSettingsResponse locationSettingsResponse) {
                        return locationProvider.getUpdatedLocation(locationRequest);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        addressObservable = locationProvider.getUpdatedLocation(locationRequest)
                .flatMapSingle(new Function<Location, Single<List<Address>>>() {
                    @Override
                    public Single<List<Address>> apply(Location location) {
                        return locationProvider.reverseGeocode(location.getLatitude(), location.getLongitude(), 1);
                    }
                })
                .map(new Function<List<Address>, Address>() {
                    @Override
                    public Address apply(List<Address> addresses) {
                        return addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
                    }
                })
                .map(new AddressToStringFunc())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        activityObservable = locationProvider
                .detectedActivity(50)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onLocationPermissionGranted() {
        lastKnownLocationDisposable = lastKnownLocationObservable
                .map(new LocationToStringFunc())
                .subscribe(new DisplayTextOnViewAction(lastKnownLocationView), new ErrorHandler());

        updatableLocationDisposable = locationUpdatesObservable
                .map(new LocationToStringFunc())
                .map(new Function<String, String>() {
                    int count = 0;

                    @Override
                    public String apply(String s) {
                        return s + " " + count++;
                    }
                })
                .subscribe(new DisplayTextOnViewAction(updatableLocationView), new ErrorHandler());


        addressDisposable = addressObservable
                .subscribe(new DisplayTextOnViewAction(addressLocationView), new ErrorHandler());

        activityDisposable = activityObservable
                .map(new ToMostProbableActivity())
                .map(new DetectedActivityToString())
                .subscribe(new DisplayTextOnViewAction(currentActivityView), new ErrorHandler());
    }

    @Override
    protected void onStop() {
        super.onStop();
        dispose(updatableLocationDisposable);
        dispose(addressDisposable);
        dispose(lastKnownLocationDisposable);
        dispose(activityDisposable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Geofencing").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, GeofenceActivity.class));
                return true;
            }
        });
        menu.add("Places").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (TextUtils.isEmpty(getString(R.string.API_KEY))) {
                    Toast.makeText(MainActivity.this, "First you need to configure your API Key - see README.md", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(MainActivity.this, PlacesActivity.class));
                }
                return true;
            }
        });
        menu.add("Mock Locations").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, MockLocationsActivity.class));
                return true;
            }
        });
        return true;
    }

    private class ErrorHandler implements Consumer<Throwable> {
        @Override
        public void accept(Throwable throwable) {
            Toast.makeText(MainActivity.this, "Error occurred.", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Error occurred", throwable);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);//intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                //Reference: https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
                switch (resultCode) {
                    case RESULT_OK:
                        // All required changes were successfully made
                        Log.d(TAG, "User enabled location");
                        break;
                    case RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.d(TAG, "User Cancelled enabling location");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

}
