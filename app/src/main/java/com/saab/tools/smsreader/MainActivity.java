package com.saab.tools.smsreader;

import android.Manifest;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.CreateSmsMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saab.tools.smsreader.util.PermissionUtils;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.annotation.Nonnull;

import type.CreateSmsInput;

public class MainActivity extends AppCompatActivity implements SmsListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AWSAppSyncClient mAWSAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Ensuring necessary permissions...");
        PermissionUtils.ensurePermissionsAreGranted(this, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        MessageReceiver.setListener(this);
    }

    /**
     * Processes permission request codes.
     *
     * @param requestCode  The request code passed in requestPermissions()
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i(TAG, String.format("Permissions request response: [permissions=%s, results=%]", permissions, grantResults));
        PermissionUtils.handleRequestPermissionResults(this, requestCode, permissions, grantResults);
    }

    /**
     * Listen to new SMSs.
     */
    @Override
    public void newSms(SmsMessage smsMessage) {
        Log.i(TAG, String.format("Received SMS: [%s]", ReflectionToStringBuilder.toString(smsMessage)));

        String message = String.format("Sender: %s, Email From: %s, Email body: %s, " +
                        "Display message body: %s, Time in millisecond: %d, Message: %s",
                smsMessage.getDisplayOriginatingAddress(),
                smsMessage.getEmailFrom(),
                smsMessage.getEmailBody(),
                smsMessage.getDisplayMessageBody(),
                smsMessage.getTimestampMillis(),
                smsMessage.getMessageBody());
        Log.i(TAG, String.format("SMS details: [%s]", message));

        Toast.makeText(this, "Received SMS: " + message, Toast.LENGTH_SHORT).show();

        // Post all Nedbank messages to AWS!
        if (smsMessage.getMessageBody().startsWith("Nedbank")) {
            Log.i(TAG, "About to post message to AWS...");
            CreateSmsInput createSmsInput = CreateSmsInput.builder().
                    number(smsMessage.getDisplayOriginatingAddress()).
                    date(smsMessage.getTimestampMillis()).
                    message(smsMessage.getMessageBody()).
                    build();

            mAWSAppSyncClient.mutate(CreateSmsMutation.builder().input(createSmsInput).build())
                    .enqueue(mutationCallback);
        } else {
            Log.i(TAG, "This message is not going to be posted to AWS.");
        }
    }

    private GraphQLCall.Callback<CreateSmsMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateSmsMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateSmsMutation.Data> response) {
            Log.i(TAG, "SMS Successfully posted to AWS! " + ReflectionToStringBuilder.toString(response));
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };
}
