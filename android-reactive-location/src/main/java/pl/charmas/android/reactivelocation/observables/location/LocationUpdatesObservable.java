package pl.charmas.android.reactivelocation.observables.location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.content.Context;
import android.location.Location;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class LocationUpdatesObservable extends BaseLocationObservable<Location> {

  private final LocationRequest locationRequest;
  private LocationListener listener;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    private LocationUpdatesObservable(Context ctx, LocationRequest locationRequest) {
    super(ctx);
    this.locationRequest = locationRequest;
  }

  public static Observable<Location> createObservable(Context ctx,
      LocationRequest locationRequest) {
    return Observable.create(new LocationUpdatesObservable(ctx, locationRequest));
  }

  @Override
  protected void onLocationClientReady(GoogleApiClient googleApiClient,
      final Observer<? super Location> observer) {
    listener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        observer.onNext(location);
      }
    };
      fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, listener);
  }

  @Override
  protected void onUnsubscribed(GoogleApiClient googleApiClient) {
    if (googleApiClient.isConnected()) {
        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, listener);
    }
  }

  @Override
  protected void onLocationClientDisconnected(Observer<? super Location> observer) {
    observer.onCompleted();
  }
}
