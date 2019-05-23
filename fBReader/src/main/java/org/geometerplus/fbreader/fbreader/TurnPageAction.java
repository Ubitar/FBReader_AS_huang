package org.geometerplus.fbreader.fbreader;

import org.geometerplus.android.fbreader.FBAndroidAction;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.fbreader.options.PageTurningOptions;
import org.geometerplus.zlibrary.core.application.ZLApplication;

public class TurnPageAction extends FBAndroidAction {
    private final boolean myForward;

    public TurnPageAction(FBReader baseActivity, FBReaderApp fbreader, boolean forward) {
        super(baseActivity, fbreader);
        myForward = forward;
    }

    @Override
    public boolean isEnabled() {
        final PageTurningOptions.FingerScrollingType fingerScrolling =
                Reader.PageTurningOptions.FingerScrolling.getValue();
        return
                fingerScrolling == PageTurningOptions.FingerScrollingType.byTap ||
                        fingerScrolling == PageTurningOptions.FingerScrollingType.byTapAndFlick;
    }

    @Override
    protected void run(Object... params) {
        final PageTurningOptions preferences = Reader.PageTurningOptions;
        if (BaseActivity.onTurnBackIntercept(myForward)) return;
        if (params.length == 2 && params[0] instanceof Integer && params[1] instanceof Integer) {
            final int x = (Integer) params[0];
            final int y = (Integer) params[1];
            Reader.getViewWidget().startAnimatedScrolling(
                    myForward ? FBView.PageIndex.next : FBView.PageIndex.previous,
                    x, y,
                    preferences.Horizontal.getValue()
                            ? FBView.Direction.rightToLeft : FBView.Direction.up,
                    preferences.AnimationSpeed.getValue()
            );
        } else {
            Reader.getViewWidget().startAnimatedScrolling(
                    myForward ? FBView.PageIndex.next : FBView.PageIndex.previous,
                    preferences.Horizontal.getValue()
                            ? FBView.Direction.rightToLeft : FBView.Direction.up,
                    preferences.AnimationSpeed.getValue()
            );
        }
        ZLApplication.Instance().runAction(ActionCode.PAGE_TURN_START);
    }
}
