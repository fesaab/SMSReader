package com.saab.tools.smsreader.cloud;

import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateSmsMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.annotation.Nonnull;

public class DefaultCreateSmsCallback extends GraphQLCall.Callback<CreateSmsMutation.Data> {

    private final String TAG = DefaultCreateSmsCallback.class.getSimpleName();

    @Override
    public void onResponse(@Nonnull Response<CreateSmsMutation.Data> response) {
        Log.i(TAG, "SMS Successfully posted to AWS! " + ReflectionToStringBuilder.toString(response));
    }

    @Override
    public void onFailure(@Nonnull ApolloException e) {
        Log.e(TAG, "Failed to post SMS to AWS! Error: " + e.toString());
    }
}