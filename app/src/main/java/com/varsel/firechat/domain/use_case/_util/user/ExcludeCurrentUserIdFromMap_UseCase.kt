package com.varsel.firechat.domain.use_case._util.user

import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import javax.inject.Inject

class ExcludeCurrentUserIdFromMap_UseCase @Inject constructor(
    val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    operator fun <T> invoke(userIds: HashMap<String, T>): HashMap<String, T> {
        val newMaps = hashMapOf<String, T>()
        for(i in userIds) {
            if(i.key == getCurrentUserIdUseCase()) {
                continue
            } else {
                newMaps.put(i.key, i.value)
            }
        }

        return newMaps
    }
}