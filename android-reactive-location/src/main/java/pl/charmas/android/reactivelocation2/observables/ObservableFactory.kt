package pl.charmas.android.reactivelocation2.observables

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiPredicate
import pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionSuspendedException

class ObservableFactory constructor(private val context: ObservableContext) {
    fun <T> createObservable(source: ObservableOnSubscribe<T>): Observable<T> {
        return Observable.create(source).compose(
            RetryOnConnectionSuspension(
                context.isRetryOnConnectionSuspended
            )
        )
    }

    private class RetryOnConnectionSuspension<T> internal constructor(private val shouldRetry: Boolean) :
        ObservableTransformer<T, T> {
        override fun apply(upstream: Observable<T>): ObservableSource<T> {
            return if (shouldRetry) {
                upstream.retry(IsConnectionSuspendedException())
            } else upstream
        }

        private class IsConnectionSuspendedException :
            BiPredicate<Int, Throwable> {
            @Throws(Exception::class)
            override fun test(integer: Int, throwable: Throwable): Boolean {
                return throwable is GoogleAPIConnectionSuspendedException
            }
        }
    }
}