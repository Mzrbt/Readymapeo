package com.dev.readymapeo.network

import android.util.Log
import com.dev.readymapeo.database.DatabaseHelper
import com.dev.readymapeo.models.Club
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ClubDownloader {
    private val TAG = "ClubDownloader"
    private val API_BASE_URL = "http://10.0.2.2:8080/api"
    private val CLUBS_URL = "$API_BASE_URL/clubs"
    private val LOGIN_URL = "$API_BASE_URL/login"

    fun getClubAndAdd(dbHelper: DatabaseHelper): Boolean {
        val connection: HttpURLConnection? = null
        val jsonString: String? = executeRequest(CLUBS_URL, "GET")

        try {
            val root = JSONObject(jsonString)
            val paginationObj = root.getJSONObject("data")
            val jsonArray = paginationObj.getJSONArray("data")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val club = Club(
                    id = obj.getInt("club_id"),
                    name = obj.getString("club_name"),
                    street = obj.getString("club_street"),
                    city = obj.getString("club_city"),
                    postalCode = obj.getString("club_postal_code"),
                    description = obj.optString("description", "")
                )
                dbHelper.addClub(club)
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des clubs", e)
        } finally {
            connection?.disconnect()
        }
        return false
    }

    /**
     * Fonction universelle pour exécuter une requête HTTP
     */
    private fun executeRequest(
        urlStr: String,
        method: String,
        body: String? = null,
        token: String? = null,
        isMultipart: Boolean = false,
        boundary: String? = null,
    ): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doInput = true

            connection.setRequestProperty("Accept", "application/json")
            token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }

            if (method == "POST") {
                if (isMultipart) {
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                } else {
                    connection.setRequestProperty("Content-Type", "application/json")
                }
                body?.let {
                    connection.outputStream.use { os ->
                        os.write(it.toByteArray())
                    }
                }
            }

            // 3. LECTURE DE LA RÉPONSE
            val code = connection.responseCode
            if (code == 200 || code == 201 ) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log de l'erreur pour le debug
        } finally {
            connection?.disconnect()
        }
        return null
    }

    fun login(email: String, mdp: String): String? {
        val json = JSONObject().apply {
            put("email", email)
            put("password", mdp)
        }.toString()

        val response = executeRequest(LOGIN_URL, "POST", json)

        return response?.let {
            Log.d("DEBUG_LOGIN", "Réponse reçue : $it")
            val jsonObj = JSONObject(it)

            // On vérifie d'abord si le token est dans "data"
            (if (jsonObj.has("data")) {
                val data = jsonObj.getJSONObject("data")
                if (data.has("token")) {
                    return data.getString("token")
                }else{
                    null
                }
            }else{
                null
            }).toString()


        }
    }
}