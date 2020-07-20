package pl.charmas.android.reactivelocation2.observables

import io.reactivex.Maybe
import io.reactivex.MaybeOnSubscribe
import io.reactivex.MaybeSource
import io.reactivex.MaybeTransformer
import io.reactivex.functions.BiPredicate
import pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionSuspendedException

class MaybeFactory(private val context: MaybeContext) {
    fun <T> createMaybe(source: MaybeOnSubscribe<T>?): Maybe<T> {
        return Maybe.create(source).compose(
            RetryOnConnectionSuspension(
                context.isRetryOnConnectionSuspended
            )
        )
    }

    private class RetryOnConnectionSuspension<T> internal constructor(private val shouldRetry: Boolean) :
        MaybeTransformer<T, T> {
        override fun apply(upstream: Maybe<T>): MaybeSource<T> {
            return if (shouldRetry) {
                upstream.retry(IsConnectionSuspendedException())
            } else upstream
        }

        private class IsConnectionSuspendedException :
            BiPredicate<Int, Throwable> {
            override fun test(integer: Int, throwable: Throwable): Boolean {
                return throwable is GoogleAPIConnectionSuspendedException
            }
        }
    }
}