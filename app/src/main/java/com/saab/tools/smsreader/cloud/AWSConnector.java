package com.saab.tools.smsreader.cloud;

import android.content.Context;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateSmsMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import type.CreateSmsInput;

public class AWSConnector {

    private static final String TAG = AWSConnector.class.getSimpleName();

    private AWSAppSyncClient mAWSAppSyncClient;
    private Context applicationContext;

    public AWSConnector(final Context applicationContext) {
        this.applicationContext = applicationContext;

        Log.i(TAG, "Initializing AWS AppSync client...");
        this.mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(applicationContext)
                .awsConfiguration(new AWSConfiguration(applicationContext))
                .build();
    }

    /**
     * Post one SMS to AWS through Amplify.
     *
     * @param phoneNumber
     * @param dateTime
     * @param message
     * @param callback
     */
    public void postNewMessage(String phoneNumber, long dateTime, String message, GraphQLCall.Callback<CreateSmsMutation.Data> callback) {
        Log.i(TAG, String.format("Received a new message to post: [number=%s, timestamp=%d, message=%s]", phoneNumber, dateTime, message));
        CreateSmsInput createSmsInput = CreateSmsInput.builder().
                number(phoneNumber).
                date(dateTime).
                message(message).
                build();

        Log.i(TAG, "Posting message to AWS AppSync client. CreateSmsInput= " + ReflectionToStringBuilder.toString(createSmsInput));
        mAWSAppSyncClient.mutate(CreateSmsMutation.builder().input(createSmsInput).build())
                .enqueue(callback == null ? new DefaultCreateSmsCallback() : callback);
    }
}
