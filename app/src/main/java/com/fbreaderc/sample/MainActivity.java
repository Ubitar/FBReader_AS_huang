package com.fbreaderc.sample;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.fbreader.FBReaderHelper;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.book.Book;

public class MainActivity extends AppCompatActivity {

    private FBReaderHelper fbReaderHelper;

    private Button btn;

//    private String path = Environment.getExternalStorageDirectory() + "/test.txt";
//    private String path2 = Environment.getExternalStorageDirectory() + "/test.mobi";
    private String path3 = Environment.getExternalStorageDirectory() + "/test.epub";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fbReaderHelper = new FBReaderHelper(this);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //必须确保activity有绑定服务才能通过jni获取书本信息
                fbReaderHelper.bindToService(new Runnable() {
                    @Override
                    public void run() {
                        Book book = fbReaderHelper.getCollection().getBookByFile(path3);
                        FBReader.openBook(MainActivity.this, book, null);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //对fbreader阅读服务进行绑定
        fbReaderHelper.bindToService(null);
    }

    @Override
    protected void onPause() {
        //注销fbreader阅读服务绑定，service只允许绑定一个activity，所以为保证下一个activity能使用阅读服务，必须注销
        fbReaderHelper.unBind();
        super.onPause();
    }
}
