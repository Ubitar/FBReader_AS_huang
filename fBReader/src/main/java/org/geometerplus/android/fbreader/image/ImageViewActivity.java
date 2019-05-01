package org.geometerplus.android.fbreader.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.image.ZLImageManager;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

/**
 * Created by laohuang on 2019/1/22.
 */
public class ImageViewActivity extends Activity implements View.OnClickListener{

    public static final String URL_KEY = "fbreader.imageview.url";
    public static final String BACKGROUND_COLOR_KEY = "fbreader.imageview.background";

    private Bitmap myBitmap;
    private ZLColor myBgColor;

    private RelativeLayout layoutMain;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_image_preview);
        final ZLAndroidLibrary library = (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
        final boolean showStatusBar = library.ShowStatusBarOption.getValue();
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                showStatusBar ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        Thread.setDefaultUncaughtExceptionHandler(
                new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this)
        );


        final Intent intent = getIntent();

        myBgColor = new ZLColor(
                intent.getIntExtra(BACKGROUND_COLOR_KEY, new ZLColor(127, 127, 127).intValue())
        );

        final String url = intent.getStringExtra(URL_KEY);
        final String prefix = ZLFileImage.SCHEME + "://";
        if (url != null && url.startsWith(prefix)) {
            final ZLFileImage image = ZLFileImage.byUrlPath(url.substring(prefix.length()));
            if (image == null) finish();
            try {
                final ZLImageData imageData = ZLImageManager.Instance().getImageData(image);
                myBitmap = ((ZLAndroidImageData) imageData).getFullSizeBitmap();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        } else {
            finish();
        }

        layoutMain=findViewById(R.id.layoutMain);
        layoutMain.setBackgroundColor(ZLAndroidColorUtil.rgb(myBgColor));
        imageView=findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        imageView.setImageBitmap(myBitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        OrientationUtil.setOrientation(this, getIntent());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_anim_no, R.anim.activity_anim_no);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        OrientationUtil.setOrientation(this, intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myBitmap != null) {
            myBitmap.recycle();
        }
        myBitmap = null;
    }

    @Override
    public void onClick(View view) {
        onBackPressed();
    }
}
