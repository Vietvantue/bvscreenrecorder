package com.example.bvlab.screenrecord;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

public class GalleryActivity extends FragmentActivity implements ActionBar.TabListener {
    ViewPager viewPager;
    private ActionBar myActionBar;
    ViewPagerAdapter viewPagerAdapter;

    private com.google.android.gms.ads.AdView g_adView;
    private RelativeLayout layout_ads_main_screen;
    View btnBack, btnSetting;


    private void showGBannerAds(final ViewGroup myview_ads) {

        g_adView = new com.google.android.gms.ads.AdView(this);
        g_adView.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);
        g_adView.setBackgroundColor(Color.TRANSPARENT);
        g_adView.setAdUnitId(getString(R.string.admob_banner));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        g_adView.setLayoutParams(params);
        myview_ads.addView(g_adView);
        AdRequest.Builder builder = new AdRequest.Builder();
//        builder.addTestDevice("5209A0E30EFC293107FAB2BB7ECE2FCC");
        AdRequest adRequest = builder.build();

        //adRequest.te
        g_adView.loadAd(adRequest);
    }

    private void loadGInterstitialAd() {
        g_FullAdView = new com.google.android.gms.ads.InterstitialAd(GalleryActivity.this);
        g_FullAdView.setAdUnitId(getString(R.string.admob_full));

        g_FullAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {

                finish();

            }

        });
        requestNewInterstitial();
    }

    private com.google.android.gms.ads.InterstitialAd g_FullAdView;

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("5209A0E30EFC293107FAB2BB7ECE2FCC")
                .build();

        g_FullAdView.loadAd(adRequest);

    }

    @Override
    public void onBackPressed() {


        if (g_FullAdView != null && g_FullAdView.isLoaded()) {
            g_FullAdView.show();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_activity);

        //ads
        layout_ads_main_screen = (RelativeLayout) findViewById(R.id.layout_ads_main_screen_nl);
        showGBannerAds(layout_ads_main_screen);

        loadGInterstitialAd();

        try {
            FloatingView.removeView();
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e("error", e.toString());
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        myActionBar = getActionBar();
        if (myActionBar != null) {
            myActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//            myActionBar.setDisplayHomeAsUpEnabled(true);
//            myActionBar.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            myActionBar.addTab(myActionBar.newTab().setText(getString(R.string.string_video)).setTabListener(this));
            myActionBar.addTab(myActionBar.newTab().setText(getString(R.string.string_image)).setTabListener(this));
//            myActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2196f3")));
//            myActionBar.setDisplayShowTitleEnabled(true);
//            myActionBar.setTitle(getString(R.string.string_gallery));
//            myActionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
            myActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            myActionBar.setCustomView(R.layout.layout_toolbar_review);
            View customActionbarView = myActionBar.getCustomView();

            btnBack = customActionbarView.findViewById(R.id.btn_back_activity);
            btnSetting = customActionbarView.findViewById(R.id.btn_setting_activity);

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GalleryActivity.this.finish();
                }
            });

            btnSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                    i.putExtra("setting_start", true);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            });

        }

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), GalleryActivity.this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d("onPageScrolled", String.valueOf(position));
            }

            @Override
            public void onPageSelected(int position) {
                myActionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public Intent getParentActivityIntent() {
        finish();
        return super.getParentActivityIntent();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Intent i = new Intent(getApplicationContext(), SettingActivity.class);
//                i.putExtra("setting_start", true);
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onDestroy() {
        try {
            FloatingView.addView();
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e("error", e.toString());
        }
        super.onDestroy();
    }
}

