package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class AddGeofenceObservable extends BaseLocationObservable<AddGeofenceObservable.AddGeofenceResult> {
    private final List<Geofence> gefences;
    private final PendingIntent geofenceTransitionPendingIntent;

    public static Observable<AddGeofenceResult> createObservable(Context ctx, List<Geofence> geofences, PendingIntent geofenceTransitionPendingIntent) {
        return Observable.create(new AddGeofenceObservable(ctx, geofences, geofenceTransitionPendingIntent));
    }

    private AddGeofenceObservable(Context ctx, List<Geofence> geofences, PendingIntent geofenceTransitionPendingIntent) {
        super(ctx);
        this.gefences = geofences;
        this.geofenceTransitionPendingIntent = geofenceTransitionPendingIntent;
    }

    @Override
    protected void onLocationClientReady(LocationClient locationClient, final Observer<? super AddGeofenceResult> observer) {
        locationClient.addGeofences(gefences, geofenceTransitionPendingIntent, new LocationClient.OnAddGeofencesResultListener() {
            @Override
            public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
                AddGeofenceResult result = new AddGeofenceResult(statusCode, geofenceRequestIds);
                if(LocationStatusCode.ERROR.equals(result.getStatusCode())) {
                    observer.onError(new AddGeofenceException(result));
                } else {
                    observer.onNext(result);
                    observer.onCompleted();
                }
            }
        });
    }

    @Override
    protected void onLocationClientDisconnected(Observer<? super AddGeofenceResult> observer) {
    }

    public static final class AddGeofenceResult {
        private final LocationStatusCode statusCode;
        private final String[] geofenceRequestIds;

        public AddGeofenceResult(int statusCode, String[] geofenceRequestIds) {
            this.statusCode = LocationStatusCode.fromCode(statusCode);
            this.geofenceRequestIds = geofenceRequestIds;
        }

        public LocationStatusCode getStatusCode() {
            return statusCode;
        }

        public String[] getGeofenceRequestIds() {
            return geofenceRequestIds;
        }

        public boolean isSuccess() {
            return statusCode == LocationStatusCode.SUCCESS;
        }
    }

}
