package com.varsel.firechat.domain.use_case.current_user

import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class GetFriendsUseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(): MutableStateFlow<Resource<List<User>>> {
        return currentUserRepository.fetchFriends()
    }
}