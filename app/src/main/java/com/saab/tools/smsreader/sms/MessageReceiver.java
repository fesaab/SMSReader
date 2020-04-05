package com.saab.tools.smsreader.sms;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.saab.tools.smsreader.ui.SmsListViewHelper;
import com.saab.tools.smsreader.util.Constants;

public class MessageReceiver extends BroadcastReceiver {

    private static final String TAG = MessageReceiver.class.getSimpleName();

    private static SmsListViewHelper smsListViewHelper;

    public static void init(SmsListViewHelper smsListViewHelper) {
        MessageReceiver.smsListViewHelper = smsListViewHelper;
    }

    @Override
    @SuppressLint("DefaultLocale")
    @TargetApi(Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {

        if (smsListViewHelper != null) {
            Bundle data = intent.getExtras();
            Object[] pdus = (Object[]) data.get("pdus");

            boolean shouldRefreshUi = false;

            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                Log.i(TAG, "Received new SMS message: " + smsMessage.toString());

                if (smsMessage.getMessageBody().startsWith(Constants.SMS_PREFIX_NEDBANK)) {

                    // At this point we don't have the "id" from the SMS so we cannot create it on DB =(
                    shouldRefreshUi = true;
                }
            }

            // Search for the new SMSs and save them on DB
            if (shouldRefreshUi)
                smsListViewHelper.searchNewSmss();

        } else {
            Log.i(TAG, "Message received but not processed because the MessageReceiver was not initialized!");
        }

    }

}
