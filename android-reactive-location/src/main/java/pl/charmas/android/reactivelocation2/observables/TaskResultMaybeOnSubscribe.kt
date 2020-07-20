package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.tasks.Task
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe

class TaskResultMaybeOnSubscribe<T>(private val task: Task<T>) :
    MaybeOnSubscribe<T> {
    override fun subscribe(emitter: MaybeEmitter<T>) {
        task.addOnSuccessListener { t: T ->
            if (!emitter.isDisposed) {
                emitter.onSuccess(t)
            }
        }
        task.addOnCompleteListener { command ->
            if (!emitter.isDisposed) {
                val result = command.result
                if (result != null) {
                    emitter.onSuccess(result)
                }else{
                    emitter.onComplete()
                }
            }
        }
        task.addOnFailureListener { exception ->
            if (!emitter.isDisposed) {
                emitter.onError(exception)
            }
        }
    }
}