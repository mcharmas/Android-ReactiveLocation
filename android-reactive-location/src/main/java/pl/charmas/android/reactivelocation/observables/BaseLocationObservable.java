package pl.charmas.android.reactivelocation.observables;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;

import rx.Observable;
import rx.Observer;
import rx.Subscription;


public abstract class BaseLocationObservable<T> implements Observable.OnSubscribeFunc<T> {

    private final Context ctx;

    protected BaseLocationObservable(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public Subscription onSubscribe(Observer<? super T> observer) {
        final LocationConnectionCallbacks locationConnectionCallbacks = new LocationConnectionCallbacks(observer);
        final LocationClient locationClient = new LocationClient(ctx, locationConnectionCallbacks, locationConnectionCallbacks);
        locationConnectionCallbacks.setClient(locationClient);

        try {
            locationClient.connect();
        } catch (Throwable ex) {
            observer.onError(ex);
        }

        return new Subscription() {
            @Override
            public void unsubscribe() {
                if (locationClient.isConnected() || locationClient.isConnecting()) {
                    onUnsubscribed(locationClient);
                    locationClient.disconnect();
                }
            }
        };
    }

    protected void onUnsubscribed(LocationClient locationClient) {
    }

    protected abstract void onLocationClientReady(LocationClient locationClient, Observer<? super T> observer);

    protected abstract void onLocationClientDisconnected(Observer<? super T> observer);

    private class LocationConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        final private Observer<? super T> observer;
        private LocationClient locationClient;

        private LocationConnectionCallbacks(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onLocationClientReady(locationClient, observer);
            } catch (Throwable ex) {
                observer.onError(ex);
            }
        }

        @Override
        public void onDisconnected() {
            try {
                onLocationClientDisconnected(observer);
            } catch (Throwable ex) {
                observer.onError(ex);
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            observer.onError(new LocationConnectionException("Error connecting to LocationClient.", connectionResult));
        }

        public void setClient(LocationClient client) {
            this.locationClient = client;
        }
    }
}
