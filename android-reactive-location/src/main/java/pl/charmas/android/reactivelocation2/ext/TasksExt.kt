package pl.charmas.android.reactivelocation2.ext

import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Result
import com.google.android.gms.tasks.Task
import io.reactivex.Maybe
import io.reactivex.Observable
import pl.charmas.android.reactivelocation2.observables.PendingResultObservableOnSubscribe
import pl.charmas.android.reactivelocation2.observables.TaskResultMaybeOnSubscribe

fun <T> Task<T>.toMaybe(): Maybe<T> {
    return Maybe.create(TaskResultMaybeOnSubscribe(this))
}

fun <T : Result> PendingResult<T>.toObservable(): Observable<T> {
    return Observable.create(PendingResultObservableOnSubscribe(this))
}
