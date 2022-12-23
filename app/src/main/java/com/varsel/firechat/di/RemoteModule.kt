package com.varsel.firechat.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.repository.CurrentUserRepositoryImpl
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.repository.OtherUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideOtherUserRepository(firebase: Firebase, databaseReference: DatabaseReference): OtherUserRepository {
        return OtherUserRepositoryImpl(firebase, databaseReference)
    }

    @Provides
    @Singleton
    fun provideCurrentUserRepository(firebase: Firebase): CurrentUserRepository {
        return CurrentUserRepositoryImpl(firebase)
    }

    @Provides
    @Singleton
    fun provideFirebase(mDbRef: DatabaseReference, mAuth: FirebaseAuth, firebaseStorage: FirebaseStorage
    ) : Firebase {
        return Firebase(mDbRef, mAuth, firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth() : FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase() : DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage() : FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

}