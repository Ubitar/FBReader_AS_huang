package com.fbreader;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.CoverUtil;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.util.TextSnippet;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLViewEnums;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.text.view.style.ZLTextNGStyleDescription;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laohuang on 2019/1/3.
 */
public class FBReaderHelper {

    private Activity activity;

    private FBReaderApp myFBReaderApp;

    public FBReaderHelper(Activity activity) {
        this.activity = activity;
        myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
    }

    public BookCollectionShadow getCollection() {
        return (BookCollectionShadow) myFBReaderApp.Collection;
    }

    public void bindToService(Runnable runnable) {
        getCollection().bindToService(activity, runnable);
    }

    public void unBind() {
        getCollection().unbind();
    }

    public void addListener(IBookCollection.Listener listener) {
        myFBReaderApp.Collection.addListener(listener);
    }

    public FormatPlugin getFormatPlugin(Book book) {
        final PluginCollection pluginCollection = PluginCollection.Instance(myFBReaderApp.SystemInfo);
        try {
            return BookUtil.getPlugin(pluginCollection, book);
        } catch (BookReadingException e) {
            return null;
        }
    }

    /**
     * 异步加载封面图
     *
     * @param book
     * @param listener
     */
    public void loadBookCover(@NonNull Book book, @NonNull final OnGetCoverListener listener) {
        PluginCollection pluginCollection = PluginCollection.Instance(Paths.systemInfo(activity));
        BookUtil.getEncoding(book, pluginCollection);
        final ZLImage image = CoverUtil.getCover(book, pluginCollection);
        if (image == null) {
            listener.finish(false, null);
        } else {
            if (image instanceof ZLImageProxy) {
                ((ZLImageProxy) image).startSynchronization(new AndroidImageSynchronizer(activity), new Runnable() {
                    public void run() {
                        loadCover(image, listener);
                    }
                });
            } else {
                loadCover(image, listener);
            }
        }
    }

