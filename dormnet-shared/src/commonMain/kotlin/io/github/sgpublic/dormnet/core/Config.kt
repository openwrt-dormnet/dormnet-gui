package io.github.sgpublic.dormnet.core

import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.sgpublic.dormnet.targets.core.DormnetTargetRegistry


private val School = stringPreferencesKey("schools")
suspend fun Config.setSchool(school: DormnetTargetRegistry?) {
    this {
        if (school == null) {
            it.remove(School)
        } else {
            it[School] = school.name
        }
    }
}
suspend fun Config.getSchool(): DormnetTargetRegistry? {
    val school = Config[School] ?: return null
    return runCatching {
        DormnetTargetRegistry.valueOf(school)
    }.getOrNull()
}
