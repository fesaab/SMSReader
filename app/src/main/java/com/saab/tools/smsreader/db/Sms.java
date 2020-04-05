package com.saab.tools.smsreader.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Sms {

    private String _id;
    private String id;
    private String message;
    private String phoneNumber;
    private long dateTime;
    private boolean synced;
    private String syncedMessage;

}
