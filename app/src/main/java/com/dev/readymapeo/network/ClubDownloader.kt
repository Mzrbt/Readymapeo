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
    private val API_URL = "http://10.0.2.2:8080/api/clubs"

    fun loadAndSave(dbHelper: DatabaseHelper): Boolean {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(API_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.use { it.readText() }

                val root = JSONObject(jsonString)

                val paginationObj = root.getJSONObject("data")
                val jsonArray = paginationObj.getJSONArray("data")

                Log.d(TAG, "Données reçues: ${jsonArray.length()} clubs trouvés.")
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val club = Club(
                        id = obj.getInt("club_id"),
                        name = obj.getString("club_name"),
                        street = obj.getString("club_street"),
                        city = obj.getString("club_city"),
                        postalCode = obj.getString("club_postal_code"),
                        description = obj.optString("description", ""),
                        isDirty = 0
                    )
                    dbHelper.addClub(club)
                }
                return true
            } else {
                Log.e(TAG, "Erreur HTTP: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des clubs", e)
        } finally {
            connection?.disconnect()
        }
        return false
    }
}