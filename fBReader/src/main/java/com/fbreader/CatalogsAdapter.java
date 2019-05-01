package com.fbreader;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.List;

public class CatalogsAdapter extends BaseQuickAdapter<TOCTree, BaseViewHolder> {

    private int selectedIndex = -1;

    public CatalogsAdapter(@Nullable List<TOCTree> data) {
        super(R.layout.holder_catalogs_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, TOCTree item) {
        helper.setText(R.id.txt, item.getText());
        helper.setGone(R.id.imgMark, helper.getLayoutPosition() == selectedIndex);
    }

    public int setSelectItem(TOCTree selectItem) {
        if (selectItem == null || getData().size() <= 0) return -1;
        List<TOCTree> tocTrees = getData();
        for (int i = 0, size = tocTrees.size(); i < size; i++) {
            TOCTree tocTree = tocTrees.get(i);
            if (selectItem.getReference().ParagraphIndex == tocTree.getReference().ParagraphIndex) {
                if (i != selectedIndex) {
                    int oldIndex = selectedIndex;
                    selectedIndex = i;
                    if (oldIndex >= 0) setData(oldIndex, getItem(oldIndex));
                    setData(i, selectItem);
                }
                break;
            }
        }
        return selectedIndex;
    }
}
