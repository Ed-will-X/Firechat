package com.varsel.firechat.di

import com.varsel.firechat.data.local.Image.ImageDao
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.repository.ChatImageRepositoryImpl
import com.varsel.firechat.data.repository.FirebaseRepositoryImpl
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import com.varsel.firechat.domain.repository.*
import com.varsel.firechat.domain.use_case._util.animation.ChangeDialogDimAmountUseCase
import com.varsel.firechat.domain.use_case._util.animation.ChangeIconColorUseCase
import com.varsel.firechat.domain.use_case._util.animation.Rotate90UseCase
import com.varsel.firechat.domain.use_case._util.image.SetOverlayBindings_UseCase
import com.varsel.firechat.domain.use_case._util.message.*
import com.varsel.firechat.domain.use_case._util.search.SetupSearchBarUseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.user.*
import com.varsel.firechat.domain.use_case.auth.SignUp_UseCase
import com.varsel.firechat.domain.use_case.camera.CheckIfCameraPermissionGranted_UseCase
import com.varsel.firechat.domain.use_case.camera.OpenCamera_UseCase
import com.varsel.firechat.domain.use_case.camera.RequestCameraPermission_UseCase
import com.varsel.firechat.domain.use_case.chat_image.*
import com.varsel.firechat.domain.use_case.chat_room.AppendChatRoom_UseCase
import com.varsel.firechat.domain.use_case.chat_room.AppendParticipants_UseCase
import com.varsel.firechat.domain.use_case.chat_room.SendMessage_UseCase
import com.varsel.firechat.domain.use_case.chat_image.UploadChatImage_UseCase
import com.varsel.firechat.domain.use_case.current_user.*
import com.varsel.firechat.domain.use_case.group_chat.*
import com.varsel.firechat.domain.use_case.image.*
import com.varsel.firechat.domain.use_case.message.*
import com.varsel.firechat.domain.use_case.other_user.*
import com.varsel.firechat.domain.use_case.profile_image.*
import com.varsel.firechat.domain.use_case.public_post.*
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
    fun provideChatImageRepository(firebase: Firebase, imageDao: ImageDao): ChatImageRepository {
        return ChatImageRepositoryImpl(firebase, imageDao)
    }

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
    fun provideSetupSearchbarUseCase(searchlistofusersUsecase: SearchListOfUsers_UseCase): SetupSearchBarUseCase {
        return SetupSearchBarUseCase(searchlistofusersUsecase)
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
    fun provideDisplayProfileImage(repository: CurrentUserRepository, setoverlaybindingsUsecase: SetOverlayBindings_UseCase): DisplayProfileImage {
        return DisplayProfileImage(repository, setoverlaybindingsUsecase)
    }

    @Provides
    @Singleton
    fun provideDisplayChatImage(repository: CurrentUserRepository, setoverlaybindingsUsecase: SetOverlayBindings_UseCase): DisplayChatImage_UseCase {
        return DisplayChatImage_UseCase(repository, setoverlaybindingsUsecase)
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
    fun provideInterpolateGroupParticipants(currentUserRepository: CurrentUserRepository, truncateUsecase: Truncate_UseCase): InterpolateGroupParticipantsUseCase {
        return InterpolateGroupParticipantsUseCase(currentUserRepository, truncateUsecase)
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

    @Provides
    @Singleton
    fun provideExitGroup(messageRepository: MessageRepository): ExitGroup_UseCase {
        return ExitGroup_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideMakeAdmin(messageRepository: MessageRepository): MakeAdminUseCase {
        return MakeAdminUseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveAdmin(messageRepository: MessageRepository): RemoveAdminUseCase {
        return RemoveAdminUseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveFromGroup(messageRepository: MessageRepository): RemoveFromGroup_UseCase {
        return RemoveFromGroup_UseCase(messageRepository)
    }


    @Provides
    @Singleton
    fun provideEditGroup(messageRepository: MessageRepository): EditGroupUseCase {
        return EditGroupUseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideAddGroupMembers(firebaseRepository: FirebaseRepository): AddGroupMembers_UseCase {
        return AddGroupMembers_UseCase(firebaseRepository)
    }

    @Provides
    @Singleton
    fun provideSignOut(currentUserRepository: CurrentUserRepository): SignoutUseCase {
        return SignoutUseCase(currentUserRepository)
    }

    @Provides
    @Singleton
    fun provideGetOtherUserFromParticipants(currentUserRepository: CurrentUserRepository): GetOtherUserFromParticipants_UseCase {
        return GetOtherUserFromParticipants_UseCase(currentUserRepository)
    }

    @Provides
    @Singleton
    fun provideAppendChatroom(messageRepository: MessageRepository): AppendChatRoom_UseCase {
        return AppendChatRoom_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideAppendParticipants(messageRepository: MessageRepository): AppendParticipants_UseCase {
        return AppendParticipants_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideSendMessage(messageRepository: MessageRepository): SendMessage_UseCase {
        return SendMessage_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideSignUp(currentUserRepository: CurrentUserRepository): SignUp_UseCase {
        return SignUp_UseCase(currentUserRepository)
    }



    @Provides
    @Singleton
    fun provideStoreChatImage(chatImageRepository: ChatImageRepository): StoreChatImageUseCase {
        return StoreChatImageUseCase(chatImageRepository)
    }

    @Provides
    @Singleton
    fun provideGetChatImage(chatImageRepository: ChatImageRepository): GetChatImageUseCase {
        return GetChatImageUseCase(chatImageRepository)
    }

    @Provides
    @Singleton
    fun provideCheckChatImageInDb(chatImageRepository: ChatImageRepository): CheckChatImageInDb {
        return CheckChatImageInDb(chatImageRepository)
    }

    @Provides
    @Singleton
    fun provideSetChatImage(): SetChatImageUseCase {
        return SetChatImageUseCase()
    }


    @Provides
    @Singleton
    fun provideSetOverlayBindings(formatstampmessageUsecase: FormatStampMessage_UseCase): SetOverlayBindings_UseCase {
        return SetOverlayBindings_UseCase(formatstampmessageUsecase)
    }

    @Provides
    @Singleton
    fun provideOpenImagePicker(): OpenImagePicker_UseCase {
        return OpenImagePicker_UseCase()
    }

    @Provides
    @Singleton
    fun provideCheckCameraPermission(): CheckIfCameraPermissionGranted_UseCase {
        return CheckIfCameraPermissionGranted_UseCase()
    }


    @Provides
    @Singleton
    fun provideRequestCameraPermission(): RequestCameraPermission_UseCase {
        return RequestCameraPermission_UseCase()
    }

    @Provides
    @Singleton
    fun provideOpenCamera(
        checkifcamerapermissiongrantedUsecase: CheckIfCameraPermissionGranted_UseCase,
        requestcamerapermissionUsecase: RequestCameraPermission_UseCase
    ): OpenCamera_UseCase {
        return OpenCamera_UseCase(checkifcamerapermissiongrantedUsecase, requestcamerapermissionUsecase)
    }

    @Provides
    @Singleton
    fun provideHandleOnActivityResult(encodeimageUsecase: EncodeImage_UseCase): HandleOnActivityResult_UseCase {
        return HandleOnActivityResult_UseCase(encodeimageUsecase)
    }

    @Provides
    @Singleton
    fun provideCheckImageDimensions(): CheckImageDimensions_UseCase {
        return CheckImageDimensions_UseCase()
    }

    @Provides
    @Singleton
    fun provideEncodeImageUseCase(): EncodeImage_UseCase {
        return EncodeImage_UseCase()
    }

    @Provides
    @Singleton
    fun provideResizeImage(): ResizeImageUseCase {
        return ResizeImageUseCase()
    }

    @Provides
    @Singleton
    fun provideuploadChatImage(encodeuriUsecase: EncodeUri_UseCase, generateuidUsecase: GenerateUid_UseCase): UploadChatImage_UseCase {
        return UploadChatImage_UseCase(encodeuriUsecase, generateuidUsecase)
    }

    @Provides
    @Singleton
    fun provideDisplayGroupImage(setoverlaybindingsUsecase: SetOverlayBindings_UseCase): DisplayGroupImage_UseCase {
        return DisplayGroupImage_UseCase(setoverlaybindingsUsecase)
    }

    @Provides
    @Singleton
    fun provideDisplayImageMessage(
        currentUserRepository: CurrentUserRepository,
        setoverlaybindingsUsecase: SetOverlayBindings_UseCase
    ): DisplayImageMessageGroup_UseCase {
        return DisplayImageMessageGroup_UseCase(currentUserRepository, setoverlaybindingsUsecase)
    }

    @Provides
    @Singleton
    fun provideDisplayPublicPostImage(
        setoverlaybindingsUsecase: SetOverlayBindings_UseCase
    ): DisplayPublicPostImage_UseCase {
        return DisplayPublicPostImage_UseCase(setoverlaybindingsUsecase)
    }


    @Provides
    @Singleton
    fun provideSortByTimestamp(): SortByTimestamp_UseCase {
        return SortByTimestamp_UseCase()
    }

    @Provides
    @Singleton
    fun provideGetFirstName(): GetFirstName_UseCase {
        return GetFirstName_UseCase()
    }


    @Provides
    @Singleton
    fun provideSortUsersByName(): SortUsersByName_UseCase {
        return SortUsersByName_UseCase()
    }

    @Provides
    @Singleton
    fun provideSortUsersByNameInGroup(): SortUsersByNameInGroup_UseCase {
        return SortUsersByNameInGroup_UseCase()
    }


    @Provides
    @Singleton
    fun provideSearchListOfUsers(): SearchListOfUsers_UseCase {
        return SearchListOfUsers_UseCase()
    }

    @Provides
    @Singleton
    fun provideSearchListOfUsersAndGroups(): SearchListOfUsersAndGroups_UseCase {
        return SearchListOfUsersAndGroups_UseCase()
    }

    @Provides
    @Singleton
    fun provideTruncate(): Truncate_UseCase {
        return Truncate_UseCase()
    }

    @Provides
    @Singleton
    fun provideGetOtherUserId(getCurrentUserIdUseCase: GetCurrentUserIdUseCase): GetOtherUserId_UseCase {
        return GetOtherUserId_UseCase(getCurrentUserIdUseCase)
    }


    @Provides
    @Singleton
    fun provideFormatStampMessage(): FormatStampMessage_UseCase {
        return FormatStampMessage_UseCase()
    }

    @Provides
    @Singleton
    fun provideFormatTimestampChatsPage(): FormatTimestampChatsPage_UseCase {
        return FormatTimestampChatsPage_UseCase()
    }

    @Provides
    @Singleton
    fun provideSortChats(sortmessagesUsecase: SortMessages_UseCase): SortChats_UseCase {
        return SortChats_UseCase(sortmessagesUsecase)
    }

    @Provides
    @Singleton
    fun provideCalculateTimestampDifferenceLess(): CalculateTimestampDifferenceLess_UseCase {
        return CalculateTimestampDifferenceLess_UseCase()
    }


    @Provides
    @Singleton
    fun provideGetLastMessage(sortmessagesUsecase: SortMessages_UseCase): GetLastMessage_UseCase {
        return GetLastMessage_UseCase(sortmessagesUsecase)
    }

    @Provides
    @Singleton
    fun provideFormatPerson(): FormatPerson_UseCase {
        return FormatPerson_UseCase()
    }

    @Provides
    @Singleton
    fun provideFormatSystemMessage(formatpersonUsecase: FormatPerson_UseCase, formatStampChatsPage_UseCase: FormatTimestampChatsPage_UseCase): FormatSystemMessage_UseCase {
        return FormatSystemMessage_UseCase(formatpersonUsecase, formatStampChatsPage_UseCase)
    }

    @Provides
    @Singleton
    fun provideSortMessages(): SortMessages_UseCase {
        return SortMessages_UseCase()
    }



}