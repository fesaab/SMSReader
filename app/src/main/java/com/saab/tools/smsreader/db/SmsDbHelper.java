package com.saab.tools.smsreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsDbHelper extends SQLiteOpenHelper {

    public SmsDbHelper(Context context) {
        super(context, SmsDbContract.DB_NAME, null, SmsDbContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + SmsDbEntry.TABLE + " ( " +
                SmsDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SmsDbEntry.COL_SMS_ID + " TEXT NOT NULL, " +
                SmsDbEntry.COL_SMS_MESSAGE + " TEXT NOT NULL, " +
                SmsDbEntry.COL_SMS_NUMBER + " TEXT NOT NULL, " +
                SmsDbEntry.COL_SMS_DATE + " INTEGER NOT NULL, " +
                SmsDbEntry.COL_SMS_SYNCED + " INTEGER DEFAULT 0, " +
                SmsDbEntry.COL_SMS_SYNCED_MSG + " TEXT);";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SmsDbEntry.TABLE);
        onCreate(db);
    }
}
