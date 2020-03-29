package com.saab.tools.smsreader;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {

    private static final String TAG = MessageReceiver.class.getSimpleName();

    private static SmsListener listener;

    public static void setListener(SmsListener listener) {
        MessageReceiver.listener = listener;
    }

    @Override
    @SuppressLint("DefaultLocale")
    @TargetApi(Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            Log.i(TAG, "Received new SMS message: " + smsMessage.toString());

            if (listener != null) {
                Log.i(TAG, "Invoking message listener...");
                listener.newSms(smsMessage);
                Log.i(TAG, "Message listener invoked!");
            } else {
                Log.i(TAG, "No message listener configured!");
            }
        }
    }

}
