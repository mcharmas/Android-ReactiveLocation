package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.location.LocationServices

abstract class BaseLocationObservableOnSubscribe<T> protected constructor(ctx: ObservableContext) :
    BaseObservableOnSubscribe<T>(ctx, LocationServices.API)