package com.varsel.firechat.domain.use_case.last_online

import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.varsel.firechat.R
import com.varsel.firechat.common.Constants
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.OtherUserRepository
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessageDetail_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page.ChatPageFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class CheckLastOnline_UseCase @Inject constructor(
    val formatStamp: FormatStampMessageDetail_UseCase,
    val otherUserRepository: OtherUserRepository
) {
    operator fun invoke(activity: SignedinActivity, fragment: ChatPageFragment, userId: String, rootView: View, stringCallback: (string: String)-> Unit) {
        var timer: Timer? = null

        otherUserRepository.getUserRecurrent(userId).onEach {
            if(timer != null) {
                timer?.cancel()
            }
            Log.d("LLL", "Listener removed")
            timer = null

            when(it) {
                is Resource.Success -> {
                    if(it.data == null) { return@onEach }

                    val timeDiff = System.currentTimeMillis() - it.data.lastOnline
                    if(it.data.lastOnline == 0L) {
                        stringCallback(activity.getString(R.string.offline))
                    } else if(timeDiff < Constants.LAST_SEEN_REFRESH * 1000) {
                        // Constantly check for subsequent offline possibilities because
                        // the user obj isn't being updated in firebase after this check
                        if(timer == null) {
                            Log.d("LLL", "Listener attached")
                            timer = fixedRateTimer("timer", false, 0L, Constants.LAST_SEEN_REFRESH.toLong() * 1000){
                                val timeDiff_online = System.currentTimeMillis() - it.data.lastOnline

                                Log.d("LLL", "Checked")
                                if(timeDiff_online < Constants.LAST_SEEN_REFRESH * 1000) {
                                    rootView.post {
                                        stringCallback(activity.getString(R.string.online))
                                    }
                                } else {
                                    // Cancelling the timer here because when next last seen is updated,
                                    // the timer will be re-attached
                                    this.cancel()
                                    timer = null
                                    rootView.post {
                                        stringCallback(formatStamp(it.data.lastOnline))
                                    }
                                }
                            }
                        }
//                        else {
//                            timer?.cancel()
//                            Log.d("LLL", "Listener removed")
//                            timer = null
//                            rootView.post {
//                                stringCallback(formatStamp(it.data.lastOnline))
//                            }
//                        }

                    } else if(timeDiff > Constants.LAST_SEEN_REFRESH * 1000) {
                        stringCallback(formatStamp(it.data.lastOnline))
                    }
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {

                }
            }
        }.launchIn(fragment.lifecycleScope)
    }
}