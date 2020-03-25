package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.tasks.Task
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe

class TaskResultMaybeOnSubscribe<T>(private val result: Task<T>) :
    MaybeOnSubscribe<T> {
    override fun subscribe(emitter: MaybeEmitter<T>) {
        result.addOnSuccessListener { t: T ->
            if (!emitter.isDisposed) {
                emitter.onSuccess(t)
            }
        }
        result.addOnCompleteListener { command ->
            if (!emitter.isDisposed) {
                val value = command.result
                if (value != null) {
                    emitter.onSuccess(value)
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