package com.varsel.firechat.presentation.signedIn.fragments.screens.friend_list

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendListBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ExtensionFunctions.Companion.hideKeyboard
import com.varsel.firechat.utils.ExtensionFunctions.Companion.showKeyboard
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.utils.SearchUtils
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.utils.gestures.FriendsSwipeGesture
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedIn.adapters.FriendListAdapter
import com.varsel.firechat.presentation.viewModel.FriendListFragmentViewModel
import com.varsel.firechat.presentation.viewModel.SortTypes
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException

@AndroidEntryPoint
class FriendListFragment : Fragment() {
    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!
    private var adapter: FriendListAdapter? = null
    private lateinit var parent: SignedinActivity
    private lateinit var viewModel: FriendListFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendListBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(this).get(FriendListFragmentViewModel::class.java)

        parent = activity as SignedinActivity

        collectState()

        setupFriendsAdapter()
        setupSearchBar()
        setClickListeners()
        setupSortDialogOverlay(viewModel.state.value.friends.toList())

        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {

        }
    }

    private fun setupSearchBar() {
        viewModel.setupSearchBar(binding, parent, this, {
            // resets the searchbar visibility
            viewModel.isSearchBarVisible.value = false

            viewModel.isSearchBarVisible.observe(viewLifecycleOwner, Observer {
                // Sets the visibilities of the search bar and the soft keyboard
                if(it){
                    binding.searchBar.visibility = View.VISIBLE
                    binding.searchBox.requestFocus()
                    requireContext().showKeyboard()
                } else {
                    binding.searchBar.visibility = View.GONE
                    hideKeyboard()
                }
            })

            binding.searchIcon.setOnClickListener {
                viewModel.toggleSearchBarVisible()
            }
        }, {
            submitListToAdapter(it)
        })
//
//        SearchUtils.setupSearchBar(
//            binding.clearText,
//            binding.searchBox,
//            this,
//            binding.noFriends,
//            binding.noMatch,
//            binding.allFriendsRecyclerView,
//            parent.firebaseViewModel.friends,
//            {
//                // resets the searchbar visibility
//                viewModel.isSearchBarVisible.value = false
//
//                viewModel.isSearchBarVisible.observe(viewLifecycleOwner, Observer {
//                    // Sets the visibilities of the search bar and the soft keyboard
//                    if(it){
//                        binding.searchBar.visibility = View.VISIBLE
//                        binding.searchBox.requestFocus()
//                        requireContext().showKeyboard()
//                    } else {
//                        binding.searchBar.visibility = View.GONE
//                        hideKeyboard()
//                    }
//                })
//
//                binding.searchIcon.setOnClickListener {
//                    viewModel.toggleSearchBarVisible()
//                }
//            }, {
//                submitListToAdapter(it)
//            }
//        )
    }

    private fun setupFriendsAdapter() {
        adapter = FriendListAdapter(parent, this, viewModel, { id, user, base64 ->
            navigateToOtherProfile(id, user, base64)
        }, { profileImage, user ->
            ImageUtils.displayProfilePicture(profileImage, user, parent)
        })
        binding.allFriendsRecyclerView.adapter = adapter

        val swipeGesture = object : FriendsSwipeGesture(parent){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                LifecycleUtils.observeInternetStatus(parent, this@FriendListFragment, {
                    if(direction == ItemTouchHelper.LEFT){
                        if(adapter != null){
                            viewModel.unfriendUser(adapter!!.friends[viewHolder.adapterPosition])
                            removeFromAdapter(adapter!!, viewHolder)
                        }
                    }
                }, {})
            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)

        // Disables swipe if no internet
        LifecycleUtils.observeInternetStatus(parent, this, {
            touchHelper.attachToRecyclerView(binding.allFriendsRecyclerView)
        }, {
            touchHelper.attachToRecyclerView(null)
        })
    }

    private fun setClickListeners() {
        binding.addFriendsClickable.setOnClickListener {
            navigateToAddfriends()
        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }
    }

    private fun navigateToOtherProfile(userId: String, user: User, base64: String?) {
        try {
            val action = FriendListFragmentDirections.actionFriendListFragmentToOtherProfileFragment(userId)
            view?.findNavController()?.navigate(action)

//            parent.firebaseViewModel.selectedUser.value = user
            parent.profileImageViewModel.selectedOtherUserProfilePic.value = base64
        } catch (e: IllegalArgumentException) { }
    }

    private fun addFriendsToAdapter_initial(friends: List<User>?, sortType: Int){

        // TODO: Fix "java.lang.UnsupportedOperationException" here
        adapter?.friends = listOf()

        if(friends != null && friends.isNotEmpty()){
            setFriendCount(friends)

            if(sortType == SortTypes.ASCENDING){
                val sorted = UserUtils.sortUsersByName(friends)

                if(adapter != null){
                    adapter!!.friends = sorted.toList()
                    adapter!!.notifyDataSetChanged()
                }
            } else if(sortType == SortTypes.DESCENDING){
                val sorted = UserUtils.sortUsersByName(friends).reversed()

                if(adapter != null){
                    adapter!!.friends = sorted
                    adapter!!.notifyDataSetChanged()
                }
            } else if(sortType == SortTypes.DEFAULT){
                if(adapter != null){
                    adapter!!.friends = friends
                    adapter!!.notifyDataSetChanged()
                }
            } else if(sortType == SortTypes.OLDEST){
                if(adapter != null){
                    adapter!!.friends = friends
                    adapter!!.notifyDataSetChanged()
                }
            } else if(sortType == SortTypes.NEWEST){
                if(adapter != null){
                    adapter!!.friends = friends.reversed()
                    adapter!!.notifyDataSetChanged()
                }
            }
        } else {
            setFriendCount(0)

        }
    }

    private fun setupSortDialogOverlay(friends: List<User>){
        viewModel.setBinding(binding)

        viewModel.changeSortMethod(SortTypes.ASCENDING)

        setSortDialogClickListeners()
        binding.sortClickable.setOnClickListener {
            hideKeyboard()
            viewModel.isSortDialogOverlayOpen.value = true
        }

        binding.sortDialogOverlay.setOnClickListener {
            viewModel.isSortDialogOverlayOpen.value = false
        }

        viewModel.isSortDialogOverlayOpen.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.sortDialogOverlay.visibility = View.VISIBLE
            } else {
                binding.sortDialogOverlay.visibility = View.GONE
            }
        })

        viewModel.sortMethod.observe(viewLifecycleOwner, Observer {
            setSortFilterText(it, friends)
        })
    }

    private fun setSortDialogClickListeners(){
        binding.aToZParent.setOnClickListener {
            viewModel.changeSortMethod(SortTypes.ASCENDING)
        }

        binding.zToAParent.setOnClickListener {
            viewModel.changeSortMethod(SortTypes.DESCENDING)
        }

        binding.oldestFirstParent.setOnClickListener {
            viewModel.changeSortMethod(SortTypes.OLDEST)
        }

        binding.newestFirstParent.setOnClickListener {
            viewModel.changeSortMethod(SortTypes.NEWEST)
        }

        binding.defaultParent.setOnClickListener {
            viewModel.changeSortMethod(SortTypes.DEFAULT)
        }
    }

    private fun setSortFilterText(sortType: Int, friends: List<User>){
        addFriendsToAdapter_initial(friends, sortType)

        if(sortType == SortTypes.ASCENDING){
            binding.sortText.setText(parent.getString(R.string.a_to_z_sorting))
        } else if(sortType == SortTypes.DESCENDING){
            binding.sortText.setText(parent.getText(R.string.z_to_a_sorting))
        } else if(sortType == SortTypes.NEWEST){
            binding.sortText.setText(parent.getString(R.string.newest_first))
        } else if(sortType == SortTypes.OLDEST){
            binding.sortText.setText(parent.getString(R.string.oldest_first))
        } else if(sortType == SortTypes.DEFAULT){
            binding.sortText.setText(parent.getString(R.string.default_))
        }
    }

    private fun navigateToAddfriends(){
        val action = FriendListFragmentDirections.actionFriendListFragmentToAddFriends()
        findNavController().navigate(action)
    }

    private fun checkIfEmptyFriends(friends: List<User?>?){
        if(friends?.isNotEmpty() == true){
            binding.noFriends.visibility = View.GONE
            binding.allFriendsRecyclerView.visibility = View.VISIBLE
        } else {
            binding.noFriends.visibility = View.VISIBLE
            binding.allFriendsRecyclerView.visibility = View.GONE
        }
    }

    private fun toggleCancelIconVisibility(){
        binding.searchBox.doAfterTextChanged {
            if (it.toString().isNotEmpty()){
                binding.clearText.visibility = View.VISIBLE
            } else {
                binding.clearText.visibility = View.GONE
            }
        }
    }

    private fun searchRecyclerView(friends: ArrayList<User>, it: Editable){
        if(it.toString().isEmpty()){
            // Text box is empty
            submitListToAdapter(friends)
        } else {
            val term = binding.searchBox.text.toString()
            val results = UserUtils.searchListOfUsers(term, friends)

            if(results.isNotEmpty()){
                // There are matches
                submitListToAdapter(results)
            } else {
                // No match is found
                submitListToAdapter(arrayListOf())
                binding.noMatch.visibility = View.VISIBLE
                binding.allFriendsRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun setFriendCount(friends: List<User?>){
        if(friends.size == 1){
            binding.friendCount.text = getString(R.string.people_count_one)
        } else {
            binding.friendCount.text = getString(R.string.people_count, friends.size)
        }
    }

    private fun setFriendCount(count: Int){
        if(count == 1){
            binding.friendCount.text = getString(R.string.people_count_one)
        } else {
            binding.friendCount.text = getString(R.string.people_count, count)
        }
    }

    private fun removeFromAdapter(adapter: FriendListAdapter, viewHolder: RecyclerView.ViewHolder){
        val currentList = adapter.friends.toMutableList()
        currentList.removeAt(viewHolder.absoluteAdapterPosition)
        adapter.friends = currentList as ArrayList<User>

        setFriendCount(currentList)

        adapter.notifyDataSetChanged()
    }

    private fun submitListToAdapter(list: List<User?>){
        if(list != null && list.isNotEmpty()){
            adapter?.friends = list as MutableList<User>

            setFriendCount(list)
//            binding.allFriendsRecyclerView.visibility = View.VISIBLE
//            binding.noMatch.visibility = View.GONE
//            binding.noFriends.visibility = View.GONE


            adapter?.notifyDataSetChanged()
        } else {
            setFriendCount(0)
        }
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.isSortDialogOverlayOpen.value = false
        _binding = null
    }
}