package com.varsel.firechat.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExtensionFunctions {
    companion object {
        fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
            observe(lifecycleOwner, object : Observer<T> {
                override fun onChanged(t: T?) {
                    observer.onChanged(t)
                    removeObserver(this)
                }
            })
        }

        fun Fragment.hideKeyboard() {
            view?.let { activity?.hideKeyboard(it) }
        }

        fun Activity.hideKeyboard() {
            hideKeyboard(currentFocus ?: View(this))
        }

        fun Context.hideKeyboard(view: View) {
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun Context.showKeyboard() {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }

        fun NavController.navigate(directions: NavDirections, afterCallback: ()-> Unit) {
            navigate(directions.actionId, directions.arguments, null)
            afterCallback()
        }

        fun <T> ComponentActivity.collectLatestLifecycleFlow (flow: Flow<T>, collect: suspend (T) -> Unit) {
            lifecycleScope.launch {
                repeatOnLifecycle (Lifecycle.State.STARTED) {
                    flow.collectLatest(collect)
                }
            }
        }

        fun <T> Fragment.collectLatestLifecycleFlow (flow: Flow<T>, collect: suspend (T) -> Unit) {
            lifecycleScope.launch {
                repeatOnLifecycle (Lifecycle.State.STARTED) {
                    flow.collectLatest(collect)
                }
            }
        }
    }
}