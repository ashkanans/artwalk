// SharedViewModel.kt
package io.ashkanans.artwalk

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _navigateToFragment = MutableLiveData<Class<out Fragment>>()
    val navigateToFragment: LiveData<Class<out Fragment>> get() = _navigateToFragment

    fun navigateTo(fragmentClass: Class<out Fragment>) {
        _navigateToFragment.value = fragmentClass
    }
}
