package pl.charmas.android.reactivelocation2;

import com.google.android.gms.common.data.AbstractDataBuffer;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;


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
                emitter.setDisposable(Disposable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        buffer.release();
                    }
                }));
            }
        });
    }
}
