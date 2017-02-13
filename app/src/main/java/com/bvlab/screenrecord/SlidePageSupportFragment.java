package com.bvlab.screenrecord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bvlab.screenrecord.R;

public class SlidePageSupportFragment extends android.support.v4.app.Fragment {
    int pageNumber = 0;

    public void setPageNumber(int position) {
        pageNumber = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup my_view = null;
        if (pageNumber == 0) {
            my_view = (ViewGroup) inflater.inflate(R.layout.layout_list_video, container, false);
//            onSaveInstanceState(savedInstanceState);
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getFragmentManager(), getActivity());
            viewPagerAdapter.bindViewPage1(my_view);
        } else if (pageNumber == 1) {
            my_view = (ViewGroup) inflater.inflate(R.layout.layout_grid_image, container, false);
//            onSaveInstanceState(savedInstanceState);
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getFragmentManager(), getActivity());
            viewPagerAdapter.bindViewPage2(my_view);
        }
        return my_view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}


