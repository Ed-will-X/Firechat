package com.varsel.firechat.domain.use_case.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import javax.inject.Inject

class StoreSetting_Integer_UseCase @Inject constructor(

) {
    operator suspend fun invoke(key: String, value: Int, datastore: DataStore<Preferences>) {
        val datastoreKey = preferencesKey<Int>(key)

        datastore.edit { settings ->
            settings[datastoreKey] = value
        }
    }
}