package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.common.api.Status;

/**
 * Exception that is thrown only on {@link com.google.android.gms.location.LocationStatusCodes#ERROR}
 * when adding geofences. Exception contains whole operation result.
 */
public class AddGeofenceException extends Throwable {
  private final Status addGeofenceResult;

  AddGeofenceException(Status status) {
    super("Error adding geofences. Status code: " + status.getStatusMessage());
    this.addGeofenceResult = status;
  }

  public Status getAddGeofenceResult() {
    return addGeofenceResult;
  }
}
