package pl.charmas.android.reactivelocation2.observables.activity

import com.google.android.gms.location.ActivityRecognition
import pl.charmas.android.reactivelocation2.observables.BaseObservableOnSubscribe
import pl.charmas.android.reactivelocation2.observables.ObservableContext

internal abstract class BaseActivityObservableOnSubscribe<T> constructor(ctx: ObservableContext) :
    BaseObservableOnSubscribe<T>(ctx, ActivityRecognition.API)