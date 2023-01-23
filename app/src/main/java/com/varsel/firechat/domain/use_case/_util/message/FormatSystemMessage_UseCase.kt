package com.varsel.firechat.domain.use_case._util.message

import com.varsel.firechat.R
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.SystemMessageType
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class FormatSystemMessage_UseCase @Inject constructor(
    val formatPerson: FormatPerson_UseCase,
    val formatStampChatsPage: FormatTimestampChatsPage_UseCase,
    val firebase: FirebaseRepository
) {
    operator fun invoke(message: Message, activity: SignedinActivity, afterCallback: (message: String)-> Unit){
        val currentUser = activity.firebaseAuth.currentUser!!.uid
        if(message.messageUID == SystemMessageType.GROUP_REMOVE){
            val messageArr: Array<String> = message.message.split(" ").toTypedArray()

            firebase.getFirebaseInstance().getUserSingle(messageArr[0], { remover ->
                firebase.getFirebaseInstance().getUserSingle(messageArr[1], { removed ->
//                    afterCallback("${if (remover.userUID == currentUser) "You" else "${remover.name}"} removed ${if(removed.userUID == currentUser) "You" else "${removed.name}"}")
                    afterCallback(activity.getString(R.string.group_removed, if (remover.userUID == currentUser) "You" else "${remover.name}", if(removed.userUID == currentUser) "You" else "${removed.name}"))
                }, {})
            }, {})
            // TODO: Add lone return
        }

        if(message.messageUID == SystemMessageType.GROUP_ADD){
            val users: Array<String> = message.message.split(" ").toTypedArray()
            firebase.getFirebaseInstance().getUserSingle(users[0], {
                if (users.size < 3) {
                    formatPerson(it.userUID, activity, {
                        afterCallback(activity.getString(R.string.group_add_second_person_singular))
                    }, {
                        afterCallback(
                            activity.getString(
                                R.string.group_add_third_person_singular,
                                it.name
                            )
                        )
                    })
                } else {
                    formatPerson(it.userUID, activity, {
                        afterCallback(
                            activity.getString(
                                R.string.group_add_second_person,
                                users.size - 1
                            )
                        )
                    }, {
                        afterCallback(
                            activity.getString(
                                R.string.group_add_third_person,
                                it.name,
                                users.size - 1
                            )
                        )
                    })
                }
            }, {})
            // TODO: Add lone return statement
        }

        firebase.getFirebaseInstance().getUserSingle(message.message, {
            if(message.messageUID == SystemMessageType.GROUP_CREATE){
                formatPerson(it.userUID, activity, {
                    afterCallback(
                        activity.getString(
                            R.string.group_create_second_person,
                            formatStampChatsPage(message.time.toString())
                        )
                    )
                }, {
                    afterCallback(
                        activity.getString(
                            R.string.group_create_third_person,
                            it.name,
                            formatStampChatsPage(message.time.toString())
                        )
                    )
                })
            }
            else if(message.messageUID == SystemMessageType.NOW_ADMIN){
                formatPerson(it.userUID, activity, {
                    afterCallback(activity.getString(R.string.now_admin_second_person))
                }, {
                    afterCallback(activity.getString(R.string.now_admin_third_person, it.name))
                })
            }
            else if(message.messageUID == SystemMessageType.NOT_ADMIN){
                formatPerson(it.userUID, activity, {
                    afterCallback(activity.getString(R.string.not_admin_second_person))
                }, {
                    afterCallback(activity.getString(R.string.not_admin_third_person, it.name))
                })
            }
            else if(message.messageUID == SystemMessageType.GROUP_EXIT){
                formatPerson(it.userUID, activity, {
                    afterCallback(activity.getString(R.string.group_exit_second_person))
                }, {
                    afterCallback(activity.getString(R.string.group_exit_third_person, it.name))
                })
            }
        }, {})
    }

}