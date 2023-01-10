package com.varsel.firechat.data.repository

import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.FirebaseRepository
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    val firebase: Firebase
): FirebaseRepository {

    override fun getFirebaseInstance(): Firebase {
        return firebase
    }
}