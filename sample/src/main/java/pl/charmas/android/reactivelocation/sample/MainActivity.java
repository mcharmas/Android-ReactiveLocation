package pl.charmas.android.reactivelocation.sample;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.sample.utils.AddressToStringFunc;
import pl.charmas.android.reactivelocation.sample.utils.DetectedActivityToString;
import pl.charmas.android.reactivelocation.sample.utils.DisplayTextOnViewAction;
import pl.charmas.android.reactivelocation.sample.utils.LocationToStringFunc;
import pl.charmas.android.reactivelocation.sample.utils.ToMostProbableActivity;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static pl.charmas.android.reactivelocation.sample.utils.UnsubscribeIfPresent.unsubscribe;

public class MainActivity extends BaseActivity {
    private final static int REQUEST_CHECK_SETTINGS = 0;
    private final static String TAG = "MainActivity";
    private ReactiveLocationProvider locationProvider;

    private TextView lastKnownLocationView;
    private TextView updatableLocationView;
    private TextView addressLocationView;
    private TextView currentActivityView;

    private Observable<Location> lastKnownLocationObservable;
    private Observable<Location> locationUpdatesObservable;
    private Observable<ActivityRecognitionResult> activityObservable;

    private Subscription lastKnownLocationSubscription;
    private Subscription updatableLocationSubscription;
    private Subscription addressSubscription;
    private Subscription activitySubscription;
    private Observable<String> addressObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastKnownLocationView = (TextView) findViewById(R.id.last_known_location_view);
        updatableLocationView = (TextView) findViewById(R.id.updated_location_view);
        addressLocationView = (TextView) findViewById(R.id.address_for_location_view);
        currentActivityView = (TextView) findViewById(R.id.activity_recent_view);

        locationProvider = new ReactiveLocationProvider(getApplicationContext());

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
                .doOnNext(new Action1<LocationSettingsResult>() {
                    @Override
                    public void call(LocationSettingsResult locationSettingsResult) {
                        Status status = locationSettingsResult.getStatus();
                        if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException th) {
                                Log.e("MainActivity", "Error opening settings activity.", th);
                            }
                        }
                    }
                })
                .flatMap(new Func1<LocationSettingsResult, Observable<Location>>() {
                    @Override
                    public Observable<Location> call(LocationSettingsResult locationSettingsResult) {
                        return locationProvider.getUpdatedLocation(locationRequest);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        addressObservable = locationProvider.getUpdatedLocation(locationRequest)
                .flatMap(new Func1<Location, Observable<List<Address>>>() {
                    @Override
                    public Observable<List<Address>> call(Location location) {
                        return locationProvider.getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
                    }
                })
                .map(new Func1<List<Address>, Address>() {
                    @Override
                    public Address call(List<Address> addresses) {
                        return addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
                    }
                })
                .map(new AddressToStringFunc())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        activityObservable = locationProvider
                .getDetectedActivity(50)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onLocationPermissionGranted() {
        lastKnownLocationSubscription = lastKnownLocationObservable
                .map(new LocationToStringFunc())
                .subscribe(new DisplayTextOnViewAction(lastKnownLocationView), new ErrorHandler());

        updatableLocationSubscription = locationUpdatesObservable
                .map(new LocationToStringFunc())
                .map(new Func1<String, String>() {
                    int count = 0;

                    @Override
                    public String call(String s) {
                        return s + " " + count++;
                    }
                })
                .subscribe(new DisplayTextOnViewAction(updatableLocationView), new ErrorHandler());


        addressSubscription = addressObservable
                .subscribe(new DisplayTextOnViewAction(addressLocationView), new ErrorHandler());

        activitySubscription = activityObservable
                .map(new ToMostProbableActivity())
                .map(new DetectedActivityToString())
                .subscribe(new DisplayTextOnViewAction(currentActivityView), new ErrorHandler());
    }

    @Override
    protected void onStop() {
        super.onStop();
        unsubscribe(updatableLocationSubscription);
        unsubscribe(addressSubscription);
        unsubscribe(lastKnownLocationSubscription);
        unsubscribe(activitySubscription);
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

    private class ErrorHandler implements Action1<Throwable> {
        @Override
        public void call(Throwable throwable) {
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
