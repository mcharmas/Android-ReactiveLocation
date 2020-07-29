package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.tasks.Task
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe

class TaskSuccessFailureMaybeOnSubscribe<T>(private val task: Task<T>) :
    MaybeOnSubscribe<T> {
    override fun subscribe(emitter: MaybeEmitter<T>) {
        task.addOnSuccessListener { t: T ->
            if (!emitter.isDisposed) {
                emitter.onSuccess(t)
            }
        }
        task.addOnFailureListener { exception ->
            if (!emitter.isDisposed) {
                emitter.onError(exception)
            }
        }
    }
}