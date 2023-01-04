package com.varsel.firechat.di

import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.repository.MessageRepository
import com.varsel.firechat.domain.repository.OtherUserRepository
import com.varsel.firechat.domain.repository.ProfileImageRepository
import com.varsel.firechat.domain.use_case._util.search.SetupSearchBarUseCase
import com.varsel.firechat.domain.use_case.current_user.*
import com.varsel.firechat.domain.use_case.message.*
import com.varsel.firechat.domain.use_case.other_user.GetOtherUserRecurrent
import com.varsel.firechat.domain.use_case.other_user.RevokeFriendRequestUseCase
import com.varsel.firechat.domain.use_case.other_user.SendFriendRequestUseCase
import com.varsel.firechat.domain.use_case.other_user.UnfriendUserUseCase
import com.varsel.firechat.domain.use_case.other_user.SearchUsersUseCase
import com.varsel.firechat.domain.use_case.profile_image.*
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

    @Provides
    @Singleton
    fun provideGetFriendsUseCase(repository: CurrentUserRepository): GetFriendsUseCase {
        return GetFriendsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideOpenFriendsUpdateStream(repository: CurrentUserRepository): OpenFriendsUpdateStream {
        return OpenFriendsUpdateStream(repository)
    }

    @Provides
    @Singleton
    fun provideSetupSearchbarUseCase(): SetupSearchBarUseCase {
        return SetupSearchBarUseCase()
    }

    @Provides
    @Singleton
    fun provideGetCurrentUserSingle(repository: CurrentUserRepository): GetCurrentUserSingleUseCase {
        return GetCurrentUserSingleUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetChatRoomsRecurrent(repository: MessageRepository): GetChatRoomsRecurrentUseCase {
        return GetChatRoomsRecurrentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInitialiseChatRoomStream(repository: MessageRepository): InitialiseChatRoomsStreamUseCase {
        return InitialiseChatRoomsStreamUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateGroup(repository: MessageRepository) : CreateGroupUseCase {
        return CreateGroupUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAppendGroupId(repository: MessageRepository): AppendGroupIdToUserUseCase {
        return AppendGroupIdToUserUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetUserId(repository: CurrentUserRepository) : GetCurrentUserIdUseCase {
        return GetCurrentUserIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInitialiseGroupRoomStream(repository: MessageRepository): InitialiseGroupRoomsStreamUseCase {
        return InitialiseGroupRoomsStreamUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetGroupRoomsRecurrent(repository: MessageRepository): GetGroupRoomsRecurrentUseCase {
        return GetGroupRoomsRecurrentUseCase(repository)
    }

    @Provides
    @Singleton
    fun getOtherUserProfileImage(repository: ProfileImageRepository): GetOtherUserProfileImageUseCase {
        return GetOtherUserProfileImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun setProfilePicUseCase(): SetProfilePicUseCase {
        return SetProfilePicUseCase()
    }

    @Provides
    @Singleton
    fun getGroupImage(repository: ProfileImageRepository): GetGroupImageUseCase {
        return GetGroupImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun getProfileImageUseCase(repository: ProfileImageRepository): GetCurrentUserProfileImageUseCase {
        return GetCurrentUserProfileImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveGroupImage(repository: ProfileImageRepository): RemoveGroupImageUseCase {
        return RemoveGroupImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveProfileImage(repository: ProfileImageRepository) : RemoveProfileImageUseCase {
        return RemoveProfileImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUploadGroupImage(repository: ProfileImageRepository): UploadGroupImageUseCase {
        return UploadGroupImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUploadProfileImage(repository: ProfileImageRepository): UploadProfileImageUseCase {
        return UploadProfileImageUseCase(repository)
    }
}