package io.github.sgpublic.dormnet.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

object Config {
    private val SharedPreference: DataStore<Preferences> by lazy {
        createDataStore("config")
    }

    suspend operator fun invoke(block: suspend (MutablePreferences) -> Unit) {
        SharedPreference.edit {
            block(it)
        }
    }

    suspend operator fun <T> get(key: Preferences.Key<T>): T? {
        return SharedPreference.data.first()[key]
    }
}
