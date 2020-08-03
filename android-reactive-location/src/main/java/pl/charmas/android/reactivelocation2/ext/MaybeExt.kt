package pl.charmas.android.reactivelocation2.ext

import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

fun <T> MaybeEmitter<T>.onSuccessOrComplete(item: T?) {
    if (isDisposed) {
        return
    }
    item?.let { onSuccess(it) } ?: onComplete()
}

fun <T> Maybe<T>.calldownOrEmpty(time: Int, timeUnit: TimeUnit, scheduler: Scheduler, id: String? = null): Maybe<T> {
    return this.compose { source ->
        Maybe.merge(
            source.map { it.asOptional<T?>() },
            Maybe.timer(time.toLong(), timeUnit, scheduler).mapOrEmpty { Optional.empty<T?>() }
        )
            .firstElement()
    }
        .mapOrEmpty { it.value }
}

fun <T, R> Maybe<T>.mapOrEmpty(mapper: (T) -> R?): Maybe<R> {
    return this.flatMap { item ->
        mapper(item)?.let { Maybe.just<R>(it) } ?: Maybe.empty()
    }
}
