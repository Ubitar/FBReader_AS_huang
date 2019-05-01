package com.fbreaderc.sample;

import android.app.Application;

import org.geometerplus.android.fbreader.FBReaderApplication;
import org.geometerplus.android.fbreader.api.FBReaderIntents;

public class App extends Application {

    static {
        //这里需要自己设置自己 build.gradle 里的  applicationId 到DEFAULT_PACKAGE字段
        FBReaderIntents.DEFAULT_PACKAGE = "com.fbreaderc.sample";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FBReaderApplication.init(this);
    }

}
