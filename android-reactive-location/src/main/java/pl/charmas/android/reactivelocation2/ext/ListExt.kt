package pl.charmas.android.reactivelocation2.ext

inline fun <T> List<T>.reduceRightDefault(defaultIfEmpty: T, operation: (T, acc: T) -> T): T {
    return if (isEmpty()) defaultIfEmpty
    else reduceRight(operation)
}
