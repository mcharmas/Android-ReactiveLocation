package pl.charmas.android.reactivelocation2;

import com.google.android.gms.common.data.DataBuffer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposables;


/**
 * Util class that creates observable from buffer.
 */
public final class DataBufferObservable {

    private DataBufferObservable() {
        //no instance
    }

    /**
     * Creates observable from buffer. On unsubscribe buffer is automatically released.
     *
     * @param buffer source buffer
     * @param <T>    item type
     * @return observable that emits all items from buffer and on unsubscription releases it
     */
    public static <T> Observable<T> from(final DataBuffer<T> buffer) {
        return Observable.create(emitter -> {
            for (T item : buffer) {
                emitter.onNext(item);
            }
            emitter.setDisposable(Disposables.fromAction(() -> buffer.release()));
        });
    }
}
