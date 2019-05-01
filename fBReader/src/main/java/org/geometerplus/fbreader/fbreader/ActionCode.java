
package org.geometerplus.fbreader.fbreader;

public interface ActionCode {
    String SHOW_PREFERENCES = "preferences";
    String SHOW_BOOK_INFO = "bookInfo";
    String SHOW_BOOKMARK = "SHOW_BOOKMARK";
    String HIDE_BOOKMARK = "HIDE_BOOKMARK";


    String SEARCH = "search";
    String FIND_PREVIOUS = "findPrevious";
    String FIND_NEXT = "findNext";
    String CLEAR_FIND_RESULTS = "clearFindResults";

    String SET_TEXT_VIEW_MODE_VISIT_HYPERLINKS = "hyperlinksOnlyMode";
    String SET_TEXT_VIEW_MODE_VISIT_ALL_WORDS = "dictionaryMode";

    String TURN_PAGE_BACK = "previousPage";
    String TURN_PAGE_FORWARD = "nextPage";

    String MOVE_CURSOR_UP = "moveCursorUp";
    String MOVE_CURSOR_DOWN = "moveCursorDown";
    String MOVE_CURSOR_LEFT = "moveCursorLeft";
    String MOVE_CURSOR_RIGHT = "moveCursorRight";

    String VOLUME_KEY_SCROLL_FORWARD = "volumeKeyScrollForward";
    String VOLUME_KEY_SCROLL_BACK = "volumeKeyScrollBackward";
    String SHOW_MENU = "SHOW_MENU";
    String HIDE_MENU = "HIDE_MENU";
    String SHOW_TOC = "SHOW_TOC";
    String HIDE_TOC = "HIDE_TOC";
    String SHOW_NAVIGATION = "navigate";
    String PAGE_TURN_START = "PAGE_TURN_START";
    String PAGE_TURNING = "PAGE_TURNING";
    String PAGE_TURNING_END = "PAGE_TURNING_END";

    String GO_BACK = "goBack";

    String SET_SCREEN_ORIENTATION_SYSTEM = "screenOrientationSystem";
    String SET_SCREEN_ORIENTATION_SENSOR = "screenOrientationSensor";
    String SET_SCREEN_ORIENTATION_PORTRAIT = "screenOrientationPortrait";
    String SET_SCREEN_ORIENTATION_LANDSCAPE = "screenOrientationLandscape";
    String SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT = "screenOrientationReversePortrait";
    String SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE = "screenOrientationReverseLandscape";

    String DISPLAY_BOOK_POPUP = "displayBookPopup";
    String PROCESS_HYPERLINK = "processHyperlink";

    String SELECTION_SHOW_PANEL = "selectionShowPanel";
    String SELECTION_HIDE_PANEL = "selectionHidePanel";
    String SELECTION_CLEAR = "selectionClear";
    String SELECTION_COPY_TO_CLIPBOARD = "selectionCopyToClipboard";
    String SELECTION_BOOKMARK = "selectionBookmark";

    String OPEN_VIDEO = "video";

    String HIDE_TOAST = "hideToast";
    String INSTALL_PLUGINS = "plugins";
}
