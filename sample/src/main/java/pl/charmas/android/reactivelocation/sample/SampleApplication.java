package pl.charmas.android.reactivelocation.sample;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.trace.TraceRoute;
import pl.charmas.android.reactivelocation.trace.Tracer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class SampleApplication extends Application {

    private static Context mContext;

    private float TRACE_SUFFICIENT_ACCURACY = 100; //meters

    Subscription trace;

    private final static String TRACE_KEY = "traceroute";


    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }

    public void startTrack() {

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {

            if (trace != null) trace.unsubscribe();

            TraceRoute.getInstance().startTrace(TRACE_KEY);

            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setFastestInterval(500);

            ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());
            trace = locationProvider.getUpdatedLocation(request)
                    .filter(new Func1<Location, Boolean>() {
                        @Override
                        public Boolean call(Location location) {
                            return location.getAccuracy() < TRACE_SUFFICIENT_ACCURACY;
                        }
                    })    // you can filter location updates
                    .subscribe(new Action1<Location>() {
                        @Override
                        public void call(Location location) {
                            TraceRoute.getInstance().setTraceRun(TRACE_KEY, location);
                        }
                    });
        }
    }

    public Tracer stopTrack() {
        if (trace != null) trace.unsubscribe();
        return TraceRoute.getInstance().stopTrace(TRACE_KEY);
    }
}