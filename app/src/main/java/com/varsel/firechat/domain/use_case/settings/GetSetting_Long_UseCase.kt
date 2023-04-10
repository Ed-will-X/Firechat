package com.varsel.firechat.domain.use_case.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import kotlinx.coroutines.flow.first

class GetSetting_Long_UseCase {
    operator suspend fun invoke(key: String, datastore: DataStore<Preferences>): Long? {
        val datastoreKey = preferencesKey<Long>(key)
        val preferences = datastore.data.first()

        return preferences[datastoreKey]
    }
}