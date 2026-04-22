package io.github.sgpublic.dormnet.core

import java.io.File

actual fun basePath(): String {
    return File(System.getProperty("user.dir"), "data").absolutePath
}
