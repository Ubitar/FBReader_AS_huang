package com.fbreader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * Created by laohuang on 2019/1/25.
 */
public abstract class FragmentUtil {
    public static void replace(FragmentManager manager, Fragment fragment, int containerId, String tag) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(containerId, fragment, tag);
        transaction.commit();
    }

    public static void hide(FragmentManager manager, String tag) {
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment != null) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.hide(fragment);
            transaction.commit();
        }
    }

    public static void show(FragmentManager manager, String tag) {
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment != null) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.show(fragment);
            transaction.commit();
        }
    }
}
