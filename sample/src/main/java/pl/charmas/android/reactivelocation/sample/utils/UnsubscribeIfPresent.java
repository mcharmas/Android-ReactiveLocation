package pl.charmas.android.reactivelocation.sample.utils;

import io.reactivex.disposables.Disposable;

public final class UnsubscribeIfPresent {
    private UnsubscribeIfPresent() {//no instance
    }

    public static void unsubscribe(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