    /**
     * 获取选中的文字    应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public String getSelectionText() {
        TextSnippet snippet = myFBReaderApp.getTextView().getSelectedSnippet();
        if (snippet == null) return null;
        return snippet.getText();
    }

    /**
     * 清除文字的选中   应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public void clearSelection() {
        myFBReaderApp.getTextView().clearSelection();
    }

    /**
     * 屏幕方向    ZLibrary.SCREEN_ORIENTATION_SYSTEM
     *
     * @param optionValue
     */
    public void setOrientation(String optionValue) {
        int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if (ZLibrary.SCREEN_ORIENTATION_SENSOR.equals(optionValue)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        } else if (ZLibrary.SCREEN_ORIENTATION_PORTRAIT.equals(optionValue)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (ZLibrary.SCREEN_ORIENTATION_LANDSCAPE.equals(optionValue)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT.equals(optionValue)) {
            orientation = 9; // ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        } else if (ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE.equals(optionValue)) {
            orientation = 8; // ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
        activity.setRequestedOrientation(orientation);
        ZLibrary.Instance().getOrientationOption().setValue(optionValue);
        myFBReaderApp.onRepaintFinished();
    }

    /**
     * 设置页面滑动的动画效果
     *
     * @param anim
     */
    public void setSlideAnim(ZLViewEnums.Animation anim) {
        myFBReaderApp.PageTurningOptions.Animation.setValue(anim);
    }

    /**
     * 设置字体大小
     */
    public void setFontSize(int size) {
        myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption.setValue(size);
        if (myFBReaderApp.getViewWidget() != null) {
            myFBReaderApp.clearTextCaches();
            myFBReaderApp.getViewWidget().repaint();
        }
    }

    /**
     * 设置首行缩进距离   （推荐缩进距离为字体的两倍大小）
     *
     * @param px
     */
    public void setTextFirstLineIndent(int px) {
        List<ZLTextNGStyleDescription> descriptionList = myFBReaderApp.ViewOptions.getTextStyleCollection().getDescriptionList();
        for (ZLTextNGStyleDescription description : descriptionList) {
            if ("Regular Paragraph".equals(description.Name)) {
                description.TextIndentOption.setValue(px + "px");
                break;
            }
        }
    }

    /**
     * 设置阅读器屏幕亮度
     */
    public void setScreenBrightness(int value) {
        if (myFBReaderApp.getViewWidget() != null)
            myFBReaderApp.getViewWidget().setScreenBrightness(value);
    }

    /**
     * 顶部距离
     *
     * @param value
     */
    public void setMarginTop(int value) {
        myFBReaderApp.ViewOptions.TopMargin.setValue(value);
    }

    public void setMarginBottom(int value) {
        myFBReaderApp.ViewOptions.BottomMargin.setValue(value);
    }

    public void setMarginLeft(int value) {
        myFBReaderApp.ViewOptions.LeftMargin.setValue(value);
    }

    public void setMarginRight(int value) {
        myFBReaderApp.ViewOptions.RightMargin.setValue(value);
    }

    /**
     * 设置行间距
     *
     * @param value
     */
    public void setLineSpace(int value) {
        myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().LineSpaceOption.setValue(value);
    }

    /**
     * 设置颜色风格
     *
     * @param colorProfile ColorProfile.XXXXXXXX
     */
    public void setColorStyle(@NonNull String colorProfile) {
        myFBReaderApp.ViewOptions.ColorProfileName.setValue(colorProfile);
        if (myFBReaderApp.getViewWidget() != null) {
            myFBReaderApp.getViewWidget().reset();
            myFBReaderApp.getViewWidget().repaint();
        }
    }

    /**
     * 设置墙纸
     *
     * @param wallpagerUrl
     */
    public void setWallpaper(String wallpagerUrl) {
        myFBReaderApp.ViewOptions.getColorProfile().WallpaperOption.setValue("wallpapers/wood.jpg");
        if (myFBReaderApp.getViewWidget() != null) {
            myFBReaderApp.clearTextCaches();
            myFBReaderApp.getViewWidget().repaint();
        }
    }

    /**
     * 设置字体颜色
     *
     * @param color
     */
    public void setFontColor(int color) {
        myFBReaderApp.ViewOptions.getColorProfile().RegularTextOption.setValue(new ZLColor(color));
        if (myFBReaderApp.getViewWidget() != null) {
            myFBReaderApp.clearTextCaches();
            myFBReaderApp.getViewWidget().repaint();
        }
    }

    /**
     * 阅读时显示状态栏
     *
     * @param visible
     */
    public void setStatusBarVisibility(boolean visible) {
        ZLAndroidApplication.library().ShowStatusBarOption.setValue(visible);
    }

    /**
     * 设置阅读时电池容量大于value时不进行睡眠
     *
     * @param value
     */
    public void setBatteryLevelToTurnScreenOffValue(int value) {
        ZLAndroidApplication.library().BatteryLevelToTurnScreenOffOption.setValue(value);
    }

    /**
     * 设置阅读时底部键盘灯是否发亮
     */
    public void setButtonLightsDisable(boolean value) {
        ZLAndroidApplication.library().DisableButtonLightsOption.setValue(value);
    }

    /**
     * 跳转到
     *
     * @param tree
     */
    public void openBookTo(@NonNull TOCTree tree) {
        final TOCTree.Reference reference = tree.getReference();
        if (reference != null) {
            myFBReaderApp.addInvisibleBookmark();
            myFBReaderApp.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
            myFBReaderApp.showBookTextView();
            myFBReaderApp.storePosition();
        }
    }

    /**
     * 加载书本标签       应该先确定Collection是否已经绑定服务
     *
     * @param book
     * @return
     */
    public List<Bookmark> loadBookMarks(Book book) {
        List<Bookmark> bookmarks = new ArrayList<>(50);
        for (BookmarkQuery query = new BookmarkQuery(book, 50); ; query = query.next()) {
            final List<Bookmark> thisBookBookmarks = myFBReaderApp.Collection.bookmarks(query);
            if (thisBookBookmarks.isEmpty()) break;
            bookmarks.addAll(thisBookBookmarks);
        }
        return bookmarks;
    }

    /**
     * 跳转至指定标签     应该先确定Collection是否已经绑定服务且已打开书本
     * @param activity
     * @param bookmark
     */
    public void gotoBookMark(Activity activity, Bookmark bookmark) {
        FBReader.openBook(activity, myFBReaderApp.getCurrentBook(), bookmark);
    }

    /**
     * 保存阅读的位置     应该先确定Collection是否已经绑定服务
     *
     * @param book
     * @param position
     */
    public void storePosition(Book book, ZLTextFixedPosition position) {
        myFBReaderApp.Collection.storePosition(book.getId(), position);
    }

    public ZLTextFixedPosition getStorePosition(Book book) {
        return myFBReaderApp.Collection.getStoredPosition(book.getId());
    }

    /**
     * 删除书本标签       应该先确定Collection是否已经绑定服务
     *
     * @param bookmark
     */
    public void deleteBookMarker(Bookmark bookmark) {
        myFBReaderApp.Collection.deleteBookmark(bookmark);
    }

    /**
     * 删除书本所有标签
     */
    public void deleteAllBookMarker() {
        List<Bookmark> bookmarks = loadBookMarks(myFBReaderApp.getCurrentBook());
        for (Bookmark bookmark : bookmarks)
            myFBReaderApp.Collection.deleteBookmark(bookmark);
    }

    /**
     * 保存书本标签        应该先确定Collection是否已经绑定服务
     *
     * @param bookmark
     */
    public void saveBookMarker(Bookmark bookmark) {
        myFBReaderApp.Collection.saveBookmark(bookmark);
    }

    /**
     * 获取当前章节       应该先确定Collection是否已经绑定服务
     *
     * @return
     */
    public TOCTree getCurTOCTree() {
        return myFBReaderApp.getCurrentTOCElement();
    }

    /**
     * 获取当前书本的目录       应该先确定Collection是否已经绑定服务
     *
     * @return
     */
    public TOCTree getBookTOCTree() {
        return myFBReaderApp.Model.TOCTree;
    }

    /**
     * 根据书本目录名查询书本目录对象
     *
     * @param name  目录名称
     * @param level 第几级
     * @return
     */
    public TOCTree getBookTOCWithChapterName(String name, int level) {
        TOCTree root = getBookTOCTree();
        if (root == null) return null;
        else return getBookTOCWithChapterName(root.subtrees(), name, level);
    }

    public TOCTree getBookTOCWithChapterName(BookModel model, String name, int level) {
        TOCTree root = model.TOCTree;
        return getBookTOCWithChapterName(root.subtrees(), name, level);
    }

    private TOCTree getBookTOCWithChapterName(List<TOCTree> roots, String name, int level) {
        for (TOCTree root : roots) {
            if (root.Level > level) return null;
            else if (root.Level == level) {
                if (root.getText().trim().equals(name.trim())) return root;
            } else {
                TOCTree tocTree = getBookTOCWithChapterName(root.subtrees(), name, level);
                if (tocTree != null) return tocTree;
            }
        }
        return null;
    }

    /**
     * 获得从0到指定段落索引的字数    应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @param paragraphIndex
     * @return
     */
    public int getTextCountToParagraph(int paragraphIndex) {
        if (myFBReaderApp.Model == null) return 0;
        else return myFBReaderApp.Model.getTextModel().getTextLength(paragraphIndex);
    }

    /**
     * 获取书本总字数  应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public int getSumTextCount() {
        if (myFBReaderApp.Model == null) return 0;
        else {
            return myFBReaderApp.Model.getTextModel().getTextLength(
                    myFBReaderApp.Model.getTextModel().getParagraphsNumber()
            );
        }
    }

    /**
     * 获取阅读器屏幕亮度
     *
     * @return
     */
    public int getScreenBrightness() {
        if (myFBReaderApp.getViewWidget() != null)
            return myFBReaderApp.getViewWidget().getScreenBrightness();
        return -1;
    }

    /**
     * 获取书本总页数    应该先确定Collection是否已经绑定服务 并且已打开书本
     *
     * @return
     */
    public int getBookTotalPage() {
        return myFBReaderApp.getTextView().pagePosition().Total;
    }

    /**
     * 获取书本当前页数    应该先确定Collection是否已经绑定服务 并且已打开书本
     *
     * @return
     */
    public int getBookCurPage() {
        return myFBReaderApp.getTextView().pagePosition().Current;
    }

    /**
     * 获取本页开始的指针  应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public ZLTextWordCursor getStartCursor() {
        return myFBReaderApp.getTextView().getStartCursor();
    }

    /**
     * 获取本页结束的指针  应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public ZLTextWordCursor getEndCursor() {
        return myFBReaderApp.getTextView().getEndCursor();
    }

    /**
     * 获取当前页第一个字的位置    应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public ZLTextPosition getCurPageFirstWordPosition() {
        ZLTextWordCursor stCursor = getStartCursor();
        return new ZLTextFixedPosition(stCursor.getParagraphIndex(), stCursor.getElementIndex(), stCursor.getCharIndex());
    }

    /**
     * 获取当前页第一个字到文章开头的长度    应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public int getCurPageFirstWordTextLength() {
        ZLTextWordCursor stCursor = getStartCursor();
        return myFBReaderApp.Model.getTextModel().getTextLength(stCursor.getParagraphIndex()) + stCursor.getElementIndex();
    }

    /**
     * 获取当前页最后一个字到文章开头的长度   应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public int getCurPageLastWordTextLength() {
        ZLTextWordCursor stCursor = getEndCursor();
        return myFBReaderApp.Model.getTextModel().getTextLength(stCursor.getParagraphIndex()) + stCursor.getElementIndex();
    }

    /**
     * 获取当前页的字数量   应该先确定Collection是否已经绑定服务且已打开书本
     *
     * @return
     */
    public int getCurPageWordCount() {
        ZLTextWordCursor stCursor = getStartCursor();
        ZLTextWordCursor edCursor = getEndCursor();
        if (myFBReaderApp.Model == null || edCursor.getParagraphIndex() <= 0)
            return edCursor.getElementIndex();
        return myFBReaderApp.Model.getTextModel().getTextLength(edCursor.getParagraphIndex() - 1) + edCursor.getElementIndex() -
                myFBReaderApp.Model.getTextModel().getTextLength(stCursor.getParagraphIndex()) + stCursor.getElementIndex();
    }

    public Book getCurrentBook() {
        return myFBReaderApp.getCurrentBook();
    }

    public int getMarginTop() {
        return myFBReaderApp.ViewOptions.TopMargin.getValue();
    }

    public int getMarginBottom() {
        return myFBReaderApp.ViewOptions.BottomMargin.getValue();
    }

    public int getMarginLeft() {
        return myFBReaderApp.ViewOptions.LeftMargin.getValue();
    }

    public int getMarginRight() {
        return myFBReaderApp.ViewOptions.RightMargin.getValue();
    }

    public int getLineSpace(int value) {
        return myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().LineSpaceOption.getValue();
    }

    public boolean getButtonLightsDisable() {
        return ZLAndroidApplication.library().DisableButtonLightsOption.getValue();
    }

    /**
     * 获取字体大小
     *
     * @return
     */
    public int getFontSize() {
        return myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption.getValue();
    }

    /**
     * 获取颜色风格
     */
    public String getColorStyle() {
        return myFBReaderApp.ViewOptions.ColorProfileName.getValue();
    }

    private void loadCover(ZLImage image, OnGetCoverListener listener) {
        final ZLAndroidImageData data = ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(image);
        if (data == null) listener.finish(false, null);
        else listener.finish(true, data.getFullSizeBitmap());
    }

    public Activity getActivity() {
        return activity;
    }

    public interface OnGetCoverListener {
        void finish(boolean success, Bitmap bitmap);
    }

}
