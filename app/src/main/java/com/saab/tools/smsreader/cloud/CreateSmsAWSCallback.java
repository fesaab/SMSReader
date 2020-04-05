package com.saab.tools.smsreader.cloud;

import com.amazonaws.amplify.generated.graphql.CreateSmsMutation;
import com.apollographql.apollo.GraphQLCall;
import com.saab.tools.smsreader.db.Sms;

public abstract class CreateSmsAWSCallback extends GraphQLCall.Callback<CreateSmsMutation.Data> {

    private static final String TAG = CreateSmsAWSCallback.class.getSimpleName();

    private Sms sms;

    public CreateSmsAWSCallback(Sms sms) {
        this.sms = sms;
    }

    public Sms getSms() {
        return this.sms;
    }

}
