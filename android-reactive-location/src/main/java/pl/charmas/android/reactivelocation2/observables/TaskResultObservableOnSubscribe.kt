package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.tasks.Task
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

class TaskResultObservableOnSubscribe<T>(private val result: Task<T>) :
    ObservableOnSubscribe<T> {
    override fun subscribe(emitter: ObservableEmitter<T>) {
        result.addOnSuccessListener { t: T ->
            if (!emitter.isDisposed) {
                emitter.onNext(t)
                emitter.onComplete()
            }
        }
        result.addOnCompleteListener { command ->
            if (!emitter.isDisposed) {
                val value = command.result
                if (value != null) {
                    emitter.onNext(value)
                }else{
                    emitter.onComplete()
                }
            }
        }
        result.addOnFailureListener { exception ->
            if (!emitter.isDisposed) {
                emitter.onError(exception)
            }
        }
    }
}