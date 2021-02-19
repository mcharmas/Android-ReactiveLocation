package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Result
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables

class PendingResultObservableOnSubscribe<T : Result>(
    private val pendingResult: PendingResult<T>
) : ObservableOnSubscribe<T> {

    private var complete = false

    override fun subscribe(emitter: ObservableEmitter<T>) {
        pendingResult.setResultCallback { result: T ->
            if (!emitter.isDisposed) {
                emitter.onNext(result)
                emitter.onComplete()
            }
            complete = true
        }
        emitter.setDisposable(Disposables.fromAction {
            if (!complete) {
                pendingResult.cancel()
            }
        })
    }
}