package com.dev.readymapeo.network

import android.util.Log
import com.dev.readymapeo.database.DatabaseHelper
import com.dev.readymapeo.models.Club
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ClubDownloader {
    private val TAG = "ClubDownloader"
    private val API_BASE_URL = "http://10.0.2.2:8080/api"
    private val CLUBS_URL = "$API_BASE_URL/clubs"
    private val LOGIN_URL = "$API_BASE_URL/login"

    /**
     * Récupère la liste des clubs depuis l'API et les enregistre dans la base de données locale.
     * @param dbHelper L'assistant de base de données pour l'enregistrement.
     * @return True si la récupération et l'enregistrement ont réussi, false sinon.
     */
    fun getClubAndAdd(dbHelper: DatabaseHelper): Boolean {
        val jsonString = executeRequest(CLUBS_URL, "GET") ?: return false
        return try {
            val jsonArray = JSONObject(jsonString).getJSONObject("data").getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                dbHelper.addClubFromAPI(Club(
                    id = obj.getInt("club_id"),
                    name = obj.getString("club_name"),
                    street = obj.getString("club_street"),
                    city = obj.getString("club_city"),
                    postalCode = obj.getString("club_postal_code"),
                    description = obj.optString("description", ""),
                    ffsoId = obj.optString("ffso_id", "")
                ))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur parsing clubs", e)
            false
        }
    }

    /**
     * Tente de connecter l'utilisateur auprès de l'API pour obtenir un jeton d'authentification.
     * @param email L'adresse email de l'utilisateur.
     * @param mdp Le mot de passe de l'utilisateur.
     * @return Le jeton (token) si la connexion réussit, null sinon.
     */
    fun login(email: String, mdp: String): String? {
        val body = JSONObject().apply {
            put("email", email)
            put("password", mdp)
        }
        val response = executeRequest(LOGIN_URL, "POST", body) ?: return null
        return try {
            JSONObject(response).getJSONObject("data").getString("token")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur parsing token", e)
            null
        }
    }

    /**
     * Envoie les clubs créés localement vers le serveur API.
     * @param dbHelper L'assistant de base de données pour récupérer les clubs dirty.
     * @param token Le jeton d'authentification nécessaire pour le POST.
     * @return True si tous les clubs ont été synchronisés avec succès.
     */
    fun syncDirtyClubs(dbHelper: DatabaseHelper, token: String): Boolean {
        val dirtyClubs = dbHelper.getDirtyClubs()
        Log.d(TAG, "${dirtyClubs.size} club(s) dirty à synchroniser")
        return dirtyClubs.fold(true) { allSuccess, club ->
            val body = JSONObject().apply {
                put("club_name", club.name)
                put("club_street", club.street)
                put("club_city", club.city)
                put("club_postal_code", club.postalCode)
                put("description", club.description)
                put("ffso_id", club.ffsoId)
            }
            val success = executeRequest(CLUBS_URL, "POST", body, token) != null
            Log.d(TAG, "postClub [${club.name}] → ${if (success) "succès" else "échec"}")
            if (success) dbHelper.markAsSynced(club.id)
            allSuccess && success
        }
    }

    /**
     * Exécute une requête HTTP générique vers l'API.
     * @param urlStr L'URL cible de la requête.
     * @param method La méthode HTTP (GET, POST, etc.).
     * @param body Le corps de la requête au format JSONObject (optionnel).
     * @param token Le jeton Bearer pour l'authentification (optionnel).
     * @return La réponse du serveur sous forme de chaîne de caractères, ou null en cas d'erreur.
     */
    private fun executeRequest(
        urlStr: String,
        method: String,
        body: JSONObject? = null,
        token: String? = null
    ): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.requestMethod = method
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doInput = true
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest")
            token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            if (method == "POST") {
                connection.doOutput = true
                body?.let { connection.outputStream.use { os -> os.write(it.toString().toByteArray()) } }
            }
            return when (val code = connection.responseCode) {
                200, 201 -> connection.inputStream.bufferedReader().use { it.readText() }
                302 -> """{"status":"success","message":"created"}"""
                else -> {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "HTTP $code — $error")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur executeRequest", e)
        } finally {
            connection?.disconnect()
        }
        return null
    }
}
