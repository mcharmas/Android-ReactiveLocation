package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.common.api.Result;

/**
 * Exception that is delivered only od {@link com.google.android.gms.location.LocationStatusCodes#ERROR}
 * when removing geofences.
 */
public class RemoveGeofencesException extends Throwable {
  private final Result statusCode;

  RemoveGeofencesException(Result statusCode) {
    super("Error removing geofences.");
    this.statusCode = statusCode;
  }

  public Result getStatusCode() {
    return statusCode;
  }
}
