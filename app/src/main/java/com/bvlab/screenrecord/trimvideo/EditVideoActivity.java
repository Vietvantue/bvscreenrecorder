package com.bvlab.screenrecord.trimvideo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bvlab.screenrecord.R;
import com.bvlab.screenrecord.GalleryActivity;

import java.io.File;

import com.bvlab.screenrecord.record.VideoArrayAdapter;

public class EditVideoActivity extends Activity {

    private static final String TAG = EditVideoActivity.class.getSimpleName();
    String filePrefix;
    TextView startTime, endTime;
    VideoSliceSeekBar videoSplitSeekbar;
    VideoView videoView;
    View trimVideo;
    private int duration = -1;
    private int rightthumb = -1;
    private int leftthumb = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_video_activity);
        startTime = (TextView) findViewById(R.id.left_pointer);
        endTime = (TextView) findViewById(R.id.right_pointer);

        videoSplitSeekbar = (VideoSliceSeekBar) findViewById(R.id.seek_bar);
        videoView = (VideoView) findViewById(R.id.video);
//        performVideoViewClick();
        Uri vidFile = Uri.parse(VideoArrayAdapter.videoSelectedPath);
        //videoView.setVideoPath(vidFile.toString());
        videoView.setVideoURI(vidFile);
        videoView.setMediaController(new MediaController(this));
//        videoView.requestFocus();
        videoView.start();
        trimVideo = findViewById(R.id.trimVideo);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), vidFile);
        duration = mp.getDuration();

        if (duration < 0) {
            Toast.makeText(getApplicationContext(), "Can't trim this video !", Toast.LENGTH_SHORT).show();
            finish();
        }

        startTime.setText(getTimeForTrackFormat(0, true));
        endTime.setText(getTimeForTrackFormat(duration, true));

        rightthumb = duration;
        leftthumb = 0;

        videoSplitSeekbar.setSeekBarChangeListener(new VideoSliceSeekBar.SeekBarChangeListener() {
            @Override
            public void SeekBarValueChanged(int leftThumb, int rightThumb) {
                //Toast.makeText(getApplicationContext(), leftThumb + "    " + rightThumb, Toast.LENGTH_SHORT).show();
                if (VideoSliceSeekBar.selectedThumb == VideoSliceSeekBar.SELECT_THUMB_LEFT) {
                    videoView.seekTo(leftThumb * duration / 100);
                    startTime.setText(getTimeForTrackFormat(leftThumb * duration / 100, true));

                } else {
                    videoView.seekTo(rightThumb * duration / 100);
                    endTime.setText(getTimeForTrackFormat(rightThumb * duration / 100, true));
                }

                rightthumb = rightThumb * duration / 100;
                leftthumb = leftThumb * duration / 100;
                //trim();
            }

        });


        trimVideo.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             executeTrimCommand(new File(VideoArrayAdapter.videoSelectedPath), leftthumb, rightthumb);
                                         }
                                     }

        );
    }


    public static String getTimeForTrackFormat(int timeInMills, boolean display2DigitsInMinsSection) {
        int minutes = (timeInMills / (60 * 1000));
        int seconds = (timeInMills - minutes * 60 * 1000) / 1000;
        String result = display2DigitsInMinsSection && minutes < 10 ? "0" : "";
        result += minutes + ":";
        if (seconds < 10) {
            result += "0" + seconds;
        } else {
            result += seconds;
        }
        return result;
    }


    private StateObserver videoStateObserver = new StateObserver();

    private class StateObserver extends Handler {

        private boolean alreadyStarted = false;

        private void startVideoProgressObserving() {
            if (!alreadyStarted) {
                alreadyStarted = true;
                sendEmptyMessage(0);
            }
        }

        private Runnable observerWork = new Runnable() {
            @Override
            public void run() {
                startVideoProgressObserving();
            }
        };

        @Override
        public void handleMessage(Message msg) {
            alreadyStarted = false;
            videoSplitSeekbar.videoPlayingProgress(videoView.getCurrentPosition());
            if (videoView.isPlaying() && videoView.getCurrentPosition() < videoSplitSeekbar.getRightProgress()) {
                postDelayed(observerWork, 50);
            } else {

                if (videoView.isPlaying()) videoView.pause();

                videoSplitSeekbar.setSliceBlocked(false);
                videoSplitSeekbar.removeVideoStatusThumb();
            }
        }
    }

    private void executeTrimCommand(File file, int startMs, int endMs) {
        File moviesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getResources().getString(R.string.string_store_video_folder) + "/");
        String s = new File(VideoArrayAdapter.videoSelectedPath).getName();
        filePrefix = s.substring(0, s.length() - 4);
        String fileExtn = ".mp4";

        try {
            File dest = new File(moviesDir, filePrefix + "_1" + fileExtn);
            if (dest.exists()) {
                dest.delete();
            }

            ShortenExample.crop(file, dest, startMs / 1000, endMs / 1000);

            Toast.makeText(getApplicationContext(), "Done.  File saved at " + dest.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent(getApplicationContext(), GalleryActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        super.onDestroy();
    }
}
