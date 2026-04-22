package io.github.sgpublic.dormnet.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.sgpublic.dormnet.targets.core.DormnetTargetRegistry
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

    private val School = stringPreferencesKey("schools")
    suspend fun setSchool(school: DormnetTargetRegistry?) {
        print("saved: $school")
        SharedPreference.edit {
            if (school == null) {
                it.remove(School)
            } else {
                it[School] = school.name
            }
        }
    }
    suspend fun getSchool(): DormnetTargetRegistry? {
        val school = SharedPreference.data.first()[School] ?: return null
        return runCatching {
            DormnetTargetRegistry.valueOf(school)
        }.getOrNull()
    }
}
