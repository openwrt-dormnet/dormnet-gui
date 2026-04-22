package io.github.sgpublic.dormnet.core

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

fun createDataStore(name: String): DataStore<Preferences> {
    return DataStoreFactory.create(createDataStoreStorage(name))
}

internal expect fun createDataStoreStorage(name: String): Storage<Preferences>
