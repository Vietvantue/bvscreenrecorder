package com.example.bvlab.screenrecord;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.bvlab.screenrecord.FloatingView.view;


public class ScreenShotActivity extends Activity {

    private static final String TAG = ScreenShotActivity.class.getName();
    private static final int REQUEST_CODE = 1909;
    private static MediaProjection MEDIA_PROJECTION;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static boolean isCaptured = false;
    private boolean capturedImageSuccess = false;
    private final DateFormat fileFormat =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss'.PNG'", Locale.US);
    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private int mWidth;
    private int mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        startProjection();
        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();

    }

    private void startProjection() {
        if (!isCaptured) {
            isCaptured = true;
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        }
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //processing = false;
                if (MEDIA_PROJECTION != null) MEDIA_PROJECTION.stop();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                MEDIA_PROJECTION = mProjectionManager.getMediaProjection(resultCode, data);
                if (MEDIA_PROJECTION != null) {
                    STORE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BVscreenshots/";
                    File storeDirectory = new File(STORE_DIRECTORY);
                    if (!storeDirectory.exists()) {
                        boolean success = storeDirectory.mkdirs();
                        if (!success) {
                            Log.e(TAG, "failed to create file storage directory.");
                            return;
                        }
                    }

                    DisplayMetrics metrics = getResources().getDisplayMetrics();
                    int density = metrics.densityDpi;
                    int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    mWidth = size.x;
                    mHeight = size.y;

                    mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
                    MEDIA_PROJECTION.createVirtualDisplay("screencap", mWidth, mHeight, density, flags, mImageReader.getSurface(), null, mHandler);
                    mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
                }
            } else {
                Log.e("finish", "finish");
                finish();
                try {
                    FloatingView.addView();
                } catch (IllegalArgumentException | NullPointerException e) {
                    Log.e("error", e.toString());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;
            try {
                if (capturedImageSuccess) {
                    Toast.makeText(getApplicationContext(), getString(R.string.string_screenshot_saved), Toast.LENGTH_LONG).show();
                    stopProjection();

                    if (view != null) {
                        FloatingView.addView();
                    }
                    finish();
                    return;
                }

                MediaPlayer mPlayer = MediaPlayer.create(ScreenShotActivity.this, R.raw.raw_camera_shutter_click);
                mPlayer.start();
                finish();
                image = mImageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;
                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    // write bitmap to a file
                    String outputName = fileFormat.format(new Date());
                    fos = new FileOutputStream(STORE_DIRECTORY + outputName);
                    bitmap.compress(CompressFormat.PNG, 100, fos);
                    updateMedia(STORE_DIRECTORY + outputName);
                    mPlayer.stop();
                    IMAGES_PRODUCED++;
                    Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                capturedImageSuccess = true;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

    private void updateMedia(final String s) {
        final Context applicationContext = this.getApplicationContext();
        MediaScannerConnection.scanFile(applicationContext, new String[]{s}, null, null);
    }

}
