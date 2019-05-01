package org.geometerplus.android.fbreader;

import android.content.Intent;
import android.os.Parcelable;
import android.view.View;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.bookmark.EditBookmarkActivity;
import org.geometerplus.android.util.OrientationUtil;

public class SelectionBookmarkAction extends FBAndroidAction {
	SelectionBookmarkAction(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final Bookmark bookmark;
		if (params.length != 0) {
			bookmark = (Bookmark)params[0];
		} else {
			bookmark = Reader.addSelectionBookmark();
		}
		if (bookmark == null) return;

		final SuperActivityToast toast =
			new SuperActivityToast(BaseActivity, SuperToast.Type.BUTTON);
		toast.setText(bookmark.getText());
		toast.setDuration(SuperToast.Duration.EXTRA_LONG);
		toast.setButtonIcon(
			android.R.drawable.ic_menu_edit,
			ZLResource.resource("dialog").getResource("button").getResource("edit").getValue()
		);
		toast.setOnClickWrapper(new OnClickWrapper("bkmk", new SuperToast.OnClickListener() {
			@Override
			public void onClick(View view, Parcelable token) {
				 Intent intent = new Intent(BaseActivity.getApplicationContext(), EditBookmarkActivity.class);
				FBReaderIntents.putBookmarkExtra(intent, bookmark);
				OrientationUtil.startActivity(BaseActivity, intent);
			}
		}));
		BaseActivity.showToast(toast);
	}
}
