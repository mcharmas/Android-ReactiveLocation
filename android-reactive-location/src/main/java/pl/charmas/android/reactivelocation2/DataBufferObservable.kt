package pl.charmas.android.reactivelocation2

import com.google.android.gms.common.data.DataBuffer
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposables

/**
 * Util class that creates observable from buffer.
 */
object DataBufferObservable {
    /**
     * Creates observable from buffer. On unsubscribe buffer is automatically released.
     *
     * @param buffer source buffer
     * @param <T>    item type
     * @return observable that emits all items from buffer and on unsubscription releases it
    </T> */
    fun <T> from(buffer: DataBuffer<T>): Observable<T> {
        return Observable.create { emitter ->
            emitter.setDisposable(Disposables.fromAction { buffer.release() })
            for (item in buffer) {
                if (!emitter.isDisposed) {
                    emitter.onNext(item)
                }
            }
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }
    }
}