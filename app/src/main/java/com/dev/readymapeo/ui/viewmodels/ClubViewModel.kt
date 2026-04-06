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
    var isLoggedIn = mutableStateOf(false)
    var loginError = mutableStateOf<String?>(null)

    var syncMessage = mutableStateOf<String?>(null)

    init {
        loadFromLocal()
    }

    /**
     * Charge la liste complète des clubs depuis la base de données locale (SQLite).
     * La liste est inversée pour afficher les ajouts les plus récents en haut de l'interface.
     */
    fun loadFromLocal() {
        clubs.clear()
        clubs.addAll(dbHelper.getAllClubs().reversed())
    }

    /**
     * Lance le processus de synchronisation bidirectionnelle avec l'API.
     * Si l'utilisateur n'est pas connecté mais possède des ajouts en attente, génère un message d'erreur.
     * Si connecté, envoie (POST) d'abord les clubs locaux vers le serveur.
     * Enfin, récupère (GET) les dernières données de l'API pour mettre à jour la liste locale.
     */
    fun sync() {
        viewModelScope.launch(Dispatchers.IO) {
            val dirtyClubs = dbHelper.getDirtyClubs()

            if (userToken == null && dirtyClubs.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val pluriel = if (dirtyClubs.size > 1) "s" else ""
                    val cesCe = if (dirtyClubs.size > 1) "ces" else "ce"
                    syncMessage.value = "Vous ne pouvez pas ajouter $cesCe club$pluriel car vous n'êtes pas connecté."
                }
            } else if (userToken != null) {
                ClubDownloader().syncDirtyClubs(dbHelper, userToken!!)
            }

            val success = ClubDownloader().getClubAndAdd(dbHelper)

            if (success) {
                withContext(Dispatchers.Main) { loadFromLocal() }
            }
        }
    }

    /**
     * Réinitialise le message d'erreur de synchronisation.
     * Doit être appelée après l'affichage du message dans l'UI (ex: Toast) pour éviter qu'il ne boucle.
     */
    fun clearSyncMessage() {
        syncMessage.value = null
    }

    /**
     * Tente d'authentifier l'utilisateur auprès de l'API.
     * En cas de succès, stocke le token Sanctum en mémoire et passe l'état de connexion à vrai.
     * En cas d'échec, définit un message d'erreur pour l'interface.
     */
    fun getToken(email: String, mdp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Login", "Tentative de connexion pour : $email")
            val token = ClubDownloader().login(email, mdp)
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

    /**
     * Crée un nouveau club et l'enregistre en base de données locale de manière asynchrone.
     * Un ID temporaire négatif est généré pour éviter tout conflit avec les IDs officiels du serveur.
     * Le club est enregistré avec le statut "dirty" (non synchronisé) en attendant une connexion.
     */
    fun addClub(
        name: String,
        street: String,
        city: String,
        postalCode: String,
        description: String,
        ffsoId: String,
        onResult: (Boolean) -> Unit
    ) {
        val tempId = -(System.currentTimeMillis().toInt())

        val club = Club(
            id = tempId,
            name = name,
            street = street,
            city = city,
            postalCode = postalCode,
            description = description,
            ffsoId = ffsoId,
            isDirty = true
        )

        dbHelper.addClubLocal(club)
        loadFromLocal()
        onResult(true)
    }
}