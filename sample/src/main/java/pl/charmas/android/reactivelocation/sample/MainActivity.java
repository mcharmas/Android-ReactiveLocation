package pl.charmas.android.reactivelocation.sample;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;

import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.observables.activity.ActivityUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String STATE_RUNNING = "STATE_RUNNING";
    private boolean is_running = false;

    private ReactiveLocationProvider locationProvider;

    private TextView lastKnownLocationView;
    private TextView updatableLocationView;
    private TextView addressLocationView;
    private TextView currentActivityView;

    private Observable<Location> lastKnownLocationObservable;
    private Observable<Location> locationUpdatesObservable;
    private Observable<DetectedActivity> activityObservable;

    private Subscription lastKnownLocationSubscription;
    private Subscription updatableLocationSubscription;
    private Subscription addressSubscription;
    private Subscription activitySubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        lastKnownLocationView = (TextView) findViewById(R.id.last_known_location_view);
        updatableLocationView = (TextView) findViewById(R.id.updated_location_view);
        addressLocationView = (TextView) findViewById(R.id.address_for_location_view);
        currentActivityView = (TextView) findViewById(R.id.most_recent_activity_view);

        locationProvider = new ReactiveLocationProvider(getApplicationContext());
        lastKnownLocationObservable = locationProvider.getLastKnownLocation();

        locationUpdatesObservable = locationProvider.getUpdatedLocation(
                LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setNumUpdates(5)
                        .setInterval(100)
        );

        activityObservable = locationProvider.getDetectedActivity(0);


    }


    @Override
    protected void onStart() {
        lastKnownLocationSubscription = lastKnownLocationObservable
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "lastKnownLocationSubscription", throwable);
                    }
                })
                .map(new LocationToStringFunc())
                .subscribe(new DisplayTextOnViewAction(lastKnownLocationView));

        updatableLocationSubscription = locationUpdatesObservable
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "locationUpdatesObservable", throwable);
                    }
                })
                .map(new LocationToStringFunc())
                .map(new Func1<String, String>() {
                    int count = 0;

                    @Override
                    public String call(String s) {
                        return s + " " + count++;
                    }
                })
                .subscribe(new DisplayTextOnViewAction(updatableLocationView));

        addressSubscription = AndroidObservable.bindActivity(this, locationUpdatesObservable
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "addressSubscription", throwable);
                    }
                })
                .flatMap(new Func1<Location, Observable<List<Address>>>() {
                    @Override
                    public Observable<List<Address>> call(Location location) {
                        return locationProvider.getGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
                    }
                })
                .map(new Func1<List<Address>, Address>() {
                    @Override
                    public Address call(List<Address> addresses) {
                        return addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
                    }
                })
                .map(new AddressToStringFunc())
                .subscribeOn(Schedulers.io()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisplayTextOnViewAction(addressLocationView));

        activitySubscription = activityObservable
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "activityObservable", throwable);
                    }
                })
                .map(new Func1<DetectedActivity, String>() {
                    @Override
                    public String call(DetectedActivity detectedActivity) {
                        String resultText = "Activity: " + ActivityUtils.getNameFromType(detectedActivity.getType()) + "\nConfidence: " + detectedActivity.getConfidence();
                        return resultText;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisplayTextOnViewAction(currentActivityView));
        super.onStart();
    }

    @Override
    protected void onStop() {
        unSubscribeIfNotNull(updatableLocationSubscription);
        unSubscribeIfNotNull(addressSubscription);
        unSubscribeIfNotNull(activitySubscription);
        super.onStop();
    }


    private void unSubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }


    private static class AddressToStringFunc implements Func1<Address, String> {
        @Override
        public String call(Address address) {
            if (address == null) return "";

            String addressLines = "";
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressLines += address.getAddressLine(i) + '\n';
            }
            return addressLines;
        }
    }

    private static class LocationToStringFunc implements Func1<Location, String> {
        @Override
        public String call(Location location) {
            if (location != null)
                return location.getLatitude() + " " + location.getLongitude() + " (" + location.getAccuracy() + ")";
            return "no location available";
        }
    }

    private static class DisplayTextOnViewAction implements Action1<String> {
        private final TextView target;

        private DisplayTextOnViewAction(TextView target) {
            this.target = target;
        }

        @Override
        public void call(String s) {
            target.setText(s);
        }
    }
}
