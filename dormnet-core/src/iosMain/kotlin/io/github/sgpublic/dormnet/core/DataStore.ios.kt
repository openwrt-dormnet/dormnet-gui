package io.github.sgpublic.dormnet.core

import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import okio.FileSystem
import okio.Path.Companion.toPath

internal actual fun createDataStoreStorage(name: String): Storage<Preferences> {
    return OkioStorage(
        fileSystem = FileSystem.SYSTEM,
        serializer = PreferencesSerializer,
    ) {
        CompatPath(name).toString().toPath()
    }
}
