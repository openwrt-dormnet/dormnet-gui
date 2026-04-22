package io.github.sgpublic.dormnet.core

import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun basePath(): String {
    val paths = NSSearchPathForDirectoriesInDomains(
        directory = NSLibraryDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    )
    return paths.first() as String
}
