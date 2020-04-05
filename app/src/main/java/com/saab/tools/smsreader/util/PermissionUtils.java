package com.saab.tools.smsreader.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1;

    public static boolean hasPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if the permissions are granted and if not request them.
     */
    public static void ensurePermissionsAreGranted(Activity activity, String... permissions) {
        // Check if any permission is missing
        List<String> missingPermissions = new ArrayList<>();
        List<String> alreadyGrantedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (!hasPermission(activity, permission)) {
                missingPermissions.add(permission);
            } else {
                alreadyGrantedPermissions.add(permission);
            }
        }

        Log.i(TAG, String.format("Missing permissions=[%s], Already granted permissions=[%s]",
                String.join(",", missingPermissions),
                String.join(",", alreadyGrantedPermissions)));

        // If so, request the permissions
        if (!missingPermissions.isEmpty()) {
            Log.i(TAG, String.format("Requesting permissions to [%s]", String.join(",", missingPermissions)));
            ActivityCompat.requestPermissions(activity,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    PERMISSION_REQUEST_CODE);
        } else {
            Log.i(TAG, String.format("Permissions already granted to [%s]", String.join(",", alreadyGrantedPermissions)));
        }
    }

    /**
     * Check the results of the requested permissions and show the results to the user.
     */
    public static void handleRequestPermissionResults(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> grantedPermissions = new ArrayList<>();
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int permissionStatus = grantResults[i];

                if (PackageManager.PERMISSION_GRANTED == permissionStatus) {
                    grantedPermissions.add(permission);
                } else {
                    deniedPermissions.add(permission);
                }
            }

            StringBuilder message = new StringBuilder();
            if (!grantedPermissions.isEmpty()) {
                message.append(String.format("Granted permissions: %s. ", String.join(",", grantedPermissions)));
            }
            if (!deniedPermissions.isEmpty()) {
                message.append(String.format("Denied permissions: %s. ", String.join(",", deniedPermissions)));
            }

            Log.i(TAG, message.toString());
            Toast.makeText(activity, message.toString(), Toast.LENGTH_SHORT).show();
        }

    }
}
