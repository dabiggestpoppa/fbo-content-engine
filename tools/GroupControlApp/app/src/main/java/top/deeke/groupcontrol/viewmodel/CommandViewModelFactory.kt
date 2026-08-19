package top.deeke.groupcontrol.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CommandViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommandViewModel::class.java)) {
            return CommandViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
