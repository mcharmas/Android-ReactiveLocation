package pl.charmas.android.reactivelocation.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.location.LocationRequest;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.trace.Tracer;
import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                .subscribe(new DisplayTextOnViewAction(lastKnownLocationView));

        updatableLocationSubscription = locationUpdatesObservable
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        lastKnownLocationSubscription.unsubscribe();
        updatableLocationSubscription.unsubscribe();
        addressSubscription.unsubscribe();
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

    public void onStartTrack(View view) {
        SampleApplication app = (SampleApplication) SampleApplication.getContext();
        app.startTrack();
    }

    public void onStopTrack(View view) {
        SampleApplication app = (SampleApplication) SampleApplication.getContext();
        Tracer tracer = app.stopTrack();
        if (tracer != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Track results");
            alertDialogBuilder.setMessage("Run: " + tracer.getRun() + " km \nStay time: " + tracer.getStayTime() + " ms \nAverage speed: " + tracer.getAverageSpeed() + " km/h");

            alertDialogBuilder.setNeutralButton("ОК", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();
        }
    }
}
