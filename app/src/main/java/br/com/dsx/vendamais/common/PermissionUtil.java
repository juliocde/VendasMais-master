package br.com.dsx.vendamais.common;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.R;


/**
 * Created by rmk on 18/11/15.
 */
public class PermissionUtil {

    public static final int OPTIONAL_REQUEST = 101;
    public static final int REQUIRED_REQUEST = 102;
    public static final int PERMISSION_LIST_REQUEST = 103;
    public static final int REQUEST_PERMISSION_SETTING = 244;


    public static boolean hasPermission(Context mContext, String[] permissionList, String message, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isPermissionsGranted(mContext, permissionList)) {
                return true;
            } else {
                if (canShowRationaleMessage(mContext, permissionList) && message != null) {
                    displayAlertDialogWithPermissionExplanation(mContext, message, permissionList, requestCode);
                } else {
                    displayRunTimePermissionDialogue(mContext, permissionList, requestCode);
                }
                return false;
            }
        } else {
            return true;
        }
    }

    private static boolean isPermissionsGranted(Context mContext, String[] permissionlist) {
        boolean isPermissionGranted = true;
        for (String permission : permissionlist)
            if (ContextCompat.checkSelfPermission(mContext,
                    permission)
                    == PackageManager.PERMISSION_DENIED)
                isPermissionGranted = false;
        return isPermissionGranted;
    }

    private static boolean canShowRationaleMessage(Context mContext, String[] permissionlist) {
        boolean canShowRationaleMessage = false;
        for (String permission : permissionlist)
            if (ActivityCompat.shouldShowRequestPermissionRationale(((Activity) mContext), permission))
                canShowRationaleMessage = true;
        return canShowRationaleMessage;
    }

    /**
     * @param mContext
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param message
     */

    public static void onRequestPermissionsResult(Context mContext, int requestCode, String[] permissions, int[] grantResults, String message) {
        if (isPermissionGranted(grantResults)) {
            PermissionApi.getInstance().onPermissionsGranted(requestCode);
        } else {
            if (message != null) {
                handlePermissionDenied(mContext, requestCode, permissions, message);
            } else {
                PermissionApi.getInstance().onPermissionsDenied(requestCode);
            }
        }
    }

    private static void handlePermissionDenied(Context mContext, int requestCode, String[] permissions, String message) {
        if (canShowRationaleMessage(mContext, permissions)) {
            displayAlertDialogWithPermissionExplanation(mContext, message, permissions, requestCode);
        } else {
            displayAlertDialogToTurnOnPermission(mContext, message);
        }
    }

    private static boolean isPermissionGranted(int[] grantResults) {
        boolean isPermissionGranted = true;
        if (grantResults.length <= 0)
            return false;
        for (int result : grantResults)
            if (result == PackageManager.PERMISSION_DENIED)
                isPermissionGranted = false;
        return isPermissionGranted;
    }

    /*All Alert Dialog messages*/
    private static void displayRunTimePermissionDialogue(Context mContext, String[] permissionList, int requestCode) {
        ActivityCompat.requestPermissions(((Activity) mContext), getDeniedPermissionList(mContext, permissionList), requestCode);
    }

    public static void displayAlertDialogToTurnOnPermission(final Context mContext, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                        intent.setData(uri);
                        ((Activity) mContext).startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void displayAlertDialogWithPermissionExplanation(final Context mContext, String message, final String[] permissionList, final int requestCode) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        displayRunTimePermissionDialogue(mContext, permissionList, requestCode);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Used for filtering out already granted permission so that the user is not asked twice for these permissions
     *
     * @param permissionList
     * @return
     */
    private static String[] getDeniedPermissionList(Context mContext, String[] permissionList) {
        List<String> deniedPermissionArrayList = new ArrayList<>();
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(mContext,
                    permission)
                    == PackageManager.PERMISSION_DENIED)
                deniedPermissionArrayList.add(permission);
        }
        return deniedPermissionArrayList.toArray(new String[0]);
    }


    public static String[] getOptionalPermissions() {
        return new String[]{Manifest.permission.RECEIVE_SMS};
    }

    public static String[] getPermissionList() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS};
    }

    public static String[] getPermissionReadExternalStorage() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    public static String[] getPermissionCamera() {
        return new String[]{Manifest.permission.CAMERA};
    }

    public static void onRequestPermissionsResult(Context mContext, int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case OPTIONAL_REQUEST:
                /**
                 * Passing null here instead of rationale message will result in permission being treated as denied
                 * instead of showing rationale message
                 */
                PermissionUtil.onRequestPermissionsResult(mContext, requestCode, permissions, grantResults, null);
                break;
            case REQUIRED_REQUEST:
                PermissionUtil.onRequestPermissionsResult(mContext, requestCode, permissions, grantResults, mContext.getString(R.string.message_permission_rationale));
                break;
            case PERMISSION_LIST_REQUEST:
                PermissionUtil.onRequestPermissionsResult(mContext, requestCode, permissions, grantResults, mContext.getString(R.string.message_permission_rationale));
                break;
        }

    }

}
