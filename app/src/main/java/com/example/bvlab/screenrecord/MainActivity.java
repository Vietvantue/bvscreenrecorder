package com.example.bvlab.screenrecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.example.bvlab.screenrecord.utils.Utils;


//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity {

    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.canDrawOverlays(MainActivity.this)) {
            startRecord();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        }
    }

    private boolean startRecord() {
        if (RecordScreenActivity.recording) {
            // remove notification
            RecordScreenActivity.recording = false;

            NotificationManager nm = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(RecordScreenActivity.REQUEST_CODE_CAPTURE_PERM);

            RecordScreenActivity.releaseEncoders();

            try {
                FloatingView.addView();
            } catch (Exception e) {
                return true;
            }
        } else {
            Intent intent = new Intent(MainActivity.this, FloatingView.class);
            startService(intent);
            finish();

            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void requestPermission(int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }

    private void needPermissionDialog(final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You need to allow permission");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Utils.canDrawOverlays(MainActivity.this)) {
                needPermissionDialog(OVERLAY_PERMISSION_REQ_CODE);
            } else {
                startRecord();
            }

        }
    }

}
