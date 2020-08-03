package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.tasks.Task
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe

class TaskResultMaybeOnSubscribe<T>(private val task: Task<T>) :
    MaybeOnSubscribe<T> {
    override fun subscribe(emitter: MaybeEmitter<T>) {
        task.addOnSuccessListener { result: T? ->
            if (!emitter.isDisposed) {
                result?.let { emitter.onSuccess(it) } ?: emitter.onComplete()
            }
        }
        task.addOnFailureListener { exception ->
            if (!emitter.isDisposed) {
                emitter.onError(exception)
            }
        }
    }
}