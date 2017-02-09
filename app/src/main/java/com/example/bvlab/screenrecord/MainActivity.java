package com.example.bvlab.screenrecord;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;


//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RecordActivity.recording) {
            // remove notification
            RecordActivity.recording = false;

            NotificationManager nm = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(RecordActivity.REQUEST_CODE_CAPTURE_PERM);

            RecordActivity.releaseEncoders();

            try {
                FloatingView.windowManager.addView(FloatingView.view, FloatingView.params);
            } catch (Exception e) {
                return;
            }

            return;

        }

        Intent intent = new Intent(MainActivity.this, FloatingView.class);
        startService(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
