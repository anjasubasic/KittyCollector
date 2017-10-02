package com.example.anja.lab1;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class ConfirmPasswordDialog extends android.support.v4.app.DialogFragment {

    public static String dialogPassword;
    public static int requestCode = 123;

    static ConfirmPasswordDialog newInstance(String verifyPassword) {
        ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();

        Bundle args = new Bundle();
        args.putString("verifyPassword", verifyPassword);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {

        final View view = inflater.inflate(R.layout.dialog, null);
        EditText txtEdit = (EditText) view.findViewById(R.id.passwd);
        final String firstPassword = getArguments().getString("verifyPassword");

        txtEdit.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(firstPassword))
                {
                    dialogPassword = charSequence.toString();
                    Intent intent = new Intent();
                    intent.putExtra("STRING_RESULT", charSequence.toString());
                    getTargetFragment().onActivityResult(getTargetRequestCode(), requestCode, intent);
                    dismiss();
                    //Toast.makeText(getActivity(),"Passwords match", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        return view;
    }
}