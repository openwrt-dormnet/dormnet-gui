package io.github.sgpublic.dormnet.core

import androidx.datastore.core.FileStorage
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import java.io.File

internal actual fun createDataStoreStorage(name: String): Storage<Preferences> {
    return FileStorage(
        serializer = PreferencesFileSerializer,
    ) {
        File(CompatPath(name).toString())
    }
}
