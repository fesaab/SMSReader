package com.saab.tools.smsreader;

import android.telephony.SmsMessage;

public interface SmsListener {
    void newSms(SmsMessage smsMessage);
}
