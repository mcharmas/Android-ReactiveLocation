package pl.charmas.android.reactivelocation2.sample.ext

import android.location.Address

fun Address.addressToString(): String {
    var addressLines = ""
    for (i in 0..maxAddressLineIndex) {
        addressLines += """
                ${getAddressLine(i)}

                """.trimIndent()
    }
    return addressLines
}