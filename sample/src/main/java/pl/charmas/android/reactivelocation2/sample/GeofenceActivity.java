package pl.charmas.android.reactivelocation2.sample;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation2.sample.utils.DisplayTextOnViewAction;
import pl.charmas.android.reactivelocation2.sample.utils.LocationToStringFunc;

import static pl.charmas.android.reactivelocation2.sample.utils.UnsubscribeIfPresent.dispose;

public class GeofenceActivity extends BaseActivity {
    private static final String TAG = "GeofenceActivity";

    private ReactiveLocationProvider reactiveLocationProvider;
    private EditText latitudeInput;
    private EditText longitudeInput;
    private EditText radiusInput;
    private TextView lastKnownLocationView;
    private Disposable lastKnownLocationDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reactiveLocationProvider = new ReactiveLocationProvider(this);
        setContentView(R.layout.activity_geofence);
        initViews();
    }

    private void initViews() {
        lastKnownLocationView = (TextView) findViewById(R.id.last_known_location_view);
        latitudeInput = (EditText) findViewById(R.id.latitude_input);
        longitudeInput = (EditText) findViewById(R.id.longitude_input);
        radiusInput = (EditText) findViewById(R.id.radius_input);
        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGeofence();
            }
        });
        findViewById(R.id.clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearGeofence();
            }
        });
    }

    @Override
    protected void onLocationPermissionGranted() {
        lastKnownLocationDisposable = reactiveLocationProvider
                .getLastKnownLocation()
                .map(new LocationToStringFunc())
                .subscribe(new DisplayTextOnViewAction(lastKnownLocationView));
    }

    @Override
    protected void onStop() {
        super.onStop();
        dispose(lastKnownLocationDisposable);
    }

    private void clearGeofence() {
        reactiveLocationProvider
                .removeGeofences(createNotificationBroadcastPendingIntent())
                .subscribe(new Consumer<Status>() {
                    @Override
                    public void accept(Status status) throws Exception {
                        toast("Geofences removed");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        toast("Error removing geofences");
                        Log.d(TAG, "Error removing geofences", throwable);
                    }
                });
    }

    private void toast(String text) {
        Toast.makeText(GeofenceActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    private PendingIntent createNotificationBroadcastPendingIntent() {
        return PendingIntent.getBroadcast(this, 0, new Intent(this, GeofenceBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void addGeofence() {
        final GeofencingRequest geofencingRequest = createGeofencingRequest();
        if (geofencingRequest == null) return;

        final PendingIntent pendingIntent = createNotificationBroadcastPendingIntent();

        reactiveLocationProvider
                .removeGeofences(pendingIntent)
                .flatMap(new Function<Status, Observable<Status>>() {
                    @Override
                    public Observable<Status> apply(Status status) throws Exception {
                        return reactiveLocationProvider.addGeofences(pendingIntent, geofencingRequest);
                    }

                })
                .subscribe(new Consumer<Status>() {
                    @Override
                    public void accept(Status addGeofenceResult) {
                        toast("Geofence added, success: " + addGeofenceResult.isSuccess());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        toast("Error adding geofence.");
                        Log.d(TAG, "Error adding geofence.", throwable);
                    }
                });
    }

    private GeofencingRequest createGeofencingRequest() {
        try {
            double longitude = Double.parseDouble(longitudeInput.getText().toString());
            double latitude = Double.parseDouble(latitudeInput.getText().toString());
            float radius = Float.parseFloat(radiusInput.getText().toString());
            Geofence geofence = new Geofence.Builder()
                    .setRequestId("GEOFENCE")
                    .setCircularRegion(latitude, longitude, radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            return new GeofencingRequest.Builder().addGeofence(geofence).build();
        } catch (NumberFormatException ex) {
            toast("Error parsing input.");
            return null;
        }
    }
}
