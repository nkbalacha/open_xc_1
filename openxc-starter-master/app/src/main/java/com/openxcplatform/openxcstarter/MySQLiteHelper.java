package com.openxcplatform.openxcstarter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {


    private static final String LOG="MySQLiteHelper";
    public static final String TABLE_COMMENTS = "comments";
    public static final String COLUMN_COMMENT = "comment";


    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String KEY_CREATED_AT="created_at";


    public static final String TABLE_DATA ="datas";
    public static final String COLUMN_DATA="data";
    public static final String Column_DATA_ID="data_id";

    // database  management
    private static final String DATABASE_NAME = "commments.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMMENTS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_COMMENT
            + " text not null);";

    private static final String Data_Create="create table" +TABLE_DATA +"(" +COLUMN_ID +"integer primary key autoincrement," +COLUMN_DATA+ "TEXT);";
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // database.execSQL(Data_Create);
        database.execSQL(DATABASE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_DATA);
        onCreate(db);
    }







    public void onOpen() {
    }
}
