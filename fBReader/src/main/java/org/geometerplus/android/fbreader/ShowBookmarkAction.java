package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class ShowBookmarkAction extends FBAndroidAction {
    ShowBookmarkAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        BaseActivity.showMarker((Bookmark) params[0]);
    }

}
