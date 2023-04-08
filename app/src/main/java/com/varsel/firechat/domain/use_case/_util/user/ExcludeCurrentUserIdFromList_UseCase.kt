package com.varsel.firechat.domain.use_case._util.user

import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import javax.inject.Inject

class ExcludeCurrentUserIdFromList_UseCase @Inject constructor(
    val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    operator fun invoke(userIds: List<String>): List<String> {
        val newIDs = mutableListOf<String>()

        for(i in userIds) {
            if(i == getCurrentUserIdUseCase()) {
                continue
            } else {
                newIDs.add(i)
            }
        }

        return newIDs

    }
}