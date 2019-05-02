package com.fbreader.common;

import com.flyco.tablayout.listener.CustomTabEntity;

import java.util.ArrayList;

/**
 * Created by huangpinzhang on 2018/7/12.
 */
public class SimpleTabEntry implements CustomTabEntity {
    private String title;

    public SimpleTabEntry(String title) {
        this.title = title;
    }

    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public int getTabSelectedIcon() {
        return 0;
    }

    @Override
    public int getTabUnselectedIcon() {
        return 0;
    }

    public static ArrayList<CustomTabEntity> asList(String[] titles){
        ArrayList<CustomTabEntity> entityList = new ArrayList<>(titles.length);
        for (String title : titles) entityList.add(new SimpleTabEntry(title));
        return entityList;
    }
}
