package pl.charmas.android.reactivelocation2.observables.exceptions

class GoogleAPIConnectionSuspendedException internal constructor(val errorCause: Int) :
    RuntimeException()