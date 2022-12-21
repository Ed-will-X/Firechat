package com.varsel.firechat.domain.use_case.SearchUsersUseCase

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    val repositoryImpl: OtherUserRepositoryImpl
) {
    operator fun invoke(query: String): Flow<List<User>> {
        return repositoryImpl.queryUsers(query)
    }
}