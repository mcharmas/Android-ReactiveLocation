package pl.charmas.android.reactivelocation2;

import com.google.android.gms.common.data.AbstractDataBuffer;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;


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
    public static <T> Observable<T> from(final AbstractDataBuffer<T> buffer) {
        return Observable.create(new ObservableOnSubscribe<T>() {

            @Override
            public void subscribe(final ObservableEmitter<T> emitter) {
                for (T item : buffer) {
                    emitter.onNext(item);
                }
                emitter.setDisposable(Disposables.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        buffer.release();
                    }
                }));
            }
        });
    }
}
