package com.fbreader.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import java.util.List;

/**
 * Created by chenweiqi on 2017/6/1.
 */

public class FragmentViewPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragments;
    List<String> titles;

    public FragmentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (titles != null) {
            if (titles.size() > position
                    && position >= 0) {
                return titles.get(position);
            }
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        if (fragments != null) {
            return fragments.size();
        }
        return 0;
    }

    @Override
    public void finishUpdate(View container) {
        super.finishUpdate(container);
    }
}
