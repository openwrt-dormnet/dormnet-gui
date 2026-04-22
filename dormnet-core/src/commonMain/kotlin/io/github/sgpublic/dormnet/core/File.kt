package io.github.sgpublic.dormnet.core

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

expect fun basePath(): String

fun CompatPath(vararg part: String): Path {
    return Path(basePath(), *part)
}

operator fun Path.div(path: Path): Path {
    if (path.isAbsolute) {
        return path
    }
    return Path(this, path.toString())
}

operator fun Path.div(part: String): Path {
    return Path(this, part)
}

fun Path.source(): Source {
    if (isAbsolute) {
        throw IllegalStateException("Absolute path is not allowed")
    }
    return SystemFileSystem.source(Path(basePath()) / this).buffered()
}

fun Path.sink(append: Boolean = false): Sink {
    if (isAbsolute) {
        throw IllegalStateException("Absolute path is not allowed")
    }
    return SystemFileSystem.sink(Path(basePath()) / this, append = append).buffered()
}
