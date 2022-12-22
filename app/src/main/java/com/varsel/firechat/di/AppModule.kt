package com.varsel.firechat.di

import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import com.varsel.firechat.domain.repository.OtherUserRepository
import com.varsel.firechat.domain.use_case.OtherUserUseCase.GetOtherUserRecurrent
import com.varsel.firechat.domain.use_case.OtherUserUseCase.RevokeFriendRequestUseCase
import com.varsel.firechat.domain.use_case.OtherUserUseCase.SendFriendRequestUseCase
import com.varsel.firechat.domain.use_case.OtherUserUseCase.UnfriendUserUseCase
import com.varsel.firechat.domain.use_case.SearchUsersUseCase.SearchUsersUseCase
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
}