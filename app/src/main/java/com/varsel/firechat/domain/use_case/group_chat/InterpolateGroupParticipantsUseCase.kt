package com.varsel.firechat.domain.use_case.group_chat

import android.widget.TextView
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import javax.inject.Inject

class InterpolateGroupParticipantsUseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository,
    val truncate: Truncate_UseCase
) {

    operator fun invoke(users: List<User>, textView: TextView){
        val firstnames = mutableListOf<String>()
        val currentUser = currentUserRepository.getCurrentUserId()

        for((i, v) in users.withIndex()){
            if(currentUser == v.userUID){
                continue
            }
            firstnames.add(v.name ?: "")
        }

        val usersInString = firstnames.joinToString(
            separator = ", "
        )

        textView.text = truncate(usersInString, 40)
    }
}