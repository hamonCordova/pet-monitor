package br.com.hamon.petmonitor;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionUtils {

    //Empty Constructor
    public PermissionUtils () {}

    /**
     * Verify if need ask for permissions
     * @param permissions
     * @param activity
     * @return true if needed, instead, false
     */
    public static boolean verifyAndAskForPermissions(String[] permissions, Activity activity) {

        boolean needAskForPermissions = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {

                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    needAskForPermissions = true;
                    break;
                }

            }

            //Verify if need ask for permissions
            if (needAskForPermissions) {
                ActivityCompat.requestPermissions(activity, permissions, 0);
            }

        }

        return needAskForPermissions;

    }

    /**
     * Verify if all permissions are granted
     * @param grantResults
     * @return true if all are granted, instead false
     */
    public static boolean verifyOnRequestPermissionsResult (int[] grantResults) {

        boolean allPermissionsGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        return allPermissionsGranted;

    }

}
