package pl.charmas.android.reactivelocation.sample;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import android.util.Log;

public class LocationService extends Service {

    private ReactiveLocationProvider locationProvider;
    private boolean isLocationStarted = false;
    private long LOCATION_MAX_INTERVAL = 2000L;
    private long LOCATION_FASTEST_INTERVAL = 5000L;
    private float SUFFICIENT_ACCURACY = 100; //meters

    private Subscription subscription;

    private IBinder mBinder = new LocationBinder();


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocationBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        locationProvider = new ReactiveLocationProvider(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        locationUnSubscribe();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startLocationListener();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void startLocationListener() {
        if (!isLocationStarted) {
            try {
                if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {

                    isLocationStarted = true;
                    startLocation();
                }
            } catch (IllegalStateException e) {
                Log.e("startLocationListener", "IllegalStateException", e);
            }

        }
    }

    private void startLocation() {
        Log.i("startLocation", "starting Location..");
        LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_MAX_INTERVAL)
                .setMaxWaitTime(LOCATION_MAX_INTERVAL * 2)
                .setFastestInterval(LOCATION_FASTEST_INTERVAL);

        subscription = locationProvider.getUpdatedLocation(request)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("updateLocation", "error", throwable);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Location>() {
                    @Override
                    public Location call(Throwable throwable) {
                        Log.e("getUpdatedLocation", "onErrorReturn", throwable);
                        locationUnSubscribe();
                        return null;
                    }
                })
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        return location != null && location.getAccuracy() < SUFFICIENT_ACCURACY;
                    }
                })    // you can filter location updates
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        doObtainedLocation(location);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable onError) {
                        onError.printStackTrace();
                    }
                });
    }

    public void locationUnSubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
        isLocationStarted = false;
    }

    private void doObtainedLocation(final Location location) {
        Log.i("doObtainedLocation", location.toString());
        //todo send location to server with background executor
    }
}