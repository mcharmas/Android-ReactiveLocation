package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.common.api.Status

class StatusException(val status: Status) :
    Throwable(status.statusCode.toString() + ": " + status.statusMessage)