package com.saab.tools.smsreader.db;

import android.provider.BaseColumns;

public class SmsDbEntry implements BaseColumns {
    public static final String TABLE = "sms";

    public static final String COL_SMS_ID = "id";
    public static final String COL_SMS_MESSAGE = "message";
    public static final String COL_SMS_NUMBER = "number";
    public static final String COL_SMS_DATE = "date";
    public static final String COL_SMS_SYNCED = "synced";
    public static final String COL_SMS_SYNCED_MSG = "syncedmsg";

    public static final String[] PROJECTION_ALL_FIELDS = {
            _ID,
            COL_SMS_ID,
            COL_SMS_MESSAGE,
            COL_SMS_NUMBER,
            COL_SMS_DATE,
            COL_SMS_SYNCED,
            COL_SMS_SYNCED_MSG
    };
}
