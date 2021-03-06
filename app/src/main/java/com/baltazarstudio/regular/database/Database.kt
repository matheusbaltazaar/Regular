package com.baltazarstudio.regular.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.baltazarstudio.regular.database.dao.ConfiguracaoDAO
import com.baltazarstudio.regular.database.dao.DespesaDAO
import com.baltazarstudio.regular.database.dao.EntradaDAO
import com.baltazarstudio.regular.database.dao.MovimentoDAO

abstract class Database<T>(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        MovimentoDAO.onCreate(db)
        EntradaDAO.onCreate(db)
        DespesaDAO.onCreate(db)
        ConfiguracaoDAO.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }

    abstract fun bind(cursor: Cursor, elemento: T)

    companion object {
        private const val DATABASE_NAME = "RegularDB"
        private const val DATABASE_VERSION = 1
        const val TABLE_ID = "id"
    }

}
