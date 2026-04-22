package io.github.sgpublic.dormnet.core

import kotlin.io.encoding.Base64

fun String.encodeBase64(): String {
    return Base64.encode(this.encodeToByteArray())
}