package com.timecat.module.timetable.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timecat.module.timetable.R;

/**
 * @author Administrator 刘壮飞
 */
@SuppressLint({"NewApi", "ValidFragment"})
public class ThemeMarketFragment extends LazyLoadFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.timetable_fragment_theme_market, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
    }
}
