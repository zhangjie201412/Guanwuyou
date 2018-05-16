package com.iot.zhs.guanwuyou.view;

import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.iot.zhs.guanwuyou.R;

/**
 * Created by H151136 on 1/27/2018.
 */

public class NotificationDialog extends DialogFragment {
    private String mTitle;
    private String mMessage;
    private String mButtonText1;
    private String mButtonText2;
    private NotificationDialogListener mListener;

    private TextView mMessageTextView;
    private Button mButton1;
    private Button mButton2;

    public interface NotificationDialogListener {
        void onButtonClick(int id);
    }

    public void init(String title, String buttonText1, String buttonText2, NotificationDialogListener listener) {
        mTitle = title;
        mButtonText1 = buttonText1;
        mButtonText2 = buttonText2;
        mListener = listener;
    }

    public void setMessage(String message) {
        if(mMessageTextView != null) {
            mMessageTextView.setText(message);
        }
        mMessage = message;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_notification, container, false);

        mMessageTextView = view.findViewById(R.id.dialog_message);
        mButton1 = view.findViewById(R.id.dialog_button1);
        mButton2 = view.findViewById(R.id.dialog_button2);
        mMessageTextView.setText(mMessage);
        mButton1.setText(mButtonText1);
        mButton2.setText(mButtonText2);
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onButtonClick(1);
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onButtonClick(2);
            }
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(mTitle);

        return dialog;
    }
}
