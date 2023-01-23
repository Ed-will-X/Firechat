package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentSettingsBinding
import com.varsel.firechat.data.local.Setting.Setting
import com.varsel.firechat.domain.use_case._util.status_bar.ChangeStatusBarColor_UseCase
import com.varsel.firechat.domain.use_case._util.system.CheckIfNightMode_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.current_user.SignoutUseCase
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        parent = activity as SignedinActivity
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        changeStatusBarColor()

        val view = binding.root


//        LifecycleUtils.observeInternetStatus(parent, this, {
//            binding.logout.isEnabled = true
//        }, {
//            binding.logout.isEnabled = false
//        })

        checkServerConnection().onEach {
            binding.logout.isEnabled = it
        }.launchIn(lifecycleScope)

        parent.settingViewModel.settingConfig.observe(viewLifecycleOwner, Observer {
            setNotificationBindings(it)
            setDataConsumptionBindings(it)
            setThemeBindings(it)
            setAccountBindings(it)
        })

        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return view
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