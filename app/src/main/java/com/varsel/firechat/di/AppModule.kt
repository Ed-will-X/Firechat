package com.varsel.firechat.di

import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.repository.OtherUserRepository
import com.varsel.firechat.domain.use_case.current_user.EditUserUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.current_user.OpenCurrentUserCollectionStream
import com.varsel.firechat.domain.use_case.other_user.GetOtherUserRecurrent
import com.varsel.firechat.domain.use_case.other_user.RevokeFriendRequestUseCase
import com.varsel.firechat.domain.use_case.other_user.SendFriendRequestUseCase
import com.varsel.firechat.domain.use_case.other_user.UnfriendUserUseCase
import com.varsel.firechat.domain.use_case.other_user.SearchUsersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSearchUserUserCase(repository: OtherUserRepository): SearchUsersUseCase {
        return SearchUsersUseCase(repository as OtherUserRepositoryImpl)
    }

    @Provides
    @Singleton
    fun provideGetOtherUserRecurrentUseCase(repository: OtherUserRepository): GetOtherUserRecurrent {
        return GetOtherUserRecurrent(repository)
    }

    @Provides
    @Singleton
    fun provideSendFriendRequestUseCase(repository: OtherUserRepository) : SendFriendRequestUseCase {
        return SendFriendRequestUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRevokeRequestUseCase(repository: OtherUserRepository) : RevokeFriendRequestUseCase {
        return RevokeFriendRequestUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUnfriendUserUseCase(repository: OtherUserRepository) : UnfriendUserUseCase {
        return UnfriendUserUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetUserRecurrentUseCase(repository: CurrentUserRepository): GetCurrentUserRecurrentUseCase {
        return GetCurrentUserRecurrentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideOpenCurrentUserCollectionStream(repository: CurrentUserRepository): OpenCurrentUserCollectionStream {
        return OpenCurrentUserCollectionStream(repository)
    }

    @Provides
    @Singleton
    fun provideEditProfileUseCase(repository: CurrentUserRepository): EditUserUseCase {
        return EditUserUseCase(repository)
    }
}