package pl.charmas.android.reactivelocation2.sample.utils;

import io.reactivex.rxjava3.disposables.Disposable;

public final class UnsubscribeIfPresent {
    private UnsubscribeIfPresent() {//no instance
    }

    public static void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
