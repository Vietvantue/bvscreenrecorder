package com.bvlab.screenrecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bvlab.screenrecord.R;

public class StopRecordReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        try {
            RecordScreenActivity.releaseEncoders();
            Toast.makeText(context, context.getString(R.string.string_record_stop), Toast.LENGTH_LONG).show();
//        context.startService(new Intent(context, FloatingView.class));
            FloatingView.addView();
            if (!checkSystemWritePermission()) {
                Intent settingIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(settingIntent);
            } else {
                Settings.System.putInt(context.getContentResolver(), "show_touches", 0);
            }

        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context);
        }
        return retVal;
    }
}
