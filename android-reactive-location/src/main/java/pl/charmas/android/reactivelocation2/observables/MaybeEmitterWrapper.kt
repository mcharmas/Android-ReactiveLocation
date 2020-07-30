package pl.charmas.android.reactivelocation2.observables

import io.reactivex.MaybeEmitter
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable

class MaybeEmitterWrapper<T>(private val emitter: MaybeEmitter<T>) :
    MaybeObserver<T> {
    override fun onSubscribe(d: Disposable) {}

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

    override fun onSuccess(value: T) {
        if (!emitter.isDisposed) {
            emitter.onSuccess(value)
        }
    }
}