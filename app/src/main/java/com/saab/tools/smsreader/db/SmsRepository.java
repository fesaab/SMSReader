package com.saab.tools.smsreader.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.saab.tools.smsreader.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SmsRepository {

    private static final String TAG = SmsRepository.class.getSimpleName();
    public static final String CONTENT_SMS_INBOX = "content://sms/inbox";

    private ContentResolver contentResolver;
    private SmsDbHelper dbHelper;

    public SmsRepository(ContentResolver contentResolver, SmsDbHelper dbHelper) {
        this.contentResolver = contentResolver;
        this.dbHelper = dbHelper;
    }

    public List<Sms> querySmsInbox() {
        Log.i(TAG, "Querying SMS from the inbox...");
        Uri mSmsinboxQueryUri = Uri.parse(CONTENT_SMS_INBOX);
        Cursor cursor = contentResolver.query(mSmsinboxQueryUri,
                new String[] { "_id", "body", "address", "date" }, null, null, null);
        Log.i(TAG, "Querying SMSs from the inbox returned " + cursor.getCount() + " results");

        List<Sms> smsList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            Sms sms = Sms.builder()
                    .id(cursor.getString(0))
                    .message(cursor.getString(1))
                    .phoneNumber(cursor.getString(2))
                    .dateTime(cursor.getLong(3))
                    .build();

            if (sms.getMessage().startsWith(Constants.SMS_PREFIX_NEDBANK)) {
                Log.i(TAG, String.format("Sms=[%s]", sms.toString()));
                smsList.add(sms);
            }
        }

        return smsList;
    }

    private List<Sms> doQuerySmsDb(String filter) {
        List<Sms> smsList = new ArrayList<>();

        // Query the DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SmsDbEntry.TABLE,
                SmsDbEntry.PROJECTION_ALL_FIELDS,
                filter,
                null,
                null,
                null,
                SmsDbEntry.COL_SMS_DATE + " desc");

        // Parse to the entity object
        while(cursor.moveToNext()) {
            Sms sms = Sms.builder()
                    ._id(cursor.getString(cursor.getColumnIndex(SmsDbEntry._ID)))
                    .id(cursor.getString(cursor.getColumnIndex(SmsDbEntry.COL_SMS_ID)))
                    .message(cursor.getString(cursor.getColumnIndex(SmsDbEntry.COL_SMS_MESSAGE)))
                    .phoneNumber(cursor.getString(cursor.getColumnIndex(SmsDbEntry.COL_SMS_NUMBER)))
                    .dateTime(cursor.getLong(cursor.getColumnIndex(SmsDbEntry.COL_SMS_DATE)))
                    .synced("1".equals(cursor.getString(cursor.getColumnIndex(SmsDbEntry.COL_SMS_SYNCED))))
                    .syncedMessage(cursor.getString(cursor.getColumnIndex(SmsDbEntry.COL_SMS_SYNCED_MSG)))
                    .build();

            Log.d(TAG, "Sms: " + sms.toString());
            smsList.add(sms);
        }

        // Release the resources and return the result
        cursor.close();
        db.close();
        return smsList;
    }

    public List<Sms> querySmsDb() {
        return doQuerySmsDb(null);
    }

    public List<Sms> querySmsDbById(List<String> ids) {
        return doQuerySmsDb(SmsDbEntry.COL_SMS_ID + " in (" + String.join(",", ids) + ")");
    }

    public List<Sms> querySmsDbPendingSync() {
        return doQuerySmsDb(SmsDbEntry.COL_SMS_SYNCED + " = 0");
    }

    public void insert(Sms smsToInsert) {
        Log.i(TAG, "Inserting new SMS on DB: " + smsToInsert.toString());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SmsDbEntry.COL_SMS_ID, smsToInsert.getId());
        values.put(SmsDbEntry.COL_SMS_MESSAGE, smsToInsert.getMessage());
        values.put(SmsDbEntry.COL_SMS_NUMBER, smsToInsert.getPhoneNumber());
        values.put(SmsDbEntry.COL_SMS_DATE, String.valueOf(smsToInsert.getDateTime()));
        values.put(SmsDbEntry.COL_SMS_SYNCED, smsToInsert.isSynced() ? "1" : "0");
        values.put(SmsDbEntry.COL_SMS_SYNCED_MSG, smsToInsert.getSyncedMessage());
        db.insertWithOnConflict(SmsDbEntry.TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void update(Sms smsToUpdate) {
        Log.i(TAG, "Updating SMS on DB: " + smsToUpdate.toString());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SmsDbEntry.COL_SMS_SYNCED, smsToUpdate.isSynced() ? "1" : "0");
        values.put(SmsDbEntry.COL_SMS_SYNCED_MSG, smsToUpdate.getSyncedMessage());
        db.updateWithOnConflict(SmsDbEntry.TABLE,
                values,
                SmsDbEntry._ID + " = ?",
                new String[]{smsToUpdate.get_id()},
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void resetSync() {
        Log.i(TAG, "Reseting the sync of all SMSs on DB: ");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SmsDbEntry.COL_SMS_SYNCED, "0");
        values.put(SmsDbEntry.COL_SMS_SYNCED_MSG, "");
        db.updateWithOnConflict(SmsDbEntry.TABLE,
                values,
                SmsDbEntry.COL_SMS_SYNCED + " = ?",
                new String[]{"1"},
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
}
