package com.dev.readymapeo.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

    var userToken: String? = null
    var isLoggedIn = mutableStateOf<Boolean>(false)
    var loginError = mutableStateOf<String?>(null)

    init {
        loadFromLocal()
    }

    fun loadFromLocal() {
        clubs.clear()
        clubs.addAll(dbHelper.getAllClubs())
    }

    fun sync() {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ClubDownloader().getClubAndAdd(dbHelper)
            if (success) {
                withContext(Dispatchers.Main) { loadFromLocal() }
            }
        }
    }


    fun getToken(email: String, mdp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Login", "Tentative de connexion pour : $email")

            val downloader = ClubDownloader()
            val token = downloader.login(email, mdp)

            withContext(Dispatchers.Main) {
                if (token != null) {
                    Log.d("Login", "Succès ! Token reçu : $token")
                    isLoggedIn.value = true
                    userToken = token
                } else {
                    loginError.value = "Erreur de connexion"
                }
            }

        }
    }
}