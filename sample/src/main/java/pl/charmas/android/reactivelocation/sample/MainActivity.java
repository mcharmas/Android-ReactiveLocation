package pl.charmas.android.reactivelocation.sample;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;

import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends ActionBarActivity {

    private ReactiveLocationProvider locationProvider;

    private TextView lastKnownLocationView;
    private TextView updatableLocationView;
    private TextView addressLocationView;

    private Observable<Location> lastKnownLocationObservable;
    private Observable<Location> locationUpdatesObservable;

    private Subscription lastKnownLocationSubscription;
    private Subscription updatableLocationSubscription;
    private Subscription addressSubscription;
    private DisplayErrorAction errorAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        errorAction = new DisplayErrorAction();

        lastKnownLocationView = (TextView) findViewById(R.id.last_known_location_view);
        updatableLocationView = (TextView) findViewById(R.id.updated_location_view);
        addressLocationView = (TextView) findViewById(R.id.address_for_location_view);

        locationProvider = new ReactiveLocationProvider(getApplicationContext());
        lastKnownLocationObservable = locationProvider.getLastKnownLocation();

        locationUpdatesObservable = locationProvider.getUpdatedLocation(
                LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setNumUpdates(5)
                        .setInterval(100)
        );

    }

    @Override
    protected void onStart() {
        super.onStart();
        lastKnownLocationSubscription = lastKnownLocationObservable
                .map(new LocationToStringFunc())
                .subscribe(new DisplayTextOnViewAction(lastKnownLocationView), errorAction);

        updatableLocationSubscription = locationUpdatesObservable
                .map(new LocationToStringFunc())
                .map(new Func1<String, String>() {
                    int count = 0;

                    @Override
                    public String call(String s) {
                        return s + " " + count++;
                    }
                })
                .subscribe(new DisplayTextOnViewAction(updatableLocationView), errorAction);

        addressSubscription = AndroidObservable.bindActivity(this, locationUpdatesObservable
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
                .subscribe(new DisplayTextOnViewAction(addressLocationView), errorAction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        lastKnownLocationSubscription.unsubscribe();
        updatableLocationSubscription.unsubscribe();
        addressSubscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        errorAction.dismiss();
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
            return location.getLatitude() + " " + location.getLongitude() + " (" + location.getAccuracy() + ")";
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

    private class DisplayErrorAction implements Action1<Throwable> {
        private AlertDialog errorDialog;

        @Override
        public void call(Throwable throwable) {
            Log.e("MainActivity", "Error occurred.", throwable);
            if(errorDialog == null) {
                errorDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
                        .setMessage("Some error occured. See LogCat")
                        .create();
                errorDialog.show();
            }
        }

        public void dismiss() {
            if(errorDialog != null) {
                errorDialog.dismiss();
            }
        }
    }
}
