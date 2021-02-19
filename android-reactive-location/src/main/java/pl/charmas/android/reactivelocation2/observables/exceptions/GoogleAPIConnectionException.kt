package pl.charmas.android.reactivelocation2.observables.exceptions

import com.google.android.gms.common.ConnectionResult

class GoogleAPIConnectionException internal constructor(
    detailMessage: String?,
    val connectionResult: ConnectionResult
) : RuntimeException(detailMessage)