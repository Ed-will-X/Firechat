package com.varsel.firechat.di

import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.repository.FirebaseRepositoryImpl
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import com.varsel.firechat.domain.repository.*
import com.varsel.firechat.domain.use_case._util.animation.ChangeDialogDimAmountUseCase
import com.varsel.firechat.domain.use_case._util.animation.ChangeIconColorUseCase
import com.varsel.firechat.domain.use_case._util.animation.Rotate90UseCase
import com.varsel.firechat.domain.use_case._util.message.GenerateUid_UseCase
import com.varsel.firechat.domain.use_case._util.search.SetupSearchBarUseCase
import com.varsel.firechat.domain.use_case.current_user.*
import com.varsel.firechat.domain.use_case.group_chat.GetGroupChatRecurrentUseCase
import com.varsel.firechat.domain.use_case.group_chat.GetGroupParticipantsUseCase
import com.varsel.firechat.domain.use_case.group_chat.InterpolateGroupParticipantsUseCase
import com.varsel.firechat.domain.use_case.group_chat.SendGroupMessage_UseCase
import com.varsel.firechat.domain.use_case.message.*
import com.varsel.firechat.domain.use_case.other_user.*
import com.varsel.firechat.domain.use_case.profile_image.*
import com.varsel.firechat.domain.use_case.public_post.DoesPostExistUseCase
import com.varsel.firechat.domain.use_case.public_post.GetPublicPostUseCase
import com.varsel.firechat.domain.use_case.public_post.SortPublicPostReversedUseCase
import com.varsel.firechat.domain.use_case.public_post.UploadPublicPostImageUseCase
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

    @Provides
    @Singleton
    fun provideDisplayProfileImage(repository: CurrentUserRepository): DisplayProfileImage {
        return DisplayProfileImage(repository)
    }

    @Provides
    @Singleton
    fun provideGetPublicPost(repository: PublicPostRepository): GetPublicPostUseCase {
        return GetPublicPostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUploadPublicPostImage(repository: PublicPostRepository): UploadPublicPostImageUseCase {
        return UploadPublicPostImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDoesPostExist(repository: PublicPostRepository): DoesPostExistUseCase {
        return DoesPostExistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSortPublicPost(): SortPublicPostReversedUseCase {
        return SortPublicPostReversedUseCase()
    }

    @Provides
    @Singleton
    fun provideCheckServerConnection(currentUserRepository: CurrentUserRepository): CheckServerConnectionUseCase {
        return CheckServerConnectionUseCase(currentUserRepository)
    }

    @Provides
    @Singleton
    fun provideChangeDialogDimAmount(): ChangeDialogDimAmountUseCase {
        return ChangeDialogDimAmountUseCase()
    }
    @Provides
    @Singleton
    fun provideChangeIconColor(): ChangeIconColorUseCase {
        return ChangeIconColorUseCase()
    }

    @Provides
    @Singleton
    fun provideRotate90UseCase(): Rotate90UseCase {
        return Rotate90UseCase()
    }

    @Provides
    @Singleton
    fun provideGetGroup(repository: MessageRepository): GetGroupChatRecurrentUseCase {
        return GetGroupChatRecurrentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFirebaseRepo(firebase: Firebase): FirebaseRepository {
        return FirebaseRepositoryImpl(firebase)
    }

    @Provides
    @Singleton
    fun provideGetGroupParticipants(messageRepository: MessageRepository, firebaseRepository: FirebaseRepository): GetGroupParticipantsUseCase {
        return GetGroupParticipantsUseCase(messageRepository, firebaseRepository)
    }

    @Provides
    @Singleton
    fun provideInterpolateGroupParticipants(currentUserRepository: CurrentUserRepository): InterpolateGroupParticipantsUseCase {
        return InterpolateGroupParticipantsUseCase(currentUserRepository)
    }

    @Provides
    @Singleton
    fun provideGenerateUid(): GenerateUid_UseCase {
        return GenerateUid_UseCase()
    }

    @Provides
    @Singleton
    fun provideSendGroupMessage(messageRepository: MessageRepository, currentUserRepository: CurrentUserRepository): SendGroupMessage_UseCase {
        return SendGroupMessage_UseCase(messageRepository, currentUserRepository)
    }

    @Provides
    @Singleton
    fun provideGetListOfUsers(otherUserRepository: OtherUserRepository, firebaseRepository: FirebaseRepository): GetListOfUsers_UseCase {
        return GetListOfUsers_UseCase(otherUserRepository, firebaseRepository)
    }

}