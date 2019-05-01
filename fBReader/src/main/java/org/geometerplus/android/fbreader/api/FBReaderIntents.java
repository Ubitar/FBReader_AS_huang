package org.geometerplus.android.fbreader.api;

import android.content.Intent;

import org.geometerplus.fbreader.book.*;

public abstract class FBReaderIntents {
	public static  String DEFAULT_PACKAGE;

	public interface Action {
		String API                              = "android.fbreader.action.API";
		String API_CALLBACK                     = "android.fbreader.action.API_CALLBACK";
		String VIEW                             = "android.fbreader.action.VIEW";
		String CONFIG_SERVICE                   = "android.fbreader.action.CONFIG_SERVICE";
		String LIBRARY_SERVICE                  = "android.fbreader.action.LIBRARY_SERVICE";
		String LIBRARY                          = "android.fbreader.action.LIBRARY";
		String EXTERNAL_LIBRARY                 = "android.fbreader.action.EXTERNAL_LIBRARY";
		String OPEN_NETWORK_CATALOG             = "android.fbreader.action.OPEN_NETWORK_CATALOG";
		String ERROR                            = "android.fbreader.action.ERROR";
		String CRASH                            = "android.fbreader.action.CRASH";
		String PLUGIN                           = "android.fbreader.action.PLUGIN";
		String CLOSE                            = "android.fbreader.action.CLOSE";
		String PLUGIN_CRASH                     = "android.fbreader.action.PLUGIN_CRASH";

		String SYNC_START                       = "android.fbreader.action.sync.START";
		String SYNC_STOP                        = "android.fbreader.action.sync.STOP";
		String SYNC_SYNC                        = "android.fbreader.action.sync.SYNC";
		String SYNC_QUICK_SYNC                  = "android.fbreader.action.sync.QUICK_SYNC";

		String PLUGIN_VIEW                      = "android.fbreader.action.plugin.VIEW";
		String PLUGIN_KILL                      = "android.fbreader.action.plugin.KILL";
		String PLUGIN_CONNECT_COVER_SERVICE     = "android.fbreader.action.plugin.CONNECT_COVER_SERVICE";
	}

	public interface Event {
		String CONFIG_OPTION_CHANGE             = "fbreader.config_service.option_change_event";

		String LIBRARY_BOOK                     = "fbreader.library_service.book_event";
		String LIBRARY_BUILD                    = "fbreader.library_service.build_event";

		String SYNC_UPDATED                     = "android.fbreader.event.sync.UPDATED";
	}

	public interface Key {
		String BOOK                             = "fbreader.book";
		String BOOKMARK                         = "fbreader.bookmark";
		String PLUGIN                           = "fbreader.plugin";
		String TYPE                             = "fbreader.type";
	}

	public static Intent defaultInternalIntent(String action) {
		return internalIntent(action).addCategory(Intent.CATEGORY_DEFAULT);
	}

	public static Intent internalIntent(String action) {
		return new Intent(action).setPackage(DEFAULT_PACKAGE);
	}

	public static void putBookExtra(Intent intent, String key, Book book) {
		intent.putExtra(key, SerializerUtil.serialize(book));
	}

	public static void putBookExtra(Intent intent, Book book) {
		putBookExtra(intent, Key.BOOK, book);
	}

	public static <B extends AbstractBook> B getBookExtra(Intent intent, String key, AbstractSerializer.BookCreator<B> creator) {
		return SerializerUtil.deserializeBook(intent.getStringExtra(key), creator);
	}

	public static <B extends AbstractBook> B getBookExtra(Intent intent, AbstractSerializer.BookCreator<B> creator) {
		return getBookExtra(intent, Key.BOOK, creator);
	}

	public static void putBookmarkExtra(Intent intent, String key, Bookmark bookmark) {
		intent.putExtra(key, SerializerUtil.serialize(bookmark));
	}

	public static void putBookmarkExtra(Intent intent, Bookmark bookmark) {
		putBookmarkExtra(intent, Key.BOOKMARK, bookmark);
	}

	public static Bookmark getBookmarkExtra(Intent intent, String key) {
		return SerializerUtil.deserializeBookmark(intent.getStringExtra(key));
	}

	public static Bookmark getBookmarkExtra(Intent intent) {
		return getBookmarkExtra(intent, Key.BOOKMARK);
	}
}
