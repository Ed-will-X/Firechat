package com.varsel.firechat.domain.use_case.last_online

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.varsel.firechat.common.Constants
import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class UpdateLastOnline_UseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(activity: SignedinActivity, secondInterval: Int = Constants.LAST_SEEN_REFRESH): Timer {
        val timer = fixedRateTimer("timer", false, 0L, secondInterval.toLong() * 1000) {
            currentUserRepository.updateLastOnline().onEach {
                when(it) {
                    is Response.Success -> {
//                        Log.d("LLL", "Last seen updated")
                    }
                    is Response.Loading -> {
//                        Log.d("LLL", "Last seen updating")
                    }
                    is Response.Fail -> {
//                        Log.d("LLL", "Last seen update failed")
                    }
                }
            }.launchIn(activity.lifecycleScope)
        }

        return timer
    }
}