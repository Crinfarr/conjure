package com.crinfarr.conjure.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.Connection;
import java.util.Dictionary;

public class CardDB {
    private static final String TAG = "CardDB";
    protected final SQLiteDatabase database;
    public CardDB(SQLiteDatabase database) {
        this.database = database;
    }
    protected Dictionary<String, String> getCardObject(String name) {
        Cursor c = database.rawQuery("SELECT * FROM carddb WHERE cardname = ?;", new String[] {name});
        if (!c.moveToFirst()) {
            throw new RuntimeException("Could not get card with name "+name);
        }
        for (String columnName : c.getColumnNames()) {
            
        }
    }
}
