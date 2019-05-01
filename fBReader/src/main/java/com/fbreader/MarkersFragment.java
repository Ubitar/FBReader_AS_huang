package com.fbreader;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.List;

public class MarkersFragment extends Fragment implements IRefresh, IBookCollection.Listener<Book> {

    private RecyclerView recyclerView;
    private MarkersAdapter adapter;

    private FBReaderHelper fbReaderHelper;

    public static MarkersFragment instance() {
        return new MarkersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalogs, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);

        return root;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new MarkersAdapter(null);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                fbReaderHelper.gotoBookMark(getActivity(), (Bookmark) adapter.getItem(position));
                ZLApplication.Instance().runAction(ActionCode.HIDE_TOC);
            }
        });
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ZLApplication.Instance().runAction(ActionCode.SHOW_BOOKMARK, adapter.getItem(position));
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        fbReaderHelper = new FBReaderHelper(getActivity());
        fbReaderHelper.bindToService(new Runnable() {
            @Override
            public void run() {
                fbReaderHelper.addListener(MarkersFragment.this);
                refresh();
            }
        });
    }

    @Override
    public void refresh() {
        if (fbReaderHelper == null) return;
        List<Bookmark> bookmarks = fbReaderHelper.loadBookMarks(fbReaderHelper.getCurrentBook());
        adapter.setNewData(bookmarks);
    }

    @Override
    public void onBookEvent(BookEvent event, Book book) {
        switch (event) {
            default:
                break;
            case BookmarksUpdated:
                refresh();
                break;
        }
    }

    @Override
    public void onBuildEvent(IBookCollection.Status status) {

    }

}
