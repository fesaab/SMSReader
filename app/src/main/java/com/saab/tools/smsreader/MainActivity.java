package com.saab.tools.smsreader;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.saab.tools.smsreader.cloud.AWSConnector;
import com.saab.tools.smsreader.db.SmsDbHelper;
import com.saab.tools.smsreader.db.SmsRepository;
import com.saab.tools.smsreader.sms.MessageReceiver;
import com.saab.tools.smsreader.ui.SmsListViewHelper;
import com.saab.tools.smsreader.util.PermissionUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AWSConnector awsConnector;
    private SmsRepository smsRepository;
    private SmsListViewHelper smsListViewHelper;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Ensuring necessary permissions...");
        PermissionUtils.ensurePermissionsAreGranted(this, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS);

        Log.i(TAG, "Initializing AWS AppSync client...");
        this.awsConnector = new AWSConnector(getApplicationContext());

        Log.i(TAG, "Retrieving, storing and configuring SmsListView...");
        smsRepository = new SmsRepository(getContentResolver(), new SmsDbHelper(this));
        smsListViewHelper = new SmsListViewHelper(this, smsRepository, awsConnector);
        smsListViewHelper.updateUI();

        Log.i(TAG, "Configure the MessageReceiver to listen to SMSs...");
        MessageReceiver.init(smsListViewHelper);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
            case R.id.action_search_all:
                Log.i(TAG, "Searching new SMSs...");
                smsListViewHelper.searchNewSmss();
                return true;

            case R.id.action_sync_all:
                Log.i(TAG, "Syncing all the pending SMSs with the cloud...");
                smsListViewHelper.syncPendingSmss();
                return true;

            case R.id.action_reset_all:
                Log.i(TAG, "Reseting all the SMSs already synced...");
                smsListViewHelper.resetAllSmss();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
