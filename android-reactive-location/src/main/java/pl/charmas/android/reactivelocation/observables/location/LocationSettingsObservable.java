package pl.charmas.android.reactivelocation.observables.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AndroidRuntimeException;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class LocationSettingsObservable extends BaseLocationObservable<Boolean> {

    private static final Map<String, WeakReference<LocationSettingsObservable>> observableMap = new HashMap<>();

    private final LocationSettingsRequest locationSettingsRequest;
    private final WeakReference<Activity> contextWeakRef;
    private WeakReference<Observer<? super Boolean>> observerWeakRef;

    public static Observable<Boolean> createObservable(Activity ctx, LocationRequest locationRequest) {
        return Observable.create(new LocationSettingsObservable(ctx, locationRequest));
    }

    private LocationSettingsObservable(Activity ctx, LocationRequest locationRequest) {
        super(ctx);
        this.contextWeakRef = new WeakReference<>(ctx);
        this.locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super Boolean> observer) {
        observerWeakRef = new WeakReference<Observer<? super Boolean>>(observer);

        LocationServices.SettingsApi.checkLocationSettings(apiClient, locationSettingsRequest).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        observer.onNext(true);
                        observer.onCompleted();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.

                        Context context = contextWeakRef.get();

                        if(context != null) {
                            String observableId = UUID.randomUUID().toString();

                            observableMap.put(observableId, new WeakReference<>(LocationSettingsObservable.this));

                            Intent intent = new Intent(context, LocationSettingsActivity.class);
                            intent.putExtra(LocationSettingsActivity.ARG_STATUS, status);
                            intent.putExtra(LocationSettingsActivity.ARG_ID, observableId);
                            context.startActivity(intent);
                        }

                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.

                        observer.onNext(false);
                        observer.onCompleted();
                        break;
                }
            }
        });
    }

    public static void onResolutionResult(String observableId, int resultCode) {
        if(observableMap.containsKey(observableId)) {
            LocationSettingsObservable observable = observableMap.get(observableId).get();

            if(observable != null && observable.observerWeakRef != null) {
                Observer<? super Boolean> observer = observable.observerWeakRef.get();

                if(observer != null) {
                    observer.onNext(resultCode == Activity.RESULT_OK);
                    observer.onCompleted();
                }
            }

            observableMap.remove(observableId);
        }
    }
}
