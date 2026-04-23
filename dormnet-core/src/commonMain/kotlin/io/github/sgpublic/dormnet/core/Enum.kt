package io.github.sgpublic.dormnet.core

inline fun <reified T: Enum<T>> String?.parse(block: (T) -> Unit) {
    if (this == null) {
        return
    }
    try {
        block(enumValueOf<T>(this))
    } catch (_: Exception) { }
}