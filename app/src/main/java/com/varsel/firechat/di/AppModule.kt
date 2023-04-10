package com.varsel.firechat.di

import com.varsel.firechat.data.local.Image.ImageDao
import com.varsel.firechat.data.local.ReadReceipt.ReadReceiptDao
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.repository.ChatImageRepositoryImpl
import com.varsel.firechat.data.repository.FirebaseRepositoryImpl
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import com.varsel.firechat.data.repository.ReadReceiptRepositoryImpl
import com.varsel.firechat.domain.repository.*
import com.varsel.firechat.domain.use_case._util.AddRecyclerViewSeparator_UseCase
import com.varsel.firechat.domain.use_case._util.animation.ChangeDialogDimAmountUseCase
import com.varsel.firechat.domain.use_case._util.animation.ChangeIconColorUseCase
import com.varsel.firechat.domain.use_case._util.animation.Rotate90UseCase
import com.varsel.firechat.domain.use_case._util.image.SetOverlayBindings_UseCase
import com.varsel.firechat.domain.use_case._util.message.*
import com.varsel.firechat.domain.use_case._util.notification.CreateNotificationMessageChannel_UseCase
import com.varsel.firechat.domain.use_case._util.notification.SendNotificationMessage_UseCase
import com.varsel.firechat.domain.use_case._util.search.SetupSearchBarUseCase
import com.varsel.firechat.domain.use_case._util.status_bar.ChangeStatusBarColor_UseCase
import com.varsel.firechat.domain.use_case._util.status_bar.SetStatusBarVisibility_UseCase
import com.varsel.firechat.domain.use_case._util.string.Truncate_UseCase
import com.varsel.firechat.domain.use_case._util.system.CheckIfNightMode_UseCase
import com.varsel.firechat.domain.use_case._util.theme.SetThemeConfiguration_UseCase
import com.varsel.firechat.domain.use_case._util.user.*
import com.varsel.firechat.domain.use_case.auth.SignUp_UseCase
import com.varsel.firechat.domain.use_case.auth.ValidateSignIn_UseCase
import com.varsel.firechat.domain.use_case.auth.ValidateSignUp_UseCase
import com.varsel.firechat.domain.use_case.camera.CheckIfCameraPermissionGranted_UseCase
import com.varsel.firechat.domain.use_case.camera.OpenCamera_UseCase
import com.varsel.firechat.domain.use_case.camera.RequestCameraPermission_UseCase
import com.varsel.firechat.domain.use_case.chat_image.*
import com.varsel.firechat.domain.use_case.chat_image.UploadChatImage_UseCase
import com.varsel.firechat.domain.use_case.chat_room.*
import com.varsel.firechat.domain.use_case.current_user.*
import com.varsel.firechat.domain.use_case.group_chat.*
import com.varsel.firechat.domain.use_case.image.*
import com.varsel.firechat.domain.use_case.last_online.CheckLastOnline_UseCase
import com.varsel.firechat.domain.use_case.last_online.UpdateLastOnline_UseCase
import com.varsel.firechat.domain.use_case.message.*
import com.varsel.firechat.domain.use_case.other_user.*
import com.varsel.firechat.domain.use_case.profile_image.*
import com.varsel.firechat.domain.use_case.public_post.*
import com.varsel.firechat.domain.use_case.read_receipt.StoreChatReceipt_UseCase
import com.varsel.firechat.domain.use_case.read_receipt.StoreGroupReceipt_UseCase
import com.varsel.firechat.domain.use_case.read_receipt_temp.FetchReceipt_UseCase
import com.varsel.firechat.domain.use_case.read_receipt_temp.StoreReceipt_UseCase
import com.varsel.firechat.domain.use_case.recent_search.AddToRecentSearch_UseCase
import com.varsel.firechat.domain.use_case.settings.*
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
    fun provideSetOverlayBindings(formatstampmessageUsecase: FormatStampMessage_UseCase, statusbarvisibilityUsecase: SetStatusBarVisibility_UseCase): SetOverlayBindings_UseCase {
        return SetOverlayBindings_UseCase(formatstampmessageUsecase, statusbarvisibilityUsecase)
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
    fun provideuploadChatImage(encodeuriUsecase: EncodeUri_UseCase, generateuidUsecase: GenerateUid_UseCase, firebaseRepository: FirebaseRepository): UploadChatImage_UseCase {
        return UploadChatImage_UseCase(encodeuriUsecase, generateuidUsecase, firebaseRepository)
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
    fun provideFormatSystemMessage(formatpersonUsecase: FormatPerson_UseCase, formatStampChatsPage_UseCase: FormatTimestampChatsPage_UseCase, firebaseRepository: FirebaseRepository): FormatSystemMessage_UseCase {
        return FormatSystemMessage_UseCase(formatpersonUsecase, formatStampChatsPage_UseCase, firebaseRepository)
    }

    @Provides
    @Singleton
    fun provideSortMessages(): SortMessages_UseCase {
        return SortMessages_UseCase()
    }

    @Provides
    @Singleton
    fun provideRemodeGroupImage(repository: ProfileImageRepository): RemoveGroupImage_UseCase {
        return RemoveGroupImage_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideReadReceiptRepository(dao: ReadReceiptDao): ReadReceiptRepository {
        return ReadReceiptRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideFetchReceipt(repository: ReadReceiptRepository): FetchReceipt_UseCase {
        return FetchReceipt_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideStoreReceipt(repository: ReadReceiptRepository): StoreReceipt_UseCase {
        return StoreReceipt_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddToRecentSearch(repository: CurrentUserRepository): AddToRecentSearch_UseCase {
        return AddToRecentSearch_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddGrouptoFavorites(repository: CurrentUserRepository): AddGroupToFavorites_UseCase {
        return AddGroupToFavorites_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveGroupFromFavorites(repository: CurrentUserRepository): RemoveGroupFromFavorites_UseCase {
        return RemoveGroupFromFavorites_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAcceptFriendRequest(repository: OtherUserRepository): AcceptFriendRequest_UseCase {
        return AcceptFriendRequest_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRejectFriendRequest(repository: OtherUserRepository): RejectFriendRequest_UseCase {
        return RejectFriendRequest_UseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFreindRequestsUseCase(repository: FirebaseRepository, sort: SortByTimestamp_UseCase): GetFriendRequests_UseCase {
        return GetFriendRequests_UseCase(repository, sort)
    }

    @Provides
    @Singleton
    fun provideValidateSignup(): ValidateSignUp_UseCase {
        return ValidateSignUp_UseCase()
    }

    @Provides
    @Singleton
    fun provideValidateSignin(): ValidateSignIn_UseCase {
        return ValidateSignIn_UseCase()
    }

    @Provides
    @Singleton
    fun provideDoesChatRoomExist(): DoesChatRoomExist_UseCase {
        return DoesChatRoomExist_UseCase()
    }

    @Provides
    @Singleton
    fun provideFindChatRoom(): FindChatRoom_UseCase {
        return FindChatRoom_UseCase()
    }

    @Provides
    @Singleton
    fun provideChangeStatusBarColor(): ChangeStatusBarColor_UseCase {
        return ChangeStatusBarColor_UseCase()
    }

    @Provides
    @Singleton
    fun provideSetStatusBarVisibility(): SetStatusBarVisibility_UseCase {
        return SetStatusBarVisibility_UseCase()
    }

    @Provides
    @Singleton
    fun provideCheckIfNightMode_UseCase(): CheckIfNightMode_UseCase {
        return CheckIfNightMode_UseCase()
    }

    @Provides
    @Singleton
    fun provideAddRecyclerViewSeparator(): AddRecyclerViewSeparator_UseCase {
        return AddRecyclerViewSeparator_UseCase()
    }

    @Singleton
    @Provides
    fun provideGetSetting_boolean(): GetSetting_Boolean_UseCase {
        return GetSetting_Boolean_UseCase()
    }

    @Singleton
    @Provides
    fun provideGetSetting_string(): GetSetting_string_UseCase {
        return GetSetting_string_UseCase()
    }

    @Singleton
    @Provides
    fun provideGetSetting_integer(): GetSetting_Integer_UseCase {
        return GetSetting_Integer_UseCase()
    }

    @Singleton
    @Provides
    fun provideStoreSetting_integer(): StoreSetting_Integer_UseCase {
        return StoreSetting_Integer_UseCase()
    }

    @Singleton
    @Provides
    fun provideStoreSetting_string(): StoreSetting_string_UseCase {
        return StoreSetting_string_UseCase()
    }

    @Singleton
    @Provides
    fun provideStoreSetting_boolean(): StoreSetting_boolean_UseCase {
        return StoreSetting_boolean_UseCase()
    }

    @Provides
    @Singleton
    fun provideCreateNotificationMessageChannel(): CreateNotificationMessageChannel_UseCase {
        return CreateNotificationMessageChannel_UseCase()
    }

    @Provides
    @Singleton
    fun provideSendNotificationMessage() : SendNotificationMessage_UseCase {
        return SendNotificationMessage_UseCase()
    }

    @Provides
    @Singleton
    fun provideSetThemeConfiguration(getsettingBooleanUsecase: GetSetting_Boolean_UseCase) : SetThemeConfiguration_UseCase {
        return SetThemeConfiguration_UseCase(getsettingBooleanUsecase)
    }

    @Provides
    @Singleton
    fun provideStoreChatReceipt(repository: MessageRepository, checkifreceiptexistsUsecase: HasBeenRead_UseCase) : StoreChatReceipt_UseCase {
        return StoreChatReceipt_UseCase(repository, checkifreceiptexistsUsecase)
    }

    @Provides
    @Singleton
    fun provideStoreGroupReceipt(repository: MessageRepository, checkifreceiptexistsUsecase: HasBeenRead_UseCase) : StoreGroupReceipt_UseCase {
        return StoreGroupReceipt_UseCase(repository, checkifreceiptexistsUsecase)
    }

    @Provides
    @Singleton
    fun provideGetCurrentUserId(getCurrentUserIdUseCase: GetCurrentUserIdUseCase) : HasBeenRead_UseCase {
        return HasBeenRead_UseCase(getCurrentUserIdUseCase)
    }

    @Provides
    @Singleton
    fun provideFormatStampDetail() : FormatStampMessageDetail_UseCase {
        return FormatStampMessageDetail_UseCase()
    }

    @Provides
    @Singleton
    fun provideExcludeCurrentUserId(getCurrentUserIdUseCase: GetCurrentUserIdUseCase) : ExcludeCurrentUserIdFromList_UseCase {
        return ExcludeCurrentUserIdFromList_UseCase(getCurrentUserIdUseCase)
    }

    @Provides
    @Singleton
    fun provideExcludeCurrentUserIdFromMap(getCurrentUserIdUseCase: GetCurrentUserIdUseCase) : ExcludeCurrentUserIdFromMap_UseCase {
        return ExcludeCurrentUserIdFromMap_UseCase(getCurrentUserIdUseCase)
    }

    @Provides
    @Singleton
    fun provideDeleteMessageForAllGroup(messageRepository: MessageRepository) : DeleteMessageForAll_GroupRoom_UseCase {
        return DeleteMessageForAll_GroupRoom_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteMessageForAllChat(messageRepository: MessageRepository) : DeleteMessageForAll_ChatRoom_UseCase {
        return DeleteMessageForAll_ChatRoom_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteMessage_Chat_UseCase(messageRepository: MessageRepository) : DeleteMessage_Chat_UseCase {
        return DeleteMessage_Chat_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteMessage_Group_UseCase(messageRepository: MessageRepository) : DeleteMessage_Group_UseCase {
        return DeleteMessage_Group_UseCase(messageRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateLastOnline(currentUserRepository: CurrentUserRepository) : UpdateLastOnline_UseCase {
        return UpdateLastOnline_UseCase(currentUserRepository)
    }

    @Provides
    @Singleton
    fun provideCheckLastOnline_UseCase(formatStamp: FormatStampMessageDetail_UseCase, otherUserRepository: OtherUserRepository) : CheckLastOnline_UseCase {
        return CheckLastOnline_UseCase(formatStamp, otherUserRepository)
    }
}