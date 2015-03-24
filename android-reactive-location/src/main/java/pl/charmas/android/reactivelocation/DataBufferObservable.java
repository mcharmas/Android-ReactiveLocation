package pl.charmas.android.reactivelocation;

import com.google.android.gms.common.data.AbstractDataBuffer;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

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
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                Observable.from(buffer).subscribe(subscriber);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        buffer.release();
                    }
                }));
            }
        });
    }
}
