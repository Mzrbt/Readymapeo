package com.dev.readymapeo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dev.readymapeo.models.Club

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "clubs.db", null, 3) {

    private val TABLE_CLUBS = "clubs"
    private val COL_ID = "club_id"
    private val COL_NAME = "club_name"
    private val COL_STREET = "club_street"
    private val COL_CITY = "club_city"
    private val COL_POSTAL = "club_postal_code"
    private val COL_DESC = "description"
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
                $COL_IS_DIRTY INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLUBS")
        onCreate(db)
    }


    fun addClub(club: Club) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, club.id)
            put(COL_NAME, club.name)
            put(COL_STREET, club.street)
            put(COL_CITY, club.city)
            put(COL_POSTAL, club.postalCode)
            put(COL_DESC, club.description)
        }
        db.insertWithOnConflict(TABLE_CLUBS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getAllClubs(): List<Club> {
        val list = mutableListOf<Club>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CLUBS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Club(
                    cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}