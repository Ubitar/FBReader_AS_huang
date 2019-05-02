package com.fbreader.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fbreader.common.FBReaderHelper;

import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLViewEnums;
import org.geometerplus.zlibrary.ui.android.R;

/**
 * Created by laohuang on 2019/1/15.
 */
public class MenuFragment extends Fragment {

    private ImageView imgBack;
    private TextView txtCatalogs;
    private TextView txtToLibrary;
    private View layoutEmpty;
    private SeekBar seekBarBright;
    private TextView txtFontSizeSmaller;
    private TextView txtFontSize;
    private TextView txtFontSizeBigger;
    private RadioButton radioCFCFCF;
    private RadioButton radioC9BFAF;
    private RadioButton radioBFA875;
    private RadioButton radio8AB990;
    private RadioButton radio294867;
    private RadioButton radio59473F;
    private TextView txtAnimCurl;
    private TextView txtAnimShift;
    private TextView txtAnimSlideOldStyle;
    private TextView txtAnimNone;

    private FBReaderHelper fbReaderHelper;

    public static MenuFragment newInstance() {
        return new MenuFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, container, false);
        imgBack = root.findViewById(R.id.imgBack);
        txtCatalogs = root.findViewById(R.id.txtCatalogs);
        txtToLibrary=root.findViewById(R.id.txtToLibrary);
        layoutEmpty = root.findViewById(R.id.layoutEmpty);
        seekBarBright = root.findViewById(R.id.seekBarBright);
        txtFontSizeSmaller = root.findViewById(R.id.txtFontSizeSmaller);
        txtFontSize = root.findViewById(R.id.txtFontSize);
        txtFontSizeBigger = root.findViewById(R.id.txtFontSizeBigger);
        radioCFCFCF = root.findViewById(R.id.radioCFCFCF);
        radioC9BFAF = root.findViewById(R.id.radioC9BFAF);
        radioBFA875 = root.findViewById(R.id.radioBFA875);
        radio8AB990 = root.findViewById(R.id.radio8AB990);
        radio294867 = root.findViewById(R.id.radio294867);
        radio59473F = root.findViewById(R.id.radio59473F);
        txtAnimCurl = root.findViewById(R.id.txtAnimCurl);
        txtAnimShift = root.findViewById(R.id.txtAnimShift);
        txtAnimSlideOldStyle = root.findViewById(R.id.txtAnimSlideOldStyle);
        txtAnimNone = root.findViewById(R.id.txtAnimNone);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fbReaderHelper = new FBReaderHelper(getActivity());
        fbReaderHelper.bindToService(new Runnable() {
            @Override
            public void run() {
                initSetValue();
            }
        });
        initListener();
    }

    private void initListener() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FBReaderApp.Instance().closeWindow();
            }
        });
        txtCatalogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZLApplication.Instance().runAction(ActionCode.HIDE_MENU);
                ZLApplication.Instance().runAction(ActionCode.SHOW_TOC);
            }
        });
        txtToLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),LibraryActivity.class));
            }
        });
        layoutEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZLApplication.Instance().runAction(ActionCode.HIDE_MENU);
            }
        });
        seekBarBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                fbReaderHelper.setScreenBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        txtFontSizeSmaller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int fontSize = fbReaderHelper.getFontSize() - 2;
                txtFontSize.setText(fontSize + "");
                fbReaderHelper.setFontSize(fontSize);
                fbReaderHelper.setTextFirstLineIndent(fontSize * 2);
            }
        });
        txtFontSizeBigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int fontSize = fbReaderHelper.getFontSize() + 2;
                txtFontSize.setText(fontSize + "");
                fbReaderHelper.setFontSize(fontSize);
                fbReaderHelper.setTextFirstLineIndent(fontSize * 2);
            }
        });
        radioCFCFCF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setColorStyle(ColorProfile.COLOR_CFCFCF);
            }
        });
        radioC9BFAF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setColorStyle(ColorProfile.COLOR_C9BFAF);
            }
        });
        radioBFA875.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setColorStyle(ColorProfile.COLOR_BFA875);
            }
        });
        radio8AB990.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setColorStyle(ColorProfile.COLOR_8AB990);
            }
        });
        radio294867.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setColorStyle(ColorProfile.COLOR_294867);
            }
        });
        radio59473F.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setColorStyle(ColorProfile.COLOR_59473F);
            }
        });
        txtAnimCurl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setSlideAnim(ZLViewEnums.Animation.curl);
                Toast toast = Toast.makeText(getActivity(), "切换为仿真动画模式", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
        txtAnimShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setSlideAnim(ZLViewEnums.Animation.shift);
                Toast toast = Toast.makeText(getActivity(), "切换为移动动画模式", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
        txtAnimSlideOldStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setSlideAnim(ZLViewEnums.Animation.slideOldStyle);
                Toast toast = Toast.makeText(getActivity(), "切换为视差滑动模式", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
        txtAnimNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbReaderHelper.setSlideAnim(ZLViewEnums.Animation.none);
                Toast toast = Toast.makeText(getActivity(), "切换为无动画模式", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    private void initSetValue() {
        txtFontSize.setText(fbReaderHelper.getFontSize() + "");
        seekBarBright.setProgress(fbReaderHelper.getScreenBrightness());
        String colorStyle = fbReaderHelper.getColorStyle();
        if (ColorProfile.COLOR_CFCFCF.equals(colorStyle)) {
            radioCFCFCF.setChecked(true);
        } else if (ColorProfile.COLOR_C9BFAF.equals(colorStyle)) {
            radioC9BFAF.setChecked(true);
        } else if (ColorProfile.COLOR_BFA875.equals(colorStyle)) {
            radioBFA875.setChecked(true);
        } else if (ColorProfile.COLOR_8AB990.equals(colorStyle)) {
            radio8AB990.setChecked(true);
        } else if (ColorProfile.COLOR_294867.equals(colorStyle)) {
            radio294867.setChecked(true);
        } else if (ColorProfile.COLOR_59473F.equals(colorStyle)) {
            radio59473F.setChecked(true);
        }
    }

}
