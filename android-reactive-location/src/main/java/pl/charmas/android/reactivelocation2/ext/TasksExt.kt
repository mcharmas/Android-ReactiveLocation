package pl.charmas.android.reactivelocation2.ext

import com.google.android.gms.tasks.Task
import io.reactivex.Maybe
import pl.charmas.android.reactivelocation2.observables.TaskResultMaybeOnSubscribe

fun <T> Task<T>.toMaybe(): Maybe<T> {
    return Maybe.create(TaskResultMaybeOnSubscribe(this))
}
