package com.dev.readymapeo.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.readymapeo.database.DatabaseHelper
import com.dev.readymapeo.models.Club
import com.dev.readymapeo.network.ClubDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClubViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    var clubs = mutableStateListOf<Club>()

    init { loadFromLocal() }

    fun loadFromLocal() {
        clubs.clear()
        clubs.addAll(dbHelper.getAllClubs())
    }

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ClubDownloader().loadAndSave(dbHelper)
            if (success) {
                withContext(Dispatchers.Main) { loadFromLocal() }
            }
        }
    }
}