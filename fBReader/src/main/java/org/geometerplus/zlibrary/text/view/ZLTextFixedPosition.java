/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.view;

import android.os.Parcel;
import android.os.Parcelable;

public class ZLTextFixedPosition extends ZLTextPosition implements Parcelable {
    public int ParagraphIndex;
    public int ElementIndex;
    public int CharIndex;

    public ZLTextFixedPosition(int paragraphIndex, int elementIndex, int charIndex) {
        ParagraphIndex = paragraphIndex;
        ElementIndex = elementIndex;
        CharIndex = charIndex;
    }

    public ZLTextFixedPosition(ZLTextPosition position) {
        ParagraphIndex = position.getParagraphIndex();
        ElementIndex = position.getElementIndex();
        CharIndex = position.getCharIndex();
    }

    protected ZLTextFixedPosition(Parcel in) {
        ParagraphIndex = in.readInt();
        ElementIndex = in.readInt();
        CharIndex = in.readInt();
    }

    public static final Creator<ZLTextFixedPosition> CREATOR = new Creator<ZLTextFixedPosition>() {
        @Override
        public ZLTextFixedPosition createFromParcel(Parcel in) {
            return new ZLTextFixedPosition(in);
        }

        @Override
        public ZLTextFixedPosition[] newArray(int size) {
            return new ZLTextFixedPosition[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ParagraphIndex);
        dest.writeInt(ElementIndex);
        dest.writeInt(CharIndex);
    }

    public final int getParagraphIndex() {
        return ParagraphIndex;
    }

    public final int getElementIndex() {
        return ElementIndex;
    }

    public final int getCharIndex() {
        return CharIndex;
    }


    public static class WithTimestamp extends ZLTextFixedPosition {
        public final long Timestamp;

        public WithTimestamp(int paragraphIndex, int elementIndex, int charIndex, Long stamp) {
            super(paragraphIndex, elementIndex, charIndex);
            Timestamp = stamp != null ? stamp : -1;
        }

        @Override
        public String toString() {
            return super.toString() + "; timestamp = " + Timestamp;
        }
    }
}
