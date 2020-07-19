package com.saab.tools.smsreader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.amazonaws.amplify.generated.graphql.CreateSmsMutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saab.tools.smsreader.R;
import com.saab.tools.smsreader.cloud.AWSConnector;
import com.saab.tools.smsreader.cloud.CreateSmsAWSCallback;
import com.saab.tools.smsreader.db.Sms;
import com.saab.tools.smsreader.db.SmsRepository;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class SmsListViewHelper {

    private static final String TAG = SmsListViewHelper.class.getSimpleName();

    private SmsRepository smsRepository;
    private SmsAdapter smsAdapter;
    private ListView smsListView;
    private Activity activity;
    private AWSConnector awsConnector;

    public SmsListViewHelper(final Activity activity, final SmsRepository smsRepository, final AWSConnector awsConnector) {
        this.activity = activity;
        this.smsRepository = smsRepository;
        this.awsConnector = awsConnector;
        this.smsAdapter = new SmsAdapter(activity, this);
        this.smsListView = activity.findViewById(R.id.list_sms);
        smsListView.setAdapter(smsAdapter);
    }

    /**
     * Search all the record from the DB and update them in the ListView of the activity.
     */
    public void updateUI() {
        Log.i(TAG, "Updating UI...");
        List<Sms> smsList = smsRepository.querySmsDb();
        smsAdapter.clear();
        smsAdapter.addAll(smsList);
        smsAdapter.notifyDataSetChanged();
    }

    /**
     * Sync new SMSs with the DB.
     *
     * Search all the inbox messages and check if they are in the DB and if not then insert.
     */
    public void searchNewSmss() {
        Log.i(TAG, "Searching new SMSs...");

        // Get all the inbox SMSs
        List<Sms> smsInboxList = smsRepository.querySmsInbox();
        Log.i(TAG, String.format("Found %d messages in the inbox", smsInboxList.size()));

        // Get all the inbox SMSs that are in the DB
        List<String> ids = smsInboxList.stream().map(Sms::getId).collect(Collectors.toList());
        List<Sms> smsDbList = smsRepository.querySmsDbById(ids);
        Log.i(TAG, String.format("From those, found %d messages already in DB", smsDbList.size()));

        // Find messages that are not in db
        List<Sms> smsNotInDbList = smsInboxList.stream()
                .filter(sms -> smsDbList.stream().noneMatch(smsDb -> sms.getId().equals(smsDb.getId())))
                .collect(Collectors.toList());
        Log.i(TAG, String.format("Found %d messages that should be created in the DB", smsNotInDbList.size()));

        // Create these messages in DB
        for (Sms smsToInsert : smsNotInDbList) {
            smsRepository.insert(smsToInsert);
        }

        // Update the list on the screen
        updateUI();

        Log.i(TAG, "Finished the search for new SMSs!");
    }

    /**
     * Search for all the SMSs that are not sent to the cloud and then send.
     */
    public void syncPendingSmss() {
        Log.i(TAG, "Searching pending SMSs to sync...");

        // Get the SMSs to sync
        List<Sms> smsListToSync = smsRepository.querySmsDbPendingSync();
        Log.i(TAG, String.format("Found %d pending messages to sync.", smsListToSync.size()));

        // Send all the messages to AWS
        for (Sms smsToSync : smsListToSync) {
            syncSmsToAws(smsToSync);
        }

        Log.i(TAG, "Finished the sync of pending SMSs!");
    }

    /**
     * Sync one single SMS with AWS.
     *
     * @param view
     */
    public void syncButtonOnClick(View view) {
        Sms sms = (Sms) view.getTag();
        syncSmsToAws(sms);
    }

    /**
     * Delegate the sync of the SMS to the AWS Connector
     * @param sms
     */
    private void syncSmsToAws(Sms sms) {
        Log.i(TAG, "Syncing SMS to AWS: " + sms.toString());
        awsConnector.postNewMessage(sms.getPhoneNumber(), sms.getDateTime(), sms.getMessage(), new CreateSmsAWSCallback(sms) {

            @Override
            public void onResponse(@Nonnull Response<CreateSmsMutation.Data> response) {
                Log.i(TAG, "SMS synced successfully! " + sms.toString());
                sms.setSynced(true);
                sms.setSyncedMessage(null);
                smsRepository.update(sms);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.i(TAG, "Error syncing sms! " + sms.toString() + " Error: " + e.getMessage());
                sms.setSynced(false);
                sms.setSyncedMessage(ExceptionUtils.getStackTrace(e));
                smsRepository.update(sms);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        });
    }

    /**
     * Reset one single SMS so it can be synced again.
     *
     * @param view
     */
    public void resetButtonOnClick(View view) {
        Sms sms = (Sms) view.getTag();
        new AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to reset the sync state of this message?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Reseting all SMSs...");
                        smsRepository.resetSync(sms);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI();
                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void resetAllSmss() {
        new AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to reset the sync state?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Reseting all SMSs...");
                        smsRepository.resetSync();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI();
                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
