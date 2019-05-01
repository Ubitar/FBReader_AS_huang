package com.fbreader;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.geometerplus.zlibrary.ui.android.R;

/**
 * Created by huangpinzhang on 2018/7/16.
 */

public class AlertConfirmDialog extends Dialog {

    private TextView txtTitle;
    private TextView txtContent;
    private Button btnCancel;
    private Button btnSubmit;
    private View viewSep;

    private int iSubmitColor, iCancelColor, iContentGravity, iContentColor, iTitleColor, iTitleGravity;
    private boolean isSubmitHide, isCancelHide;
    private String content, title, submitText, cancelText;

    private OnClickSubmitListener onClickSubmitListener;
    private OnClickCancelListener onClickCancelListener;


    public AlertConfirmDialog(@NonNull Context context) {
        super(context);

    }

    public AlertConfirmDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AlertConfirmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_simple_confirm);
        getWindow().setDimAmount(0);
        viewSep = findViewById(R.id.viewSep);
        txtTitle = findViewById(R.id.txtTitle);
        txtContent = findViewById(R.id.txtContent);
        btnCancel = findViewById(R.id.btnCancel);
        btnSubmit = findViewById(R.id.btnSubmit);

        initTitle();
        initContent();
        initBtnCancel();
        initBtnSubmit();
    }

    public AlertConfirmDialog setHideCancel(boolean isShow) {
        isCancelHide = isShow;
        if (btnCancel != null) {
            if (isShow) btnCancel.setVisibility(View.VISIBLE);
            else btnCancel.setVisibility(View.GONE);
        }
        return this;
    }

    public AlertConfirmDialog setHideSubmit(boolean isShow) {
        isSubmitHide = isShow;
        if (btnSubmit != null) {
            if (isShow) btnSubmit.setVisibility(View.VISIBLE);
            else btnSubmit.setVisibility(View.GONE);
        }
        return this;
    }

    public AlertConfirmDialog setCancelBtnTextColor(int color) {
        iCancelColor = color;
        if (btnCancel != null) btnCancel.setTextColor(color);
        return this;
    }

    public AlertConfirmDialog setSubmitBtnTextColor(int color) {
        iSubmitColor = color;
        if (btnSubmit != null) btnSubmit.setTextColor(color);
        return this;
    }

    public AlertConfirmDialog setContentText(String text) {
        content = text;
        if (txtContent != null) txtContent.setText(text);
        return this;
    }

    public AlertConfirmDialog setContentTextGravity(int gravity) {
        iContentGravity = gravity;
        if (txtContent != null) txtContent.setGravity(gravity);
        return this;
    }

    public AlertConfirmDialog setContentTextColor(int color) {
        iContentColor = color;
        if (txtContent != null) txtContent.setTextColor(color);
        return this;
    }

    public AlertConfirmDialog setTitleText(String text) {
        title = text;
        if (txtTitle != null) txtTitle.setText(text);
        return this;
    }

    public AlertConfirmDialog setTitleTextGravity(int gravity) {
        iTitleGravity = gravity;
        if (txtTitle != null) txtTitle.setGravity(gravity);
        return this;
    }

    public AlertConfirmDialog setTitleTextColor(int color) {
        iTitleColor = color;
        if (txtTitle != null) txtTitle.setTextColor(color);
        return this;
    }

    public AlertConfirmDialog setSubmitText(String submitText) {
        this.submitText = submitText;
        if (btnSubmit != null) btnSubmit.setText(submitText);
        return this;
    }

    public AlertConfirmDialog setCancelText(String cancelText) {
        this.cancelText = cancelText;
        if (btnCancel != null) btnCancel.setText(cancelText);
        return this;
    }

    public OnClickSubmitListener getOnClickSubmitListener() {
        return onClickSubmitListener;
    }

    public AlertConfirmDialog setOnClickSubmitListener(OnClickSubmitListener onClickSubmitListener) {
        this.onClickSubmitListener = onClickSubmitListener;
        return this;
    }

    public OnClickCancelListener getOnClickCancelListener() {
        return onClickCancelListener;
    }

    public AlertConfirmDialog setOnClickCancelListener(OnClickCancelListener onClickCancelListener) {
        this.onClickCancelListener = onClickCancelListener;
        return this;
    }

    public void checkSepView() {
        if (btnCancel.getVisibility() == View.GONE || btnSubmit.getVisibility() == View.GONE)
            viewSep.setVisibility(View.GONE);
        else viewSep.setVisibility(View.VISIBLE);
    }

    private void initTitle() {
        if (title == null) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setText(title);
            txtTitle.setVisibility(View.VISIBLE);
        }
    }

    private void initContent() {
        if (content == null) {
            txtContent.setVisibility(View.GONE);
        } else {
            txtContent.setText(content);
            txtContent.setVisibility(View.VISIBLE);
        }
        if (iContentGravity != 0) txtContent.setGravity(iContentGravity);
    }

    private void initBtnCancel() {
        if (isCancelHide) btnCancel.setVisibility(View.GONE);
        else btnCancel.setVisibility(View.VISIBLE);
        checkSepView();
        if (iCancelColor != 0) btnCancel.setTextColor(iCancelColor);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickCancelListener != null) onClickCancelListener.onClick(view);
                dismiss();
            }
        });
    }

    private void initBtnSubmit() {
        if (isSubmitHide) btnSubmit.setVisibility(View.GONE);
        else btnSubmit.setVisibility(View.VISIBLE);
        checkSepView();
        if (iSubmitColor != 0) btnSubmit.setTextColor(iSubmitColor);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickSubmitListener != null)
                    onClickSubmitListener.onClick(view);
                dismiss();
            }
        });
    }

    public interface OnClickSubmitListener {
        void onClick(View view);
    }

    public interface OnClickCancelListener {
        void onClick(View view);
    }
}
