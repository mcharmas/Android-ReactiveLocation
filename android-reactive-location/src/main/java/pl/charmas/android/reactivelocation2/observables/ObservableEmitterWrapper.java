package pl.charmas.android.reactivelocation2.observables;

import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ObservableEmitterWrapper<T> implements Observer<T> {
    private final ObservableEmitter<T> emitter;

    public ObservableEmitterWrapper(ObservableEmitter<T> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(T t) {
        if (!emitter.isDisposed()) {
            emitter.onNext(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (!emitter.isDisposed()) {
            emitter.onError(e);
        }
    }

    @Override
    public void onComplete() {
        if (!emitter.isDisposed()) {
            emitter.onComplete();
        }
    }
}
