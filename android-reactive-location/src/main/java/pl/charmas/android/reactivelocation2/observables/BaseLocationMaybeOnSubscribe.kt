package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.location.LocationServices

abstract class BaseLocationMaybeOnSubscribe<T> protected constructor(ctx: MaybeContext) :
    BaseMaybeOnSubscribe<T>(ctx, LocationServices.API)