package com.chandruscm.minirto.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.chandruscm.minirto.Models.Vehicle;

public class DataBaseHandler extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "history";
    
    private static final String TABLE_VEHICLE = "vehicle";

    private static final  String KEY_NUMBER = "number";
    private static final  String KEY_NAME = "name";
    private static final  String KEY_FUEL = "fuel";
    private static final  String KEY_CC = "cc";
    private static final  String KEY_ENGINE = "engine";
    private static final  String KEY_CHASIS = "chasis";
    private static final  String KEY_OWNER = "owner";
    private static final  String KEY_LOCATION = "location";
    private static final  String KEY_DATE = "expiry";
    
    public DataBaseHandler(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_VEHICLE_TABLE = "CREATE TABLE" + " " + TABLE_VEHICLE +
                                     "(" + KEY_NUMBER + " "+ "TEXT" + " " + " PRIMARY KEY,"+
                                         KEY_NAME + " " + "TEXT," +
                                         KEY_FUEL + " " + "TEXT," +
                                         KEY_CC + " " + "TEXT," +
                                         KEY_ENGINE + " " + "TEXT," +
                                         KEY_CHASIS + " " + "TEXT," +
                                         KEY_OWNER + " " + "TEXT," +
                                         KEY_LOCATION + " " + "TEXT," +
                                         KEY_DATE + " " + "TEXT" + ")";

        db.execSQL(CREATE_VEHICLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLE);
        onCreate(db);
    }

    public void addVehicle(Vehicle vehicle)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NUMBER, vehicle.getNumber());
        values.put(KEY_NAME, vehicle.getName());
        values.put(KEY_FUEL, vehicle.getFuel());
        values.put(KEY_CC, vehicle.getCc());
        values.put(KEY_ENGINE, vehicle.getEngine());
        values.put(KEY_CHASIS, vehicle.getChassis());
        values.put(KEY_OWNER, vehicle.getOwner());
        values.put(KEY_LOCATION, vehicle.getLocation());
        values.put(KEY_DATE, vehicle.getExpiry());

        db.insertWithOnConflict(TABLE_VEHICLE, null, values,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void removeVehicle(String number)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VEHICLE, KEY_NUMBER + "= '" + number +"'", null);
        db.close();
    }

    public Cursor getVehicles()
    {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {KEY_NUMBER, KEY_NAME, KEY_FUEL, KEY_CC, KEY_ENGINE, KEY_CHASIS, KEY_OWNER, KEY_LOCATION, KEY_DATE};
        qb.setTables(TABLE_VEHICLE);

        Cursor c = qb.query(db, sqlSelect, null, null, null, null, null);

        c.moveToFirst();
        db.close();
        return c;
    }
}
