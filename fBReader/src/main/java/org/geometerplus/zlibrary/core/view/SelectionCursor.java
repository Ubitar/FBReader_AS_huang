package org.geometerplus.zlibrary.core.view;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;

public abstract class SelectionCursor {
	public enum Which {
		Left,
		Right
	}

	public static void draw(ZLPaintContext context, Which which, int x, int y, ZLColor color) {
		context.setFillColor(color);
		final int dpi = ZLibrary.Instance().getDisplayDPI();
		final int unit = dpi / 120;
		final int xCenter = which == Which.Left ? x - unit - 1 : x + unit + 1;
		context.fillRectangle(xCenter - unit, y + dpi /12, xCenter + unit, y - dpi / 12);
		if (which == Which.Left) {
			context.fillCircle(xCenter, y - dpi / 10, unit * 5);
		} else {
			context.fillCircle(xCenter, y + dpi / 10, unit * 5);
		}
	}
}
