package com.varsel.firechat.domain.use_case._util.search

import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import kotlinx.coroutines.flow.MutableStateFlow

class SetupSearchBarUseCase {
    operator fun invoke (
        cancelButton: ImageView,
        searchBox: EditText,
        fragment: Fragment,
        noFriendsLayout: LinearLayout,
        noMatchLayout: LinearLayout,
        recyclerView: RecyclerView,
        observableList: MutableStateFlow<List<User>>,
        afterCallback: ()-> Unit,
        resultsCallback: (users: List<User>)-> Unit
    ){
        cancelButton.setOnClickListener {
            searchBox.setText("")
        }

        // Actual search code
        searchBox.doAfterTextChanged {
            val friends = observableList.value?.toList()

            if(it != null){
                searchRecyclerView(friends, it, searchBox, noMatchLayout, recyclerView) {
                    resultsCallback(it)
                    recyclerView.visibility = View.VISIBLE
                    noMatchLayout.visibility = View.GONE
                    noFriendsLayout.visibility = View.GONE
                }
            }
        }

        fragment.collectLatestLifecycleFlow(observableList) {
            checkIfEmptyFriends(it, noFriendsLayout, recyclerView)
        }
//        observableList.observe(fragment.viewLifecycleOwner, Observer {
//            checkIfEmptyFriends(it, noFriendsLayout, recyclerView)
//        })

        afterCallback()


        toggleCancelIconVisibility(searchBox, cancelButton)
    }

    private fun checkIfEmptyFriends(friends: List<User>?, noFriendsLayout: LinearLayout, recyclerView: RecyclerView){
        if(friends?.isNotEmpty() == true){
            noFriendsLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            noFriendsLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun toggleCancelIconVisibility(searchBox: EditText, cancelButton: ImageView){
        searchBox.doAfterTextChanged {
            if (it.toString().isNotEmpty()){
                cancelButton.visibility = View.VISIBLE
            } else {
                cancelButton.visibility = View.GONE
            }
        }
    }

    private fun searchRecyclerView(
        friends: List<User>,
        it: Editable,
        searchBox: EditText,
        noMatchLayout: LinearLayout,
        recyclerView: RecyclerView,
        submitListToAdapter: (users: List<User>) -> Unit
    ){
        noMatchLayout.visibility = View.GONE

        if(it.toString().isEmpty()){
            // Text box is empty
            submitListToAdapter(friends)
        } else {
            val term = searchBox.text.toString()
            val results = UserUtils.searchListOfUsers(term, friends as List<User>)

            if(results.isNotEmpty()){
                // There are matches
                submitListToAdapter(results)
            } else {
                // No match is found
                submitListToAdapter(arrayListOf())
                if(friends.isNotEmpty()){
                    noMatchLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
        }
    }
}