package com.fbreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

public class MarkerFragment extends Fragment {

    private ImageView imgClose;
    private TextView txtOriginal;
    private EditText edtContent;
    private TextView txtDel;
    private TextView txtChange;

    private View layoutEmpty, layoutEmpty2;
    private AlertConfirmDialog delDialog;


    private FBReaderHelper fbReaderHelper;

    private Bookmark bookmark;

    public static MarkerFragment newInstance(@Nullable Bookmark bookmark) {
        MarkerFragment markerFragment = new MarkerFragment();
        Bundle bundle = new Bundle();
        if (bookmark != null) bundle.putParcelable(IntentKey.BOOK_MARKER, bookmark);
        markerFragment.setArguments(bundle);
        return markerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_marker, container, false);
        imgClose = root.findViewById(R.id.imgClose);
        txtOriginal = root.findViewById(R.id.txtOriginal);
        edtContent = root.findViewById(R.id.edtContent);
        txtDel = root.findViewById(R.id.txtDel);
        txtChange = root.findViewById(R.id.txtChange);
        layoutEmpty = root.findViewById(R.id.layoutEmpty);
        layoutEmpty2 = root.findViewById(R.id.layoutEmpty2);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fbReaderHelper = new FBReaderHelper(getActivity());
        fbReaderHelper.bindToService(null);
        delDialog = new AlertConfirmDialog(getActivity());
        delDialog.setTitleText("是否删除").setOnClickSubmitListener(new AlertConfirmDialog.OnClickSubmitListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.deleteBookMarker(bookmark);
                ZLApplication.Instance().runAction(ActionCode.HIDE_BOOKMARK);
                SoftInputUtil.hideSoftKeyboard(getActivity());
                Toast.makeText(getActivity(),"修改成功",Toast.LENGTH_SHORT).show();
            }
        });
        initListener();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Bundle bundle = getArguments();
            if (bundle.containsKey(IntentKey.BOOK_MARKER)) {
                bookmark = (Bookmark) bundle.getParcelable(IntentKey.BOOK_MARKER);
                txtOriginal.setText(bookmark.getOriginalText());
                edtContent.setText(bookmark.getText());
            }
        }
    }

    private void initListener() {
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZLApplication.Instance().runAction(ActionCode.HIDE_BOOKMARK);
                SoftInputUtil.hideSoftKeyboard(getActivity());
            }
        });
        txtDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delDialog.show();
            }
        });
        txtChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmark.setText(edtContent.getText().toString());
                fbReaderHelper.saveBookMarker(bookmark);
                ZLApplication.Instance().runAction(ActionCode.HIDE_BOOKMARK);
                SoftInputUtil.hideSoftKeyboard(getActivity());
            }
        });
        layoutEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZLApplication.Instance().runAction(ActionCode.HIDE_BOOKMARK);
                SoftInputUtil.hideSoftKeyboard(getActivity());
            }
        });
        layoutEmpty2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZLApplication.Instance().runAction(ActionCode.HIDE_BOOKMARK);
                SoftInputUtil.hideSoftKeyboard(getActivity());
            }
        });
    }
}
