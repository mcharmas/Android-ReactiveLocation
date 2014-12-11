package pl.charmas.android.reactivelocation.observables.location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import android.content.Context;
import android.location.Location;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class LastKnownLocationObservable extends BaseLocationObservable<Location> {

   private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;


    private LastKnownLocationObservable(Context ctx) {
    super(ctx);
  }

  public static Observable<Location> createObservable(Context ctx) {
    return Observable.create(new LastKnownLocationObservable(ctx));
  }

  @Override
  protected void onLocationClientReady(GoogleApiClient googleApiClient,
      Observer<? super Location> observer) {
    observer.onNext(fusedLocationProviderApi.getLastLocation(googleApiClient));
    observer.onCompleted();
  }

  @Override
  protected void onLocationClientDisconnected(Observer<? super Location> observer) {
  }
}
