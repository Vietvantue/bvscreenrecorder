package com.bvlab.screenrecord;


import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bvlab.screenrecord.R;

public class FloatingView extends Service {

    RelativeLayout record_move, capture_move, review_move, setting_move;
    ImageView record;
    ImageView capture;
    ImageView review;
    ImageView record_setting;
    RelativeLayout close;
    public static WindowManager windowManager;
    public static ViewGroup view;
    public static WindowManager.LayoutParams params;
    int check = 0;
    int screenWidth = 0, screenHeight = 0;

    public static boolean hasAddView = false;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        view = (ViewGroup) inflater.inflate(R.layout.floatingview, null);
        record = (ImageView) view.findViewById(R.id.record);
        capture = (ImageView) view.findViewById(R.id.capture);
        review = (ImageView) view.findViewById(R.id.review);
        record_setting = (ImageView) view.findViewById(R.id.record_setting);
        close = (RelativeLayout) view.findViewById(R.id.close);

        capture_move = (RelativeLayout) view.findViewById(R.id.capture_move);
        record_move = (RelativeLayout) view.findViewById(R.id.record_move);
        review_move = (RelativeLayout) view.findViewById(R.id.review_move);
        setting_move = (RelativeLayout) view.findViewById(R.id.setting_move);

        getWidthHeight();

        Touch();
        Click();
    }

    public void getWidthHeight() {
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    public void Touch() {
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = screenWidth / 2;
        params.y = screenHeight / 2;
        addView();
        View[] v = {record_move, capture_move, review_move, setting_move, close};
        for (int i = 0; i < 5; i++) {
            try {
                v[i].setOnTouchListener(new View.OnTouchListener() {
                    private WindowManager.LayoutParams paramsF = params;
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // Get current time in nano seconds.
                                check = 0;
                                initialX = paramsF.x;
                                initialY = paramsF.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_UP:
                                Log.e("check", String.valueOf(check));
                                Click();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                check++;
                                paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                                paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                                windowManager.updateViewLayout(view, paramsF);
                                break;
                        }
                        return false;
                    }
                });
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    public void Click() {
        Log.e("checked", String.valueOf(check));
        if (check >= 10) {
            View[] v = {record_move, capture_move, review_move, setting_move};
            for (int i = 0; i < 4; i++) {
                v[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("clicked", "true");
                    }
                });
            }
            return;
        }
        record_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent recordIntent = new Intent(getBaseContext(), RecordScreenActivity.class);
//                recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(recordIntent);

                Intent recordIntent = new Intent(getBaseContext(), CountDownActivity.class);
                recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(recordIntent);

//                stopSelf();
//                Toast.makeText(getApplicationContext(), "record clicked", Toast.LENGTH_SHORT).show();
            }
        });

        capture_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent captureIntent = new Intent(getBaseContext(), ScreenShotActivity.class);
                captureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(captureIntent);
                removeView();
//                stopSelf();
            }
        });
        review_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                stopSelf();
//                Settings.System.putInt(getContentResolver(), "show_touches", 0);
                Intent i = new Intent(getApplicationContext(), GalleryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        setting_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                i.putExtra("setting_start", false);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                removeView();
//                stopSelf();
//                Toast.makeText(getApplicationContext(), "record setting", Toast.LENGTH_SHORT).show();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null) removeView();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Settings.System.putInt(getContentResolver(), "show_touches", 0);
        }
        //stopSelf();
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
        }
        return retVal;
    }

    public static void addView() {
        if (!hasAddView && windowManager != null && view != null) {
            windowManager.addView(view, params);
            hasAddView = true;
        }
    }

    public static void removeView() {
        if (windowManager != null && view != null) {
            windowManager.removeView(view);
            hasAddView = false;
        }
    }

}