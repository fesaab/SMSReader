package com.saab.tools.smsreader;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class MessageReceiver extends BroadcastReceiver {

    @Override
    @SuppressLint("DefaultLocale")
    @TargetApi(Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String message = String.format("Sender: %s, Email From: %s, Email body: %s, " +
                            "Display message body: %s, Time in millisecond: %d, Message: %s",
                    smsMessage.getDisplayOriginatingAddress(),
                    smsMessage.getEmailFrom(),
                    smsMessage.getEmailBody(),
                    smsMessage.getDisplayMessageBody(),
                    smsMessage.getTimestampMillis(),
                    smsMessage.getMessageBody());

            Toast.makeText(context, "Received message: " + message, Toast.LENGTH_SHORT).show();
        }
    }

}
