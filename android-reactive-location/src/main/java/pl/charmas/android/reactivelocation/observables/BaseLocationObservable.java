package pl.charmas.android.reactivelocation.observables;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import android.content.Context;
import android.os.Bundle;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class BaseLocationObservable<T> implements Observable.OnSubscribe<T> {

  private final Context ctx;

  protected BaseLocationObservable(Context ctx) {
    this.ctx = ctx;
  }

  @Override
  public void call(Subscriber<? super T> subscriber) {
    final LocationConnectionFailedCallbacks locationConnectionFailedCallbacks =
        new LocationConnectionFailedCallbacks(subscriber);

      final LocationConnectionCallbacks locationConnectionCallbacks =
              new LocationConnectionCallbacks(subscriber);


      final GoogleApiClient locationClient = new GoogleApiClient.Builder(ctx)
              .addApi(LocationServices.API)
              .addConnectionCallbacks(locationConnectionCallbacks)
              .addOnConnectionFailedListener(locationConnectionFailedCallbacks)
              .build();

     locationConnectionCallbacks.setClient(locationClient);
     locationConnectionFailedCallbacks.setClient(locationClient);

    try {
      locationClient.connect();
    } catch (Throwable ex) {
      subscriber.onError(ex);
    }

    subscriber.add(Subscriptions.create(new Action0() {
      @Override
      public void call() {
        if (locationClient.isConnected() || locationClient.isConnecting()) {
          onUnsubscribed(locationClient);
          locationClient.disconnect();
        }
      }
    }));
  }

  protected void onUnsubscribed(GoogleApiClient locationClient) {
  }

  protected abstract void onLocationClientReady(GoogleApiClient locationClient,
      Observer<? super T> observer);

  protected abstract void onLocationClientDisconnected(Observer<? super T> observer);

  private class LocationConnectionFailedCallbacks implements GoogleApiClient.OnConnectionFailedListener,
      GooglePlayServicesClient.ConnectionCallbacks {
    final private Observer<? super T> observer;

      private GoogleApiClient locationClient;

      private LocationConnectionFailedCallbacks(Observer<? super T> observer) {
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
      observer.onError(
          new LocationConnectionException("Error connecting to LocationClient.", connectionResult));
    }

    public void setClient(GoogleApiClient client) {
      this.locationClient = client;
    }
  }

    private class LocationConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks,
            GooglePlayServicesClient.ConnectionCallbacks {
        final private Observer<? super T> observer;
        private GoogleApiClient locationClient;

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
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onDisconnected() {
            try {
                onLocationClientDisconnected(observer);
            } catch (Throwable ex) {
                observer.onError(ex);
            }
        }


        public void setClient(GoogleApiClient client) {
            this.locationClient = client;
        }
    }
}
