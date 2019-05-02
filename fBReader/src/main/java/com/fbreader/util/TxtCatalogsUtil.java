package com.fbreader.util;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TxtCatalogsUtil {

    private static final String ChapterPatternStr = "(\\s*第)(.{1,9})[章节卷集部篇回](.+)(\\s)";

    public static TOCTree parseTxtCatalogs(BookModel model) {
        ZLTextModel textModel = model.getTextModel();
        String textData = null;
        Pattern p = Pattern.compile(ChapterPatternStr);
        Matcher matcher = null;

        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();

        outFor:
        for (int i = 0, paragraphsNumber = textModel.getParagraphsNumber(); i < paragraphsNumber; i++) {
            ZLTextParagraph.EntryIterator entryIterator = textModel.getParagraph(i).iterator();
            while (entryIterator.next()) {
                if (entryIterator.getType() == ZLTextParagraph.Entry.TEXT && textData == null) {
                    textData = String.valueOf(entryIterator.getTextData());
                    matcher = p.matcher(textData);
                    while (matcher.find()) {
                        starts.add(matcher.start(0));
                        ends.add(matcher.end(0) - 1);
                    }
                    break outFor;
                }
            }
        }

        outFor:
        if (textData != null && starts.size() > 0) {
            int catalogIndex = 0;
            int startIndex = starts.get(catalogIndex);
            int endIndex = ends.get(catalogIndex);
            for (int i = 0, paragraphsNumber = textModel.getParagraphsNumber(); i < paragraphsNumber; i++) {
                ZLTextParagraph.EntryIterator entryIterator = textModel.getParagraph(i).iterator();
                while (entryIterator.next()) {
                    if (entryIterator.getType() == ZLTextParagraph.Entry.TEXT && startIndex <= entryIterator.getTextOffset()) {
                        String catalogStr = textData.substring(startIndex, endIndex);

                        TOCTree tree = new TOCTree(model.TOCTree);
                        tree.setText(catalogStr);
                        tree.setReference(textModel, i);

                        if (++catalogIndex >= starts.size()) {
                            break outFor;
                        } else {
                            startIndex = starts.get(catalogIndex);
                            endIndex = ends.get(catalogIndex);
                        }
                    }
                }
            }
        }

        return model.TOCTree;
    }


}
