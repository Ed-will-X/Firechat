package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.settings

import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.varsel.firechat.R
import com.varsel.firechat.common.Constants
import com.varsel.firechat.data.local.Setting.Setting
import com.varsel.firechat.databinding.FragmentSettingsBinding
import com.varsel.firechat.domain.use_case._util.status_bar.ChangeStatusBarColor_UseCase
import com.varsel.firechat.domain.use_case._util.system.CheckIfNightMode_UseCase
import com.varsel.firechat.domain.use_case._util.theme.SetThemeConfiguration_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.current_user.SignoutUseCase
import com.varsel.firechat.domain.use_case.settings.*
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.viewModel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var parent: SignedinActivity

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    @Inject
    lateinit var signoutUseCase: SignoutUseCase

    @Inject
    lateinit var changeStatusBarColor: ChangeStatusBarColor_UseCase

    @Inject
    lateinit var isNightMode: CheckIfNightMode_UseCase

    @Inject
    lateinit var storeBoolean: StoreSetting_boolean_UseCase

    @Inject
    lateinit var getBoolean: GetSetting_Boolean_UseCase

    @Inject
    lateinit var storeInteger: StoreSetting_Integer_UseCase

    @Inject
    lateinit var getInteger: GetSetting_Integer_UseCase

    @Inject
    lateinit var storeLong: StoreSetting_Long_UseCase

    @Inject
    lateinit var getLong: GetSetting_Long_UseCase

    @Inject
    lateinit var storeString: StoreSetting_string_UseCase

    @Inject
    lateinit var getString: GetSetting_string_UseCase

    @Inject
    lateinit var setThemeConfiguration: SetThemeConfiguration_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        parent = activity as SignedinActivity
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        changeStatusBarColor()

        val view = binding.root

        checkServerConnection().onEach {
            binding.logout.isEnabled = it
        }.launchIn(lifecycleScope)

        getNotificationSettings()
        setNotificationSettingListeners()
        getThemeSettings()
        setThemeSettingsListeners()
        getDataConsumptionSettings()
        setDataConsumptionSettingListeners()

        getPrivacySettings()
        setPrivacySettings()

        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return view
    }

    private fun setScrollVisibilityAnimations(show: Boolean) {
        val transition: Transition = Fade()
        transition.setDuration(600)
        transition.addTarget(R.id.bottom_nav_view)

        TransitionManager.beginDelayedTransition(binding.linearLayoutParentSettings, transition)
        parent.binding.bottomNavView.setVisibility(if (show) View.VISIBLE else View.GONE)
    }

    private fun getNotificationSettings() {
        lifecycleScope.launch {
            val show_chat = getBoolean(SettingKeys_Boolean.SHOW_CHAT_NOTIFICATIONS, parent.datastore)
            val show_group = getBoolean(SettingKeys_Boolean.SHOW_GROUP_NOTIFICATIONS, parent.datastore)
            val show_friend_request = getBoolean(SettingKeys_Boolean.SHOW_FRIEND_REQUEST_NOTIFICATIONS, parent.datastore)
            val show_new_friend = getBoolean(SettingKeys_Boolean.SHOW_NEW_FRIEND_NOTIFICATIONS, parent.datastore)
            val show_group_add = getBoolean(SettingKeys_Boolean.SHOW_GROUP_ADD_NOTIFICATIONS, parent.datastore)

            binding.showChatNotifications.isChecked = show_chat == true || show_chat == null
            binding.showGroupNotifications.isChecked = show_group == true || show_group == null
            binding.showFriendRequestNotifications.isChecked = show_friend_request == true || show_friend_request == null
            binding.showNewFriendNotifications.isChecked = show_new_friend == true || show_new_friend == null
            binding.showGroupAddNotifications.isChecked = show_group_add == true || show_group_add == null
        }
    }

    private fun getThemeSettings() {
        lifecycleScope.launch {
            val darkTheme = getBoolean(SettingKeys_Boolean.DARK_THEME, parent.datastore)
            val override_theme = getBoolean(SettingKeys_Boolean.OVERRIDE_SYSTEM_THEME, parent.datastore)

            if(override_theme == true) {
                binding.darkMode.isEnabled = false
            }
            binding.darkMode.isChecked = darkTheme == true
            binding.overrideSystemTheme.isChecked = override_theme == true
        }
    }

    private fun getPrivacySettings() {
        lifecycleScope.launch {
            val show_last_seen = getBoolean(SettingKeys_Boolean.SHOW_LAST_SEEN, parent.datastore)

            binding.showLastSeen.isChecked = show_last_seen == true || show_last_seen == null
            setDisabledSwitch()

        }
    }

    private fun setPrivacySettings() {
        binding.showLastSeen.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                // TODO: If unchecked, check if last updated is less than 24h before enabling
                if(!isChecked) {
                    setDisabledSwitch()
                } else {
                    storeLong(SettingProps_Long.LAST_SEEN_LAST_ENABLED, System.currentTimeMillis(), parent.datastore)
                }
                storeBoolean(SettingKeys_Boolean.SHOW_LAST_SEEN, isChecked, parent.datastore)
            }
        }
    }

    private fun setDisabledSwitch() {
        lifecycleScope.launch {
            val lastSeenStamp = getLong(SettingProps_Long.LAST_SEEN_LAST_ENABLED, parent.datastore)
            if (lastSeenStamp != null && lastSeenStamp - System.currentTimeMillis() < Constants.LAST_SEEN_REFRESH) {
                binding.showLastSeen.isEnabled = false
            } else if (lastSeenStamp == null) {
                binding.showLastSeen.isEnabled = true
            }
        }
    }

    private fun getDataConsumptionSettings() {
        lifecycleScope.launch {
            val auto_download_image_message = getBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_IMAGE_MESSAGE, parent.datastore)
            val auto_download_video_message = getBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_VIDEO_MESSAGE, parent.datastore)
            val auto_download_gif_message = getBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_GIF_MESSAGE, parent.datastore)
            val auto_download_audio_message = getBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_AUDIO_MESSAGE, parent.datastore)
            val public_post_auto_download_limit = getInteger(
                SettingKeys_Integer.PUBLIC_POST_AUTO_DOWNLOAD_LIMIT,
                parent.datastore
            )

            binding.autoDownloadImageMessage.isChecked = auto_download_image_message == true || auto_download_image_message == null
            binding.autoDownloadVideoMessage.isChecked = auto_download_video_message == true
            binding.autoDownloadGifMessage.isChecked = auto_download_gif_message == true || auto_download_gif_message == null
            binding.autoDownloadAudioMessage.isChecked = auto_download_audio_message == true
            binding.publicPostDownloadCount.setText(public_post_auto_download_limit.toString())
        }
    }

    private fun setNotificationSettingListeners() {
        binding.showChatNotifications.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.SHOW_CHAT_NOTIFICATIONS, isChecked, parent.datastore)
            }
        }

        binding.showGroupNotifications.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.SHOW_GROUP_NOTIFICATIONS, isChecked, parent.datastore)
            }
        }

        binding.showFriendRequestNotifications.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.SHOW_FRIEND_REQUEST_NOTIFICATIONS, isChecked, parent.datastore)
            }
        }

        binding.showNewFriendNotifications.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.SHOW_NEW_FRIEND_NOTIFICATIONS, isChecked, parent.datastore)
            }
        }

        binding.showGroupAddNotifications.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.SHOW_GROUP_ADD_NOTIFICATIONS, isChecked, parent.datastore)
            }
        }
    }

    private fun setThemeSettingsListeners() {
        binding.darkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.DARK_THEME, isChecked, parent.datastore)
                setThemeConfiguration(parent.datastore, lifecycleScope)
            }
        }

        binding.overrideSystemTheme.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.OVERRIDE_SYSTEM_THEME, isChecked, parent.datastore)
                binding.darkMode.isEnabled = !isChecked

                setThemeConfiguration(parent.datastore, lifecycleScope)
            }
        }
    }

    private fun setDataConsumptionSettingListeners() {
        binding.autoDownloadImageMessage.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_IMAGE_MESSAGE, isChecked, parent.datastore)
            }
        }

        binding.autoDownloadVideoMessage.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_VIDEO_MESSAGE, isChecked, parent.datastore)
            }
        }

        binding.autoDownloadGifMessage.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_GIF_MESSAGE, isChecked, parent.datastore)
            }
        }

        binding.autoDownloadAudioMessage.setOnCheckedChangeListener { buttonView, isChecked ->
            lifecycleScope.launch {
                storeBoolean(SettingKeys_Boolean.AUTO_DOWNLOAD_AUDIO_MESSAGE, isChecked, parent.datastore)
            }
        }

        binding.publicPostDownloadCount.doAfterTextChanged {
            lifecycleScope.launch {
                try {
                    storeInteger(SettingKeys_String.PUBLIC_POST_AUTO_DOWNLOAD_LIMIT, binding.publicPostDownloadCount.text.toString().toInt(), parent.datastore)
                } catch (e: Exception) {
                    Log.d("LLL", "${e}")
                }
            }
        }
    }

    private fun changeStatusBarColor(){
        if(isNightMode(parent)){
            changeStatusBarColor(R.color.black, false, parent)
        } else {
            changeStatusBarColor(R.color.white, true, parent)
        }
    }

    private fun setAccountBindings(setting: Setting){
        binding.editProfile.setOnClickListener {
            navigateToEditProfile()
        }
    }

    private fun setNotificationBindings(setting: Setting){
        binding.showChatNotifications.isChecked = setting.show_chat_notifications
        binding.showGroupNotifications.isChecked = setting.show_group_notifications
        binding.showFriendRequestNotifications.isChecked = setting.show_friend_request_notifications
        binding.showNewFriendNotifications.isChecked = setting.show_new_friend_notifications
        binding.showGroupAddNotifications.isChecked = setting.show_group_add_notifications
    }

    private fun setDataConsumptionBindings(setting: Setting){
        binding.autoDownloadImageMessage.isChecked = setting.auto_download_image_message
        binding.autoDownloadVideoMessage.isChecked = setting.auto_download_video_message
        binding.autoDownloadGifMessage.isChecked = setting.auto_download_gif_message
        binding.autoDownloadAudioMessage.isChecked = setting.auto_download_audio_message
        binding.publicPostDownloadCount.setText(setting.public_post_auto_download_limit.toString())
    }

    private fun setThemeBindings(setting: Setting){
        binding.darkMode.isChecked = setting.getDarkMode()
        binding.overrideSystemTheme.isChecked = setting.getOverrideSystemTheme()

        if(setting.getOverrideSystemTheme()){
            binding.darkMode.isEnabled = false
        }
    }

    private fun navigateToEditProfile(){
        val action = SettingsFragmentDirections.actionSettingsFragmentToEditProfilePage()
        findNavController().navigate(action)
    }

    private fun showLogoutConfirmationDialog(){
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.logout_dialog_title))
                .setMessage(getString(R.string.logout_dialog_message))
                .setCancelable(true)
                .setNegativeButton (getString(R.string.logout_dialog_no)){ _, _ ->

                }
                .setPositiveButton(getString(R.string.logout_dialog_yes)){ _, _ ->
                    activity?.let { it1 -> settingsViewModel.logout(it1, context) }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}