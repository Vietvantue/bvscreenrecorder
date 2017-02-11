package com.example.bvlab.screenrecord;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class RecordScreenActivity extends Activity {
    public static boolean recording = false;
    public static MediaProjectionManager mMediaProjectionManager;
    public static MediaProjection mMediaProjection;
    public static MediaRecorder recorder;
    public static VirtualDisplay display;
    static String videoName;
    boolean audio;
    boolean touch;
    int bitrate;
    int width, height;
    /**
     * Default value
     */
    public static int REQUEST_CODE_CAPTURE_PERM = 1101;
    private static String STORE_DIRECTORY;
    private final DateFormat fileFormat =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss'.mp4'", Locale.US);
    Configuration configuration;
    private Resolution resolution;
    ResolutionAdapter resolutionAdapter;
    Context context;

    static final class RecordingInfo {
        final int width;
        final int height;
        final int frameRate;
        final int density;

        RecordingInfo(int width, int height, int frameRate, int density) {
            this.width = width;
            this.height = height;
            this.frameRate = frameRate;
            this.density = density;
        }
    }


    public void getSetting() {
        SharedPreferences pre = getSharedPreferences("settings", MODE_PRIVATE);
        audio = pre.getBoolean("audio", false);
        touch = pre.getBoolean("touch", false);
        bitrate = pre.getInt("bitrate", 8);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recording = true;
//        if (mMediaProjection != null) {
//            mMediaProjection.stop();
//           mMediaProjection = null;
//       }
        context = this;
        getSetting();

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_PERM);
        setConfiguration(getResources().getConfiguration());

    }

    @Override
    protected void onResume() {
        if (mMediaProjection != null) {
            releaseEncoders();
        }
        super.onResume();
    }

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, StopRecordReceiver.class), 0);
        Notification.Builder mBuilder;

        mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_record)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.string_start_record))
                .setStyle(new Notification.BigTextStyle()
                        .bigText(getString(R.string.string_tap_to_stop_record)))
                .setContentText(getString(R.string.string_stop_record))
                .setLights(Color.YELLOW, 500, 1000)
                .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(REQUEST_CODE_CAPTURE_PERM, mBuilder.build());
        //REQUEST_CODE_CAPTURE_PERM++;

    }

    private void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        if (touch) {
            Settings.System.putInt(getContentResolver(), "show_touches", 1);
        }

        Resolution screenResolution = ResolutionHelper.getScreenResolution(this);

        if (resolution != null) {
            if (!screenResolution.isPortrait()) {
                resolution.rotate();
            }
//            this.resolution = resolution;
        } else {
            this.resolution = screenResolution;
        }
        int orientation = configuration.orientation;
        if ("ORIENTATION_CURRENT".equals("ORIENTATION_LANDSCAPE")) {
            orientation = 2;
        } else if ("ORIENTATION_CURRENT".equals("ORIENTATION_PORTRAIT")) {
            orientation = 1;
        }
        this.updateResolutionAdapterWithOrientation(orientation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CAPTURE_PERM) {
            if (resultCode == RESULT_OK) {
                mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                startRecording(); // defined below
            } else {
//                startService(new Intent(getApplicationContext(), FloatingView.class));
                try {
                    FloatingView.addView();
                } catch (IllegalArgumentException | NullPointerException e) {
                    Log.e("error", e.toString());
                }
                finish();
                // user did not grant permissions
            }
        }
    }

    private RecordingInfo getRecordingInfo() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        int displayDensity = displayMetrics.densityDpi;

        Configuration configuration = context.getResources().getConfiguration();
        boolean isLandscape = configuration.orientation == ORIENTATION_LANDSCAPE;

        // Get the best camera profile available. We assume MediaRecorder supports the highest.
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        int cameraWidth = camcorderProfile != null ? camcorderProfile.videoFrameWidth : -1;
        int cameraHeight = camcorderProfile != null ? camcorderProfile.videoFrameHeight : -1;
        int cameraFrameRate = camcorderProfile != null ? camcorderProfile.videoFrameRate : 30;

        int sizePercentage = 100;

        return calculateRecordingInfo(displayWidth, displayHeight, displayDensity, isLandscape,
                cameraWidth, cameraHeight, cameraFrameRate, sizePercentage);
    }


    static RecordingInfo calculateRecordingInfo(int displayWidth, int displayHeight,
                                                int displayDensity, boolean isLandscapeDevice, int cameraWidth, int cameraHeight,
                                                int cameraFrameRate, int sizePercentage) {
        // Scale the display size before any maximum size calculations.
        displayWidth = displayWidth * sizePercentage / 100;
        displayHeight = displayHeight * sizePercentage / 100;

        if (cameraWidth == -1 && cameraHeight == -1) {
            // No cameras. Fall back to the display size.
            return new RecordingInfo(displayWidth, displayHeight, cameraFrameRate, displayDensity);
        }

        int frameWidth = isLandscapeDevice ? cameraWidth : cameraHeight;
        int frameHeight = isLandscapeDevice ? cameraHeight : cameraWidth;
        if (frameWidth >= displayWidth && frameHeight >= displayHeight) {
            // Frame can hold the entire display. Use exact values.
            return new RecordingInfo(displayWidth, displayHeight, cameraFrameRate, displayDensity);
        }

        // Calculate new width or height to preserve aspect ratio.
        if (isLandscapeDevice) {
            frameWidth = displayWidth * frameHeight / displayHeight;
        } else {
            frameHeight = displayHeight * frameWidth / displayWidth;
        }
        return new RecordingInfo(frameWidth, frameHeight, cameraFrameRate, displayDensity);
    }


    private void startRecording() {


        recorder = new MediaRecorder();
        if (audio) {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }

        RecordingInfo recordingInfo = getRecordingInfo();

        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoFrameRate(recordingInfo.frameRate);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setVideoSize(recordingInfo.width, recordingInfo.height);
        recorder.setVideoEncodingBitRate(bitrate * 1000 * 1000);
        if (audio) {
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }

//        mCamera= Camera.open();
//        Camera.Parameters parameters=mCamera.getParameters();
//        parameters.setPreviewSize(352,288);
//        parameters.set("orientation","portrait");
//        mCamera.setParameters(parameters);
//        mCamera.unlock();
//        mRecorder.setCamera(mCamera);
//        Thread.sleep(1000);
//        mCamera.lock();
//        mCamera.release();
//        videoRecordedResult=validateVideo(MediaNames.RECORDED_PORTRAIT_H263,352,288);


        STORE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getResources().getString(R.string.string_store_video_folder) + "/";
        File storeDirectory = new File(STORE_DIRECTORY);
        if (!storeDirectory.exists()) {
            boolean success = storeDirectory.mkdirs();
            if (!success) {
                Log.e("error", "failed to create file storage directory.");
                return;
            }
        }
        String getTime = fileFormat.format(new Date());
        File fileVideo = new File(STORE_DIRECTORY + getTime);
        String fileVideoPath = fileVideo.getAbsolutePath();
        videoName = fileVideoPath;

//        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        Uri fileContentUri = Uri.fromFile(fileVideo);
//        mediaScannerIntent.setData(fileContentUri);
//        this.sendBroadcast(mediaScannerIntent);
        addVideo(fileVideo, fileVideoPath);
        Log.e("video file:", fileVideoPath);

        recorder.setOutputFile(fileVideoPath);
        try {
            recorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException("Unable to prepare MediaRecorder.", e);
        }
        // Get the display size and density.
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        int screenDensity = metrics.densityDpi;

//        String s = screenWidth + "_" + screenHeight + "_" + screenDensity;
//        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

        Surface surface = recorder.getSurface();
        display = mMediaProjection.createVirtualDisplay("Recording Display", recordingInfo.width, recordingInfo.height,
                recordingInfo.density, DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION, surface, null, null);
        recorder.start();
        showNotification();
        finish();
    }

    public Uri addVideo(File videoFile, String title) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, title);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/*");
        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        return getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void updateResolutionAdapterWithOrientation(int orientation) {
        this.resolutionAdapter = ResolutionAdapter.createAdapter(this, orientation);
//        this.spinnerResolution.setAdapter(this.resolutionAdapter);
//        this.spinnerResolution.setSelection(this.resolutionAdapter.getPosition(this.resolution));
    }

    public static void releaseEncoders() {
        try {
            if (mMediaProjection != null) {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
                recorder.release();
            }
            if (display != null) {
                display.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
//        startService(new Intent(getApplicationContext(), FloatingView.class));
        super.onDestroy();
    }
}
