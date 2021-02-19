package pl.charmas.android.reactivelocation2.sample.ext

import android.location.Location
import io.reactivex.functions.Function

fun Location.text(): String {
    return "$latitude $longitude ($provider $accuracy)"
}