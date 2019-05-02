package org.geometerplus.android.fbreader;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.fbreader.util.FragmentUtils;
import com.fbreader.common.IntentKey;
import com.fbreader.view.fragment.MarkerFragment;
import com.fbreader.view.fragment.MenuFragment;
import com.fbreader.view.fragment.TOCFragment;

import org.geometerplus.android.fbreader.api.ApiListener;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.dict.DictionaryUtil;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.fbreader.httpd.DataService;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.sync.SyncOperations;
import org.geometerplus.android.fbreader.tips.TipsActivity;
import org.geometerplus.android.util.DeviceType;
import org.geometerplus.android.util.SearchDialogUtil;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.DictionaryHighlighting;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.CancelMenuHelper;
import org.geometerplus.fbreader.formats.ExternalFormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.tips.TipsManager;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.error.ErrorKeys;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FBReader extends FBReaderMainActivity implements ZLApplicationWindow {
    public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
    public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;

    public static Intent defaultIntent(Context context) {
        return new Intent(context, FBReader.class)
                .setAction(FBReaderIntents.Action.VIEW)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    public static void openBook(Context context, Book book, Bookmark bookmark) {
        final Intent intent = defaultIntent(context);
        FBReaderIntents.putBookExtra(intent, book);
        FBReaderIntents.putBookmarkExtra(intent, bookmark);
        context.startActivity(intent);
    }

    private FBReaderApp myFBReaderApp;
    private volatile Book myBook;

    private MenuFragment menuFramgent;
    private TOCFragment tocFragment;
    private MarkerFragment markerFragment;

    private RelativeLayout myRootView;
    private ZLAndroidWidget myMainView;

    private volatile boolean myShowStatusBarFlag;

    final DataService.Connection DataConnection = new DataService.Connection();

    volatile boolean IsPaused = false;
    private volatile long myResumeTimestamp;
    volatile Runnable OnResumeAction = null;

    private Intent myCancelIntent = null;
    private Intent myOpenBookIntent = null;

    private final FBReaderApp.Notifier myNotifier = new AppNotifier(this);

    private static final String PLUGIN_ACTION_PREFIX = "___";
    private final List<PluginApi.ActionInfo> myPluginActions = new LinkedList<PluginApi.ActionInfo>();
    private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).<PluginApi.ActionInfo>getParcelableArrayList(PluginApi.PluginInfo.KEY);
            if (actions != null) {
                synchronized (myPluginActions) {
                    int index = 0;
                    while (index < myPluginActions.size()) {
                        myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
                    }
                    myPluginActions.addAll(actions);
                    index = 0;
                    for (PluginApi.ActionInfo info : myPluginActions) {
                        myFBReaderApp.addAction(
                                PLUGIN_ACTION_PREFIX + index++,
                                new RunPluginAction(FBReader.this, myFBReaderApp, info.getId())
                        );
                    }
                }
            }
        }
    };

    private synchronized void openBook(Intent intent, final Runnable action, boolean force) {
        if (!force && myBook != null) return;

        myBook = FBReaderIntents.getBookExtra(intent, myFBReaderApp.Collection);
        final Bookmark bookmark = FBReaderIntents.getBookmarkExtra(intent);
        if (myBook == null) {
            final Uri data = intent.getData();
            if (data != null) {
                myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
            }
        }
        if (myBook != null) {
            ZLFile file = BookUtil.fileByBook(myBook);
            if (!file.exists()) {
                if (file.getPhysicalFile() != null) {
                    file = file.getPhysicalFile();
                }
                UIMessageUtil.showErrorMessage(this, "fileNotFound", file.getPath());
                myBook = null;
            } else {
                NotificationUtil.drop(this, myBook);
            }
        }
        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                myFBReaderApp.openBook(myBook, bookmark, action, myNotifier);
                AndroidFontUtil.clearFontCache();
            }
        });
    }

    private Book createBookForFile(ZLFile file) {
        if (file == null) return null;

        Book book = myFBReaderApp.Collection.getBookByFile(file.getPath());
        if (book != null) return book;

        if (file.isArchive()) {
            for (ZLFile child : file.children()) {
                book = myFBReaderApp.Collection.getBookByFile(child.getPath());
                if (book != null) return book;
            }
        }
        return null;
    }

    private Runnable getPostponedInitAction() {
        return new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        new TipRunner().start();
                        DictionaryUtil.init(FBReader.this, null);
                        final Intent intent = getIntent();
                        if (intent != null && FBReaderIntents.Action.PLUGIN.equals(intent.getAction()))
                            new RunPluginAction(FBReader.this, myFBReaderApp, intent.getData()).run();
                    }
                });
            }
        };
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        bindService(new Intent(this, DataService.class), DataConnection, DataService.BIND_AUTO_CREATE);

        final Config config = Config.Instance();
        config.runOnConnect(new Runnable() {
            public void run() {
                config.requestAllValuesForGroup("Options");
                config.requestAllValuesForGroup("Style");
                config.requestAllValuesForGroup("LookNFeel");
                config.requestAllValuesForGroup("Fonts");
                config.requestAllValuesForGroup("Colors");
                config.requestAllValuesForGroup("Files");
            }
        });

        final ZLAndroidLibrary zlibrary = getZLibrary();
        myShowStatusBarFlag = zlibrary.ShowStatusBarOption.getValue();

        setContentView(R.layout.activity_fbreader);

        myRootView = (RelativeLayout) findViewById(R.id.layoutRootView);
        myMainView = (ZLAndroidWidget) findViewById(R.id.readerView);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
        if (myFBReaderApp == null) {
            myFBReaderApp = new FBReaderApp(Paths.systemInfo(this), new BookCollectionShadow());
        }
        myBook = null;

        myFBReaderApp.setWindow(this);
        myFBReaderApp.initWindow();

        myFBReaderApp.setExternalFileOpener(new ExternalFileOpener(this));

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                myShowStatusBarFlag ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
            new TextSearchPopup(myFBReaderApp);
        }
        if (myFBReaderApp.getPopupById(NavigationPopup.ID) == null) {
            new NavigationPopup(myFBReaderApp);
        }
        if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
            new SelectionPopup(myFBReaderApp);
        }

        myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARK, new ShowBookmarkAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.HIDE_BOOKMARK, new HideBookmarkAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.HIDE_MENU, new HideMenuAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.HIDE_TOC, new HideTOCAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.DISPLAY_BOOK_POPUP, new DisplayBookPopupAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.OPEN_VIDEO, new OpenVideoAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.HIDE_TOAST, new HideToastAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.INSTALL_PLUGINS, new InstallPluginsAction(this, myFBReaderApp));

        final Intent intent = getIntent();
        final String action = intent.getAction();

        myOpenBookIntent = intent;
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (FBReaderIntents.Action.CLOSE.equals(action)) {
                myCancelIntent = intent;
                myOpenBookIntent = null;
            } else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(action)) {
                myFBReaderApp.ExternalBook = null;
                myOpenBookIntent = null;
                getCollection().bindToService(this, new Runnable() {
                    public void run() {
                        myFBReaderApp.openBook(null, null, null, myNotifier);
                    }
                });
            }
        }

        tocFragment = TOCFragment.newInstance();
        FragmentUtils.replace(getSupportFragmentManager(), tocFragment, R.id.layoutTOC);
        FragmentUtils.hide(tocFragment);
        menuFramgent = MenuFragment.newInstance();
        FragmentUtils.replace(getSupportFragmentManager(), menuFramgent, R.id.layoutMenu);
        FragmentUtils.hide(menuFramgent);
        markerFragment = MarkerFragment.newInstance(null);
        FragmentUtils.replace(getSupportFragmentManager(), markerFragment, R.id.layoutMarker);
        FragmentUtils.hide(markerFragment);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final String action = intent.getAction();
        final Uri data = intent.getData();

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            super.onNewIntent(intent);
        } else if (Intent.ACTION_VIEW.equals(action) && data != null && "fbreader-action".equals(data.getScheme())) {
            myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
        } else if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
            myOpenBookIntent = intent;
            if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
                final BookCollectionShadow collection = getCollection();
                final Book b = FBReaderIntents.getBookExtra(intent, collection);
                if (!collection.sameBook(b, myFBReaderApp.ExternalBook)) {
                    try {
                        final ExternalFormatPlugin plugin =
                                (ExternalFormatPlugin) BookUtil.getPlugin(
                                        PluginCollection.Instance(Paths.systemInfo(this)),
                                        myFBReaderApp.ExternalBook
                                );
                        startActivity(PluginUtil.createIntent(plugin, FBReaderIntents.Action.PLUGIN_KILL));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (FBReaderIntents.Action.PLUGIN.equals(action)) {
            new RunPluginAction(this, myFBReaderApp, data).run();
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            final String pattern = intent.getStringExtra(SearchManager.QUERY);
            final Runnable runnable = new Runnable() {
                public void run() {
                    final TextSearchPopup popup = (TextSearchPopup) myFBReaderApp.getPopupById(TextSearchPopup.ID);
                    popup.initPosition();
                    myFBReaderApp.MiscOptions.TextSearchPattern.setValue(pattern);
                    if (myFBReaderApp.getTextView().search(pattern, true, false, false, false) != 0) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                myFBReaderApp.showPopup(popup.getId());
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                UIMessageUtil.showErrorMessage(FBReader.this, "textNotFound");
                                popup.StartPosition = null;
                            }
                        });
                    }
                }
            };
            UIUtil.wait("search", runnable, this);
        } else if (FBReaderIntents.Action.CLOSE.equals(intent.getAction())) {
            myCancelIntent = intent;
            myOpenBookIntent = null;
        } else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(intent.getAction())) {
            final Book book = FBReaderIntents.getBookExtra(intent, myFBReaderApp.Collection);
            myFBReaderApp.ExternalBook = null;
            myOpenBookIntent = null;
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    final BookCollectionShadow collection = getCollection();
                    Book b = collection.getRecentBook(0);
                    if (collection.sameBook(b, book)) {
                        b = collection.getRecentBook(1);
                    }
                    myFBReaderApp.openBook(b, null, null, myNotifier);
                }
            });
        } else {
            super.onNewIntent(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCollection().bindToService(this, new Runnable() {
            public void run() {
                new Thread() {
                    public void run() {
                        getPostponedInitAction().run();
                    }
                }.start();
                myFBReaderApp.getViewWidget().repaint();
            }
        });

        initPluginActions();

        final ZLAndroidLibrary zlibrary = getZLibrary();

        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                final boolean showStatusBar = zlibrary.ShowStatusBarOption.getValue();
                if (showStatusBar != myShowStatusBarFlag) {
                    finish();
                    startActivity(new Intent(FBReader.this, FBReader.class));
                }
                zlibrary.ShowStatusBarOption.saveSpecialValue();
                myFBReaderApp.ViewOptions.ColorProfileName.saveSpecialValue();
            }
        });

        ((PopupPanel) myFBReaderApp.getPopupById(TextSearchPopup.ID)).setPanelInfo(this, myRootView);
        ((NavigationPopup) myFBReaderApp.getPopupById(NavigationPopup.ID)).setPanelInfo(this, myRootView);
        ((PopupPanel) myFBReaderApp.getPopupById(SelectionPopup.ID)).setPanelInfo(this, myRootView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        switchWakeLock(hasFocus &&
                getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() <
                        myFBReaderApp.getBatteryLevel()
        );
    }

    private void initPluginActions() {
        synchronized (myPluginActions) {
            int index = 0;
            while (index < myPluginActions.size()) {
                myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
            }
            myPluginActions.clear();
        }

        sendOrderedBroadcast(
                new Intent(PluginApi.ACTION_REGISTER),
                null,
                myPluginInfoReceiver,
                null,
                RESULT_OK,
                null,
                null
        );
    }

    private class TipRunner extends Thread {
        TipRunner() {
            setPriority(MIN_PRIORITY);
        }

        public void run() {
            final TipsManager manager = new TipsManager(Paths.systemInfo(FBReader.this));
            switch (manager.requiredAction()) {
                case Initialize:
                    startActivity(new Intent(
                            TipsActivity.INITIALIZE_ACTION, null, FBReader.this, TipsActivity.class
                    ));
                    break;
                case Show:
                    startActivity(new Intent(
                            TipsActivity.SHOW_TIP_ACTION, null, FBReader.this, TipsActivity.class
                    ));
                    break;
                case Download:
                    manager.startDownloading();
                    break;
                case None:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        myStartTimer = true;
        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                SyncOperations.enableSync(FBReader.this, myFBReaderApp.SyncOptions);

                final int brightnessLevel =
                        getZLibrary().ScreenBrightnessLevelOption.getValue();
                if (brightnessLevel != 0) {
                    getViewWidget().setScreenBrightness(brightnessLevel);
                } else {
                    setScreenBrightnessAuto();
                }
                if (getZLibrary().DisableButtonLightsOption.getValue()) {
                    setButtonLight(false);
                }

                getCollection().bindToService(FBReader.this, new Runnable() {
                    public void run() {
                        final BookModel model = myFBReaderApp.Model;
                        if (model == null || model.Book == null) return;
                        onPreferencesUpdate(myFBReaderApp.Collection.getBookById(model.Book.getId()));
                    }
                });
            }
        });

        registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        IsPaused = false;
        myResumeTimestamp = System.currentTimeMillis();
        if (OnResumeAction != null) {
            final Runnable action = OnResumeAction;
            OnResumeAction = null;
            action.run();
        }

        registerReceiver(mySyncUpdateReceiver, new IntentFilter(FBReaderIntents.Event.SYNC_UPDATED));

        if (myCancelIntent != null) {
            final Intent intent = myCancelIntent;
            myCancelIntent = null;
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    runCancelAction(intent);
                }
            });
            return;
        } else if (myOpenBookIntent != null) {
            final Intent intent = myOpenBookIntent;
            myOpenBookIntent = null;
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    openBook(intent, null, true);
                }
            });
        } else if (myFBReaderApp.getCurrentServerBook(null) != null) {
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    myFBReaderApp.useSyncInfo(true, myNotifier);
                }
            });
        } else if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    myFBReaderApp.openBook(myFBReaderApp.ExternalBook, null, null, myNotifier);
                }
            });
        } else {
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    myFBReaderApp.useSyncInfo(true, myNotifier);
                }
            });
        }

        PopupPanel.restoreVisibilities(myFBReaderApp);
        ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
    }

    @Override
    protected void onPause() {
        SyncOperations.quickSync(this, myFBReaderApp.SyncOptions);

        IsPaused = true;
        try {
            unregisterReceiver(mySyncUpdateReceiver);
        } catch (IllegalArgumentException e) {
        }

        try {
            unregisterReceiver(myBatteryInfoReceiver);
        } catch (IllegalArgumentException e) {
        }

        myFBReaderApp.stopTimer();
        if (getZLibrary().DisableButtonLightsOption.getValue())
            setButtonLight(true);
        myFBReaderApp.onWindowClosing();

        super.onPause();
    }

    @Override
    protected void onStop() {
        ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
        PopupPanel.removeAllWindows(myFBReaderApp, this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getCollection().unbind();
        unbindService(DataConnection);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        myFBReaderApp.onWindowClosing();
        super.onLowMemory();
    }

    @Override
    public boolean onSearchRequested() {
        final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
        myFBReaderApp.hideActivePopup();
        if (DeviceType.Instance().hasStandardSearchDialog()) {
            final SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
            manager.setOnCancelListener(new SearchManager.OnCancelListener() {
                public void onCancel() {
                    if (popup != null) {
                        myFBReaderApp.showPopup(popup.getId());
                    }
                    manager.setOnCancelListener(null);
                }
            });
            startSearch(myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), true, null, false);
        } else {
            SearchDialogUtil.showDialog(
                    this, FBReader.class, myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface di) {
                            if (popup != null) {
                                myFBReaderApp.showPopup(popup.getId());
                            }
                        }
                    }
            );
        }
        return true;
    }

    public void showSelectionPanel() {
        final ZLTextView view = myFBReaderApp.getTextView();
        ((SelectionPopup) myFBReaderApp.getPopupById(SelectionPopup.ID))
                .move(view.getSelectionStartY(), view.getSelectionEndY());
        myFBReaderApp.showPopup(SelectionPopup.ID);
    }

    public void hideSelectionPanel() {
        final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
        if (popup != null && popup.getId() == SelectionPopup.ID) {
            myFBReaderApp.hideActivePopup();
        }
    }

    private void onPreferencesUpdate(Book book) {
        AndroidFontUtil.clearFontCache();
        myFBReaderApp.onBookUpdated(book);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case REQUEST_PREFERENCES:
                if (resultCode != RESULT_DO_NOTHING && data != null) {
                    final Book book = FBReaderIntents.getBookExtra(data, myFBReaderApp.Collection);
                    if (book != null) {
                        getCollection().bindToService(this, new Runnable() {
                            public void run() {
                                onPreferencesUpdate(book);
                            }
                        });
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (menuFramgent.isVisible()) FragmentUtils.hide(menuFramgent);
        else if (tocFragment.isVisible()) FragmentUtils.hide(tocFragment);
        else if (markerFragment.isVisible()) FragmentUtils.hide(markerFragment);
        else myFBReaderApp.closeWindow();
    }

    private void runCancelAction(Intent intent) {
        try {
            final CancelMenuHelper.ActionType type = CancelMenuHelper.ActionType.valueOf(intent.getStringExtra(FBReaderIntents.Key.TYPE));
            Bookmark bookmark = null;
            if (type == CancelMenuHelper.ActionType.returnTo) {
                bookmark = FBReaderIntents.getBookmarkExtra(intent);
                if (bookmark == null) return;
            }
            myFBReaderApp.runCancelAction(type, bookmark);
        } catch (Exception e) {
        }
    }

    public void navigate() {
        ((NavigationPopup) myFBReaderApp.getPopupById(NavigationPopup.ID)).runNavigation();
    }

    protected void onPluginNotFound(final Book book) {
        final BookCollectionShadow collection = getCollection();
        collection.bindToService(this, new Runnable() {
            public void run() {
                final Book recent = collection.getRecentBook(0);
                if (recent != null && !collection.sameBook(recent, book)) {
                    myFBReaderApp.openBook(recent, null, null, null);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        showMenu();
        return true;
    }

    private void setButtonLight(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            setButtonLightInternal(enabled);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private void setButtonLightInternal(boolean enabled) {
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.buttonBrightness = enabled ? -1.0f : 0.0f;
        getWindow().setAttributes(attrs);
    }

    private PowerManager.WakeLock myWakeLock;
    private boolean myWakeLockToCreate;
    private boolean myStartTimer;

    @SuppressLint("InvalidWakeLockTag")
    public final void createWakeLock() {
        if (myWakeLockToCreate) {
            synchronized (this) {
                if (myWakeLockToCreate) {
                    myWakeLockToCreate = false;
                    myWakeLock =
                            ((PowerManager) getSystemService(POWER_SERVICE))
                                    .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
                    myWakeLock.acquire();
                }
            }
        }
        if (myStartTimer) {
            myFBReaderApp.startTimer();
            myStartTimer = false;
        }
    }

    private final void switchWakeLock(boolean on) {
        if (on) {
            if (myWakeLock == null) {
                myWakeLockToCreate = true;
            }
        } else {
            if (myWakeLock != null) {
                synchronized (this) {
                    if (myWakeLock != null) {
                        myWakeLock.release();
                        myWakeLock = null;
                    }
                }
            }
        }
    }

    private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final int level = intent.getIntExtra("level", 100);
            setBatteryLevel(level);
            switchWakeLock(
                    hasWindowFocus() &&
                            getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < level
            );
        }
    };

    private BookCollectionShadow getCollection() {
        return (BookCollectionShadow) myFBReaderApp.Collection;
    }

    @Override
    public void showErrorMessage(String key) {
        UIMessageUtil.showErrorMessage(this, key);
    }

    @Override
    public void showErrorMessage(String key, String parameter) {
        UIMessageUtil.showErrorMessage(this, key, parameter);
    }

    @Override
    public FBReaderApp.SynchronousExecutor createExecutor(String key) {
        return UIUtil.createExecutor(this, key);
    }

    private int myBatteryLevel;

    @Override
    public int getBatteryLevel() {
        return myBatteryLevel;
    }

    private void setBatteryLevel(int percent) {
        myBatteryLevel = percent;
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public ZLViewWidget getViewWidget() {
        return myMainView;
    }

    @Override
    public void refresh() {
    }

    @Override
    public void processException(Exception exception) {
        exception.printStackTrace();

        final Intent intent = new Intent(
                FBReaderIntents.Action.ERROR,
                new Uri.Builder().scheme(exception.getClass().getSimpleName()).build()
        );
        intent.setPackage(FBReaderIntents.DEFAULT_PACKAGE);
        intent.putExtra(ErrorKeys.MESSAGE, exception.getMessage());
        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        intent.putExtra(ErrorKeys.STACKTRACE, stackTrace.toString());
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setWindowTitle(final String title) {
        runOnUiThread(new Runnable() {
            public void run() {
                setTitle(title);
            }
        });
    }

    private BroadcastReceiver mySyncUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            myFBReaderApp.useSyncInfo(myResumeTimestamp + 10 * 1000 > System.currentTimeMillis(), myNotifier);
        }
    };

    private void fullscreen(boolean enable) {
        if (getWindow() == null) return;
        if (enable) { // 隐藏状态栏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else { // 显示状态栏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(lp);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public void outlineRegion(ZLTextRegion.Soul soul) {
        myFBReaderApp.getTextView().outlineRegion(soul);
        myFBReaderApp.getViewWidget().repaint();
    }

    public void hideDictionarySelection() {
        myFBReaderApp.getTextView().hideOutline();
        myFBReaderApp.getTextView().removeHighlightings(DictionaryHighlighting.class);
        myFBReaderApp.getViewWidget().reset();
        myFBReaderApp.getViewWidget().repaint();
    }

    public void showTOC() {
        FragmentUtils.show(tocFragment);
    }

    public void hideTOC() {
        FragmentUtils.hide(tocFragment);
    }

    public void showMenu() {
        FragmentUtils.show(menuFramgent);
        fullscreen(false);
    }

    public void hideMenu() {
        FragmentUtils.hide(menuFramgent);
        if (!myShowStatusBarFlag) fullscreen(true);
    }

    public void showMarker(Bookmark bookmark) {
        fullscreen(false);
        Bundle bundle = markerFragment.getArguments();
        bundle.putParcelable(IntentKey.BOOK_MARKER, bookmark);
        markerFragment.setArguments(bundle);
        FragmentUtils.show(markerFragment);
    }

    public void hideMarker() {
        FragmentUtils.hide(markerFragment);
        fullscreen(true);
    }

}
