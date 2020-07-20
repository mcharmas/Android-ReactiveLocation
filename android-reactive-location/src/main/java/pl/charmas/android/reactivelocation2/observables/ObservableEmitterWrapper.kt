package pl.charmas.android.reactivelocation2.observables

import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class ObservableEmitterWrapper<T>(private val emitter: ObservableEmitter<T>) :
    Observer<T> {
    override fun onSubscribe(d: Disposable) {}
    override fun onNext(value: T) {
        if (!emitter.isDisposed) {
            emitter.onNext(value)
        }
    }

    override fun onError(e: Throwable) {
        if (!emitter.isDisposed) {
            emitter.onError(e)
        }
    }

    override fun onComplete() {
        if (!emitter.isDisposed) {
            emitter.onComplete()
        }
    }
}