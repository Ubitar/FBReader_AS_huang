
package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public abstract class FBAndroidAction extends FBAction {
    protected final FBReader BaseActivity;

    public FBAndroidAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(fbreader);
        BaseActivity = baseActivity;
    }
}
