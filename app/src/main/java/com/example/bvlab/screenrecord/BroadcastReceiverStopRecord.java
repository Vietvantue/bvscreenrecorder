package com.example.bvlab.screenrecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class BroadcastReceiverStopRecord extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            RecordActivity.releaseEncoders();
            Toast.makeText(context, context.getString(R.string.string_record_stop), Toast.LENGTH_LONG).show();
//        context.startService(new Intent(context, FloatingView.class));
            Settings.System.putInt(context.getContentResolver(), "show_touches", 0);

            FloatingView.windowManager.addView(FloatingView.view, FloatingView.params);
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }
}
