package pl.charmas.android.reactivelocation.observables;

import android.content.Context;

import com.google.android.gms.location.LocationServices;


//public abstract class BaseLocationObservable<T> implements Observable.OnSubscribe<T> {
//
//    private final Context ctx;
//
//    protected BaseLocationObservable(Context ctx) {
//        this.ctx = ctx;
//    }
//
//    @Override
//    public void call(Subscriber<? super T> subscriber) {
//        final LocationConnectionCallbacks locationConnectionCallbacks = new LocationConnectionCallbacks(subscriber);
//        final GoogleApiClient apiClient = new GoogleApiClient.Builder(ctx)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(locationConnectionCallbacks)
//                .addOnConnectionFailedListener(locationConnectionCallbacks)
//                .build();
//        locationConnectionCallbacks.setClient(apiClient);
//
//        try {
//            apiClient.connect();
//        } catch (Throwable ex) {
//            subscriber.onError(ex);
//        }
//
//        subscriber.add(Subscriptions.create(new Action0() {
//            @Override
//            public void call() {
//                if (apiClient.isConnected() || apiClient.isConnecting()) {
//                    onUnsubscribed(apiClient);
//                    apiClient.disconnect();
//                }
//            }
//        }));
//    }
//
//    protected void onUnsubscribed(GoogleApiClient locationClient) {
//    }
//
//    protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super T> observer);
//
//    private class LocationConnectionCallbacks implements
//            GoogleApiClient.ConnectionCallbacks,
//            GoogleApiClient.OnConnectionFailedListener {
//        final private Observer<? super T> observer;
//        private GoogleApiClient apiClient;
//
//        private LocationConnectionCallbacks(Observer<? super T> observer) {
//            this.observer = observer;
//        }
//
//        @Override
//        public void onConnected(Bundle bundle) {
//            try {
//                onGoogleApiClientReady(apiClient, observer);
//            } catch (Throwable ex) {
//                observer.onError(ex);
//            }
//        }
//
//        @Override
//        public void onConnectionSuspended(int cause) {
//            observer.onError(new LocationConnectionSuspendedException(cause));
//        }
//
//        @Override
//        public void onConnectionFailed(ConnectionResult connectionResult) {
//            observer.onError(new LocationConnectionException("Error connecting to LocationClient.", connectionResult));
//        }
//
//        public void setClient(GoogleApiClient client) {
//            this.apiClient = client;
//        }
//    }
//}
public abstract class BaseLocationObservable<T> extends BaseObservable<T> {


    protected BaseLocationObservable(Context ctx) {
        super(ctx, LocationServices.API);
    }

}
