package com.example.bvlab.screenrecord;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.example.bvlab.screenrecord.capture.ImageAdapter;
import com.example.bvlab.screenrecord.record.VideoArrayAdapter;
import com.example.bvlab.screenrecord.record.VideoList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    ArrayList<VideoList> listVideo = new ArrayList<VideoList>();
    private static Context mContext;
    Fragment tmpFragment;
    ListView listView;
    View noVideoView, noImageView;
    VideoArrayAdapter adapter;

    private String tabTitles[] = new String[]{"Video", "Image"};

    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        tmpFragment = new SlidePageSupportFragment();
        ((SlidePageSupportFragment) tmpFragment).setPageNumber(position);
        return tmpFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


    public void bindViewPage1(ViewGroup my_view) {
        listView = (ListView) my_view.findViewById(R.id.video);
        noVideoView = my_view.findViewById(R.id.layout_no_video);
        createData();
        adapter = new VideoArrayAdapter(mContext, R.layout.item_video, listVideo);
//        adapter.sort(new Comparator<VideoList>() {
//            public int compare(VideoList arg0, VideoList arg1) {
//                return arg0.getVideo_name().compareTo(arg1.getVideo_name());
//            }
//        });
        if (listVideo != null && listVideo.size() > 0) {
            listView.setAdapter(adapter);
        } else {
            noVideoView.setVisibility(View.VISIBLE);
        }
//        adapter.notifyDataSetChanged();
    }


    public void bindViewPage2(ViewGroup my_view) {
        File[] listImage;
        final ArrayList<String> f = new ArrayList<String>();
        File file = new File(android.os.Environment.getExternalStorageDirectory(), "/BVscreenshots");
        if (file.isDirectory()) {
            listImage = file.listFiles();
            for (File aListImage : listImage) {
                f.add(0, aListImage.getAbsolutePath());
                Log.e("image: ", aListImage.getName());
            }
        }
//        uri = listImage[i].getAbsolutePath();
        GridView gridView = (GridView) my_view.findViewById(R.id.grid_image);
        noImageView = my_view.findViewById(R.id.layout_no_image);
        ImageAdapter da = new ImageAdapter(mContext, f);

        if (f != null && f.size() > 0) {
            gridView.setAdapter(da);
        } else {
            noImageView.setVisibility(View.VISIBLE);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri hacked_uri = Uri.parse("file://" + f.get(arg2));
                intent.setDataAndType(hacked_uri, "image/*");
                mContext.startActivity(intent);
                Log.e("IMG error", "file://" + f.get(arg2));
            }
        });

    }


    public void createData() {

        VideoList video1;
        File[] list_Video;
        File file = new File(android.os.Environment.getExternalStorageDirectory(), "/" + mContext.getResources().getString(R.string.string_store_video_folder) + "/");
        if (file.isDirectory()) {
            list_Video = file.listFiles();

            for (File aList_Video : list_Video) {
                video1 = new VideoList();
                Uri videoUri = Uri.parse(aList_Video.getAbsolutePath());
                video1.setVideo_uri(videoUri);

//                Bitmap bm = ThumbnailUtils.createVideoThumbnail(video_thumbnail.toString(), MediaStore.Video.Thumbnails.MINI_KIND);
//                video1.setVideo_image(bm);
                video1.setVideo_name(aList_Video.getName());
                video1.setVideo_size((double) Math.round(aList_Video.length() * 100 / 1024 / 1024) / 100 + "MB");
                video1.setMore(R.drawable.ic_more);
//                MediaPlayer mp = MediaPlayer.create(mContext, video_thumbnail);
//                Log.e("video uri", video_thumbnail.toString());
//                try {
//                    int duration = mp.getDuration();
//                    mp.release();
//                    String s = String.format("%d min, %d sec",
//                            TimeUnit.MILLISECONDS.toMinutes(duration),
//                            TimeUnit.MILLISECONDS.toSeconds(duration) -
//                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
//                    );
//                    video1.setVideo_length(s);
//                } catch (Exception e) {
//                    Log.e("error3", e.toString());
//                }
                listVideo.add(0, video1);

            }

            /*Collections.sort(listVideo, new Comparator<VideoList>() {
                public int compare(VideoList obj1, VideoList obj2) {
                    // TODO Auto-generated method stub
                    return obj1.getVideo_name().compareToIgnoreCase(obj2.getVideo_name());
                }
            });*/

            new LoadDuration().execute();


        }
    }

    class LoadDuration extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < listVideo.size(); i++) {
                Bitmap bm = ThumbnailUtils.createVideoThumbnail(listVideo.get(i).getVideo_uri().toString(), MediaStore.Video.Thumbnails.MINI_KIND);
                listVideo.get(i).setVideo_image(bm);
                MediaPlayer mp = MediaPlayer.create(mContext, listVideo.get(i).getVideo_uri());
                Log.e("video uri", listVideo.get(i).getVideo_uri().toString());
                try {
                    int duration = mp.getDuration();
//                    mp.release();
                    String s = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                    );
                    listVideo.get(i).setVideo_length(s);
                    publishProgress();
                } catch (Exception e) {
                    Log.e("error3", e.toString());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            adapter.notifyDataSetChanged();
            super.onProgressUpdate();
        }
    }
}

