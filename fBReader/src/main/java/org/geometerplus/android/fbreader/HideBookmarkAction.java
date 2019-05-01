package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

class HideBookmarkAction extends FBAndroidAction {
    HideBookmarkAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        BaseActivity.hideMarker();
    }

}
