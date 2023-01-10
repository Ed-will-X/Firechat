package com.varsel.firechat.domain.repository

import com.varsel.firechat.data.remote.Firebase

interface FirebaseRepository {
    fun getFirebaseInstance(): Firebase
}