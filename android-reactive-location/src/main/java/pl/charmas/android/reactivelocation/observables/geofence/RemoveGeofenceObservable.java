package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.location.LocationClient;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public abstract class RemoveGeofenceObservable<T> extends BaseLocationObservable<T> {

    public static Observable<RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> createObservable(Context ctx, PendingIntent pendingIntent) {
        return Observable.create(new RemoveGeofenceByPendingIntentObservable(ctx, pendingIntent));
    }

    public static Observable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> createObservable(Context ctx, List<String> requestIds) {
        return Observable.create(new RemoveGeofenceRequestIdsObservable(ctx, requestIds));
    }

    protected RemoveGeofenceObservable(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onLocationClientReady(LocationClient locationClient, final Observer<? super T> observer) {
        removeGeofences(locationClient, new LocationClient.OnRemoveGeofencesResultListener() {
            @Override
            public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
                publishResult(new RemoveGeofencesResult.RequestIdsRemoveGeofenceResult(statusCode, geofenceRequestIds));
            }

            @Override
            public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
                publishResult(new RemoveGeofencesResult.PengingIntentRemoveGeofenceResult(statusCode, pendingIntent));

            }

            private void publishResult(RemoveGeofencesResult result) {
                if (LocationStatusCode.ERROR.equals(result.getStatusCode())) {
                    observer.onError(new RemoveGeofencesException(result.getStatusCode()));
                } else {
                    deliverResultToObserver(result, observer);
                    observer.onCompleted();
                }
            }
        });
    }

    protected abstract void deliverResultToObserver(RemoveGeofencesResult result, Observer<? super T> observer);

    @Override
    protected void onLocationClientDisconnected(Observer<? super T> observer) {
    }

    protected abstract void removeGeofences(LocationClient locationClient, LocationClient.OnRemoveGeofencesResultListener onRemoveGeofencesResultListener);

}
