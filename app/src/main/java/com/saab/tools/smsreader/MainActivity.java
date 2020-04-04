package com.saab.tools.smsreader;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateSmsInput;

public class MainActivity extends AppCompatActivity implements SmsListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SMS_PREFIX_TO_SHOW = "Nedbank";

    private AWSAppSyncClient mAWSAppSyncClient;

    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Ensuring necessary permissions...");
        PermissionUtils.ensurePermissionsAreGranted(this, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS);

        Log.i(TAG, "Initializing AWS AppSync client...");
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        Log.i(TAG, "Flagging the MainActivity to listen to SMSs...");
        MessageReceiver.setListener(this);

        mTaskListView = (ListView) findViewById(R.id.list_sms);
        updateUI();

        // TODO terminar esse tutorial para listar todos os SMSs aqui e adicionar uma acao no botao SYNC ALL
        // https://www.sitepoint.com/starting-android-development-creating-todo-app/
    }

    private void updateUI() {
        List<String> taskList = new ArrayList<>();
        Log.i(TAG, "Querying SMSs...");
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
        Cursor cursor1 = getContentResolver().query(mSmsinboxQueryUri,
                new String[] { "_id", "body" }, null, null, null);

        Log.i(TAG, "Querying SMSs returned " + cursor1.getCount() + " results");
        for (int i = 0; i < cursor1.getCount(); i++) {
            cursor1.moveToPosition(i);
            String id = cursor1.getString(0);
            String body = cursor1.getString(1);
            Log.i(TAG, String.format("SMS %d: %s", i, body));
            if (body.startsWith(SMS_PREFIX_TO_SHOW)) {
                taskList.add(body);
            }
        }

        // TODO: create a custom adapter to add a POJO here!
        // TODO: add support to SQLite to persist if a message was synced or not!
        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_sms,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync_all:
                Log.i(TAG, "Searching all SMSs...");
                updateUI();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void syncSms(View view) {
        TextView smsTextView = ((View)view.getParent()).findViewById(R.id.task_title);
        String smsText = String.valueOf(smsTextView.getText());
        Log.i(TAG, String.format("Syncing sms '%s'", smsText));
    }
}
