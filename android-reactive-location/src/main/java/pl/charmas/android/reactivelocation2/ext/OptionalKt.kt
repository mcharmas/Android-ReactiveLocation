package pl.charmas.android.reactivelocation2.ext

data class Optional<T>(val value: T? = null) {

    companion object {
        fun <T> empty(): Optional<T?> {
            return Optional()
        }
    }

    fun isEmpty(): Boolean {
        return value == null
    }

    fun isNotEmpty(): Boolean {
        return value != null
    }

}

fun <T> T?.asOptional() = Optional<T?>(this)
