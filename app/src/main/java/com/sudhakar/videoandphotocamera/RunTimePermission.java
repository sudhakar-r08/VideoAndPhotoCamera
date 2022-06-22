package com.sudhakar.videoandphotocamera;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;


import androidx.core.content.ContextCompat;

import java.util.ArrayList;


public class RunTimePermission extends Activity
{

    private Activity activity;
    private ArrayList<PermissionBean> arrayListPermission;
    private String[] arrayPermissions;
    private RunTimePermissionListener runTimePermissionListener;

    public RunTimePermission(Activity activity)
    {
        this.activity = activity;
    }

    public class PermissionBean
    {

        String permission;
        boolean isAccept;
    }

    public void requestPermission(String[] permissions, RunTimePermissionListener runTimePermissionListener)
    {
        this.runTimePermissionListener = runTimePermissionListener;
        arrayListPermission = new ArrayList<PermissionBean>();


        for (String permission : permissions) {
            PermissionBean permissionBean = new PermissionBean();
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionBean.isAccept = true;
            } else {
                permissionBean.isAccept = false;
                permissionBean.permission = permission;
                arrayListPermission.add(permissionBean);
            }
        }

        if (arrayListPermission.size() <= 0)
        {
            runTimePermissionListener.permissionGranted();
            return;
        }
        arrayPermissions = new String[arrayListPermission.size()];
        for (int i = 0; i < arrayListPermission.size(); i++)
        {
            arrayPermissions[i] = arrayListPermission.get(i).permission;
        }
        activity.requestPermissions(arrayPermissions, 10);
    }

    public interface RunTimePermissionListener
    {

        void permissionGranted();

        void permissionDenied();
    }

    private void callSettingActivity()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);

    }

    private void checkUpdate()
    {
        boolean isGranted = true;
        int deniedCount = 0;
        for (int i = 0; i < arrayListPermission.size(); i++)
        {
            if (!arrayListPermission.get(i).isAccept)
            {
                isGranted = false;
                deniedCount++;
            }
        }

        if (isGranted)
        {
            if (runTimePermissionListener != null)
            {
                runTimePermissionListener.permissionGranted();
            }
        }
        else
        {
            if (runTimePermissionListener != null)
            {
//                if (deniedCount == arrayListPermission.size())
//                {
                    setAlertMessage();

//                }
                runTimePermissionListener.permissionDenied();
            }
        }
    }

    public void setAlertMessage()
    {
        AlertDialog.Builder adb;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adb = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            adb = new AlertDialog.Builder(activity);
        }

        adb.setTitle(activity.getResources().getString(R.string.app_name));
        String msg = "<p>Dear User, </p>" +
                "<p>Seems like you have <b>\"Denied\"</b> the minimum requirement permission to access more features of application.</p>" +
                "<p>You must have to <b>\"Allow\"</b> all permission. We will not share your data with anyone else.</p>" +
                "<p>Do you want to enable all requirement permission ?</p>" +
                "<p>Go To : Settings >> App > " + activity.getResources().getString(R.string.app_name) + " Permission : Allow ALL</p>";

        adb.setMessage(Html.fromHtml(msg));
        adb.setPositiveButton("Allow All", new AlertDialog.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                callSettingActivity();
                dialog.dismiss();
            }
        });

        adb.setNegativeButton("Remind Me Later", new AlertDialog.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        if (!((Activity) activity).isFinishing() && msg.length() > 0)
        {
            adb.show();
        }
        else
        {
            Log.v("log_tag", "either activity finish or message length is 0");
        }
    }

    private void updatePermissionResult(String permissions, int grantResults)
    {

        for (int i = 0; i < arrayListPermission.size(); i++)
        {
            if (arrayListPermission.get(i).permission.equals(permissions))
            {
                if (grantResults == 0)
                {
                    arrayListPermission.get(i).isAccept = true;
                }
                else
                {
                    arrayListPermission.get(i).isAccept = false;
                }
                break;
            }
        }

    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        for (int i = 0; i < permissions.length; i++)
        {
            updatePermissionResult(permissions[i], grantResults[i]);
        }
        checkUpdate();
    }


}
