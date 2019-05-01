package org.geometerplus.fbreader.book;

import android.os.Parcel;
import android.os.Parcelable;

import org.fbreader.util.ComparisonUtil;
import org.geometerplus.fbreader.util.TextSnippet;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import java.util.Comparator;
import java.util.UUID;

public final class Bookmark extends ZLTextFixedPosition implements Parcelable {
    public enum DateType {
        Creation,
        Modification,
        Access,
        Latest
    }

    private long myId;
    public final String Uid;
    private String myVersionUid;

    public final long BookId;
    public final String BookTitle;
    private String myText;
    private String myOriginalText;

    public final long CreationTimestamp;
    private Long myModificationTimestamp;
    private Long myAccessTimestamp;
    private ZLTextFixedPosition myEnd;
    private int myLength;
    private int myStyleId;

    public final String ModelId;
    public final boolean IsVisible;


    // used for migration only
    private Bookmark(long bookId, Bookmark original) {
        super(original);
        myId = -1;
        Uid = newUUID();
        BookId = bookId;
        BookTitle = original.BookTitle;
        myText = original.myText;
        myOriginalText = original.myOriginalText;
        CreationTimestamp = original.CreationTimestamp;
        myModificationTimestamp = original.myModificationTimestamp;
        myAccessTimestamp = original.myAccessTimestamp;
        myEnd = original.myEnd;
        myLength = original.myLength;
        myStyleId = original.myStyleId;
        ModelId = original.ModelId;
        IsVisible = original.IsVisible;
    }

    // create java object for existing bookmark
    // uid parameter can be null when comes from old format plugin!
    public Bookmark(
            long id, String uid, String versionUid,
            long bookId, String bookTitle, String text, String originalText,
            long creationTimestamp, Long modificationTimestamp, Long accessTimestamp,
            String modelId,
            int start_paragraphIndex, int start_elementIndex, int start_charIndex,
            int end_paragraphIndex, int end_elementIndex, int end_charIndex,
            boolean isVisible,
            int styleId
    ) {
        super(start_paragraphIndex, start_elementIndex, start_charIndex);

        myId = id;
        Uid = verifiedUUID(uid);
        myVersionUid = verifiedUUID(versionUid);

        BookId = bookId;
        BookTitle = bookTitle;
        myText = text;
        myOriginalText = originalText;
        CreationTimestamp = creationTimestamp;
        myModificationTimestamp = modificationTimestamp;
        myAccessTimestamp = accessTimestamp;
        ModelId = modelId;
        IsVisible = isVisible;

        if (end_charIndex >= 0) {
            myEnd = new ZLTextFixedPosition(end_paragraphIndex, end_elementIndex, end_charIndex);
        } else {
            myLength = end_paragraphIndex;
        }

        myStyleId = styleId;
    }

    // creates new bookmark
    public Bookmark(IBookCollection collection, Book book, String modelId, TextSnippet snippet, boolean visible) {
        super(snippet.getStart());

        myId = -1;
        Uid = newUUID();
        BookId = book.getId();
        BookTitle = book.getTitle();
        myText = snippet.getText();
        myOriginalText = snippet.getText();
        CreationTimestamp = System.currentTimeMillis();
        ModelId = modelId;
        IsVisible = visible;
        myEnd = new ZLTextFixedPosition(snippet.getEnd());
        myStyleId = collection.getDefaultHighlightingStyleId();
    }

    public long getId() {
        return myId;
    }

    public String getVersionUid() {
        return myVersionUid;
    }

    private void onModification() {
        myVersionUid = newUUID();
        myModificationTimestamp = System.currentTimeMillis();
    }

    public int getStyleId() {
        return myStyleId;
    }

    public void setStyleId(int styleId) {
        if (styleId != myStyleId) {
            myStyleId = styleId;
            onModification();
        }
    }

    public String getText() {
        return myText;
    }

    public String getOriginalText() {
        return myOriginalText;
    }

    public void setMyOriginalText(String myOriginalText) {
        this.myOriginalText = myOriginalText;
    }

    public void setText(String text) {
        myText = text;
        onModification();
    }

