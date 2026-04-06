package com.dev.readymapeo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dev.readymapeo.models.Club

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "clubs.db", null, 4) {

    private val TABLE_CLUBS = "clubs"
    private val COL_ID = "club_id"
    private val COL_NAME = "club_name"
    private val COL_STREET = "club_street"
    private val COL_CITY = "club_city"
    private val COL_POSTAL = "club_postal_code"
    private val COL_DESC = "description"
    private val COL_FFSO_ID = "ffso_id"
    private val COL_IS_DIRTY = "is_dirty"

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

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLUBS")
        onCreate(db)
    }


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
                ffsoId = cursor.getString(cursor.getColumnIndexOrThrow(COL_FFSO_ID))
            ))
        }
        cursor.close()
        return list
    }

    fun addClubFromAPI(club: Club) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, club.id)
            put(COL_NAME, club.name)
            put(COL_STREET, club.street)
            put(COL_CITY, club.city)
            put(COL_POSTAL, club.postalCode)
            put(COL_DESC, club.description)
            put(COL_FFSO_ID, club.ffsoId)
            put(COL_IS_DIRTY, 0) // 👈 clubs venant de l'API = propres
        }
        db.insertWithOnConflict(TABLE_CLUBS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun getAllClubs(): List<Club>{
        return getClubs("SELECT * FROM $TABLE_CLUBS")
    }

    fun getDirtyClubs(): List<Club>{
        return getClubs("SELECT * FROM $TABLE_CLUBS WHERE $COL_IS_DIRTY = 1")
    }

    fun markAsSynced(clubId: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COL_IS_DIRTY, 0) }
        db.update(TABLE_CLUBS, values, "$COL_ID = ?", arrayOf(clubId.toString()))
    }

}