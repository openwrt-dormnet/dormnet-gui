package io.github.sgpublic.dormnet.core

actual fun basePath(): String {
    return useContext { filesDir.absolutePath }
}