    public Long getTimestamp(DateType type) {
        switch (type) {
            case Creation:
                return CreationTimestamp;
            case Modification:
                return myModificationTimestamp;
            case Access:
                return myAccessTimestamp;
            default:
            case Latest: {
                Long latest = myModificationTimestamp;
                if (latest == null) {
                    latest = CreationTimestamp;
                }
                if (myAccessTimestamp != null && latest < myAccessTimestamp) {
                    return myAccessTimestamp;
                } else {
                    return latest;
                }
            }
        }
    }

    public ZLTextPosition getEnd() {
        return myEnd;
    }

    void setEnd(int paragraphsIndex, int elementIndex, int charIndex) {
        myEnd = new ZLTextFixedPosition(paragraphsIndex, elementIndex, charIndex);
    }

    public int getLength() {
        return myLength;
    }

    public void markAsAccessed() {
        myVersionUid = newUUID();
        myAccessTimestamp = System.currentTimeMillis();
    }

    public static class ByTimeComparator implements Comparator<Bookmark> {
        public int compare(Bookmark bm0, Bookmark bm1) {
            final Long ts0 = bm0.getTimestamp(DateType.Latest);
            final Long ts1 = bm1.getTimestamp(DateType.Latest);
            // yes, reverse order; yes, latest ts is not null
            return ts1.compareTo(ts0);
        }
    }

    void setId(long id) {
        myId = id;
    }

    public void update(Bookmark other) {
        // TODO: copy other fields (?)
        if (other != null) {
            myId = other.myId;
        }
    }

    Bookmark transferToBook(AbstractBook book) {
        final long bookId = book.getId();
        return bookId != -1 ? new Bookmark(bookId, this) : null;
    }

    // not equals, we do not compare ids
    boolean sameAs(Bookmark other) {
        return
                ParagraphIndex == other.ParagraphIndex &&
                        ElementIndex == other.ElementIndex &&
                        CharIndex == other.CharIndex &&
                        ComparisonUtil.equal(myText, other.myText);
    }

    private static String newUUID() {
        return UUID.randomUUID().toString();
    }

    private static String verifiedUUID(String uid) {
        if (uid == null || uid.length() == 36) {
            return uid;
        }
        throw new RuntimeException("INVALID UUID: " + uid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.myId);
        dest.writeString(this.Uid);
        dest.writeString(this.myVersionUid);
        dest.writeLong(this.BookId);
        dest.writeString(this.BookTitle);
        dest.writeString(this.myText);
        dest.writeString(this.myOriginalText);
        dest.writeLong(this.CreationTimestamp);
        dest.writeValue(this.myModificationTimestamp);
        dest.writeValue(this.myAccessTimestamp);
        dest.writeParcelable(this.myEnd, flags);
        dest.writeInt(this.myLength);
        dest.writeInt(this.myStyleId);
        dest.writeString(this.ModelId);
        dest.writeByte(this.IsVisible ? (byte) 1 : (byte) 0);
        dest.writeInt(this.ParagraphIndex);
        dest.writeInt(this.ElementIndex);
        dest.writeInt(this.CharIndex);
    }

    protected Bookmark(Parcel in) {
        super(in);
        this.myId = in.readLong();
        this.Uid = in.readString();
        this.myVersionUid = in.readString();
        this.BookId = in.readLong();
        this.BookTitle = in.readString();
        this.myText = in.readString();
        this.myOriginalText = in.readString();
        this.CreationTimestamp = in.readLong();
        this.myModificationTimestamp = (Long) in.readValue(Long.class.getClassLoader());
        this.myAccessTimestamp = (Long) in.readValue(Long.class.getClassLoader());
        this.myEnd = in.readParcelable(ZLTextFixedPosition.class.getClassLoader());
        this.myLength = in.readInt();
        this.myStyleId = in.readInt();
        this.ModelId = in.readString();
        this.IsVisible = in.readByte() != 0;
        this.ParagraphIndex = in.readInt();
        this.ElementIndex = in.readInt();
        this.CharIndex = in.readInt();
    }

    public static final Creator<Bookmark> CREATOR = new Creator<Bookmark>() {
        @Override
        public Bookmark createFromParcel(Parcel source) {
            return new Bookmark(source);
        }

        @Override
        public Bookmark[] newArray(int size) {
            return new Bookmark[size];
        }
    };
}
