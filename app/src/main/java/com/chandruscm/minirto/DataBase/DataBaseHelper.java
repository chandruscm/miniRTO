package com.chandruscm.minirto.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DataBaseHelper extends SQLiteAssetHelper
{
    private static final String DATABASE_NAME = "rto.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "office";

    public DataBaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Cursor getOffices()
    {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"code","district","state","address","phone","website"};
        qb.setTables(TABLE_NAME);

        Cursor c = qb.query(db, sqlSelect, null, null, null, null, null);

        c.moveToFirst();
        return c;
    }

    public Cursor matchOffice(CharSequence input)
    {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"code","district","state","address","phone","website"};
        qb.setTables(TABLE_NAME);

        Cursor c = qb.query(db, sqlSelect, "district" + " LIKE ?", new String[] {input + "%"}, null, null, null);
        return c;
    }
}
