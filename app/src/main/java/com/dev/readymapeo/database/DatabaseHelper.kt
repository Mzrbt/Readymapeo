package com.dev.readymapeo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dev.readymapeo.models.Club

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "clubs.db", null, 10) {

    private val TABLE_CLUBS = "clubs"
    private val COL_ID = "club_id"
    private val COL_NAME = "club_name"
    private val COL_STREET = "club_street"
    private val COL_CITY = "club_city"
    private val COL_POSTAL = "club_postal_code"
    private val COL_DESC = "description"
    private val COL_FFSO_ID = "ffso_id"
    private val COL_IS_DIRTY = "is_dirty"

    /**
     * Crée la table des clubs lors de la première initialisation de la base de données.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CLUBS (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_NAME TEXT,
                $COL_STREET TEXT,
                $COL_CITY TEXT,
                $COL_POSTAL TEXT,
                $COL_DESC TEXT,
                $COL_FFSO_ID TEXT,
                $COL_IS_DIRTY INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    /**
     * Gère la mise à jour de la structure de la base de données.
     * Supprime la table existante et la recrée.
     */
    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLUBS")
        onCreate(db)
    }

    /**
     * Ajoute un club localement en le marquant comme "sale" (is_dirty = 1).
     * @param club Le club à ajouter en local.
     */
    fun addClubLocal(club: Club) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, club.id)
            put(COL_NAME, club.name)
            put(COL_STREET, club.street)
            put(COL_CITY, club.city)
            put(COL_POSTAL, club.postalCode)
            put(COL_DESC, club.description)
            put(COL_FFSO_ID, club.ffsoId)
            put(COL_IS_DIRTY, 1)
        }
        db.insertWithOnConflict(TABLE_CLUBS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Exécute une requête de sélection et retourne une liste de clubs.
     * @param query La requête SQL à exécuter.
     * @return La liste des clubs trouvés.
     */
    fun getClubs(query : String): List<Club> {
        val list = mutableListOf<Club>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            list.add(Club(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                street = cursor.getString(cursor.getColumnIndexOrThrow(COL_STREET)),
                city = cursor.getString(cursor.getColumnIndexOrThrow(COL_CITY)),
                postalCode = cursor.getString(cursor.getColumnIndexOrThrow(COL_POSTAL)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC)),
                ffsoId = cursor.getString(cursor.getColumnIndexOrThrow(COL_FFSO_ID)),
                isDirty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_DIRTY)) == 1
            ))
        }
        cursor.close()
        return list
    }

    /**
     * Ajoute un club provenant de l'API en supprimant d'éventuels doublons temporaires locaux.
     * @param club Le club officiel reçu de l'API.
     */
    fun addClubFromAPI(club: Club) {
        val db = this.writableDatabase
        db.delete(TABLE_CLUBS, "$COL_NAME = ? AND $COL_ID < 0", arrayOf(club.name))
        val values = ContentValues().apply {
            put(COL_ID, club.id)
            put(COL_NAME, club.name)
            put(COL_STREET, club.street)
            put(COL_CITY, club.city)
            put(COL_POSTAL, club.postalCode)
            put(COL_DESC, club.description)
            put(COL_FFSO_ID, club.ffsoId)
            put(COL_IS_DIRTY, 0)
        }
        db.insertWithOnConflict(TABLE_CLUBS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Récupère tous les clubs enregistrés en base.
     * @return La liste de tous les clubs.
     */
    fun getAllClubs(): List<Club>{
        return getClubs("SELECT * FROM $TABLE_CLUBS")
    }

    /**
     * Récupère uniquement les clubs qui n'ont pas encore été synchronisés avec l'API.
     * @return La liste des clubs locaux modifiés.
     */
    fun getDirtyClubs(): List<Club>{
        return getClubs("SELECT * FROM $TABLE_CLUBS WHERE $COL_IS_DIRTY = 1")
    }

    /**
     * Marque un club comme synchronisé après un envoi réussi à l'API.
     * @param clubId L'identifiant du club à mettre à jour.
     */
    fun markAsSynced(clubId: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COL_IS_DIRTY, 0) }
        db.update(TABLE_CLUBS, values, "$COL_ID = ?", arrayOf(clubId.toString()))
    }
}
