package com.varsel.firechat.domain.use_case.current_user

import android.util.Log
import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OpenFriendsUpdateStream @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(friends: HashMap<String, Long>) : Flow<Response> {
        return currentUserRepository.updateFriends(friends)
    }
}