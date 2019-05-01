package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;

/**
 * Created by laohuang on 2019/1/14.
 */
public class PageTurnStartAction extends FBAction {
    public PageTurnStartAction(FBReaderApp fbreader) {
        super(fbreader);
    }

    @Override
    public boolean isEnabled() {
        return ZLApplication.Instance().isActionEnabled(ActionCode.SELECTION_SHOW_PANEL);
    }

    @Override
    protected void run(Object... params) {
        ZLApplication.Instance().runAction(ActionCode.SELECTION_CLEAR);
        ZLApplication.Instance().runAction(ActionCode.SELECTION_HIDE_PANEL);
    }
}
