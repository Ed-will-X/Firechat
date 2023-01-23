package com.varsel.firechat.domain.use_case.current_user

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.use_case._util.user.SortByTimestamp_UseCase
import javax.inject.Inject

class GetFriendRequests_UseCase @Inject constructor(
    val firebase: FirebaseRepository,
    val sortByTimestamp: SortByTimestamp_UseCase
) {
    operator fun invoke(requests: HashMap<String, Long>, requestsCallback: (users: List<User>)-> Unit) {
        val users = mutableListOf<User>()
        val sortedMap = sortByTimestamp(requests.toSortedMap())

        for(i in sortedMap.keys){
            firebase.getFirebaseInstance().getUserSingle(i, {
                users.add(it)
            }, {
                requestsCallback(users.reversed())
            })
        }
    }
}