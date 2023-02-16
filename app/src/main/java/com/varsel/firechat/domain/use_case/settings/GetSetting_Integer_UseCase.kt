package com.varsel.firechat.domain.use_case.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetSetting_Integer_UseCase @Inject constructor(

) {
    operator suspend fun invoke(key: String, datastore: DataStore<Preferences>): Int? {
        val datastoreKey = preferencesKey<Int>(key)
        val preferences = datastore.data.first()

        return preferences[datastoreKey]
    }
}