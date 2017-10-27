package com.example.anja.lab1;

/**
 * Created by jennyseong on 10/26/17.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class PasswordChangeDialog extends android.support.v4.app.DialogFragment {

    public static String dialogPassword, newPassword, currentPassword;
    public static int requestSend = 123, requestCancel = 321;
    EditText currentPwd, newPwd, checkPwd;
    Button backButton, saveButton;
    Boolean inputValid = false, pwdMatch = false, currentValid = false;
    ImageView checkImage;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        final View view = inflater.inflate(R.layout.pwdchange_dialog, null);
        currentPwd = view.findViewById(R.id.currentPasswordInput);
        newPwd = view.findViewById(R.id.newPasswordInput);
        checkPwd = view.findViewById(R.id.passwordCheck);
        saveButton = view.findViewById(R.id.pwdSaveButton);
        backButton = view.findViewById(R.id.pwdCancelButton);
        checkImage = view.findViewById(R.id.passwordCheckImage);

        setSaveButtonEnabled();

        currentPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (currentPwd.getText().length() == 0) { currentValid = false; }
                else { currentValid = true; }
                currentPassword = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) { setSaveButtonEnabled(); }
        });

        newPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (newPwd.getText().length() == 0) { inputValid = false; }
                else { inputValid = true; }

                newPassword = charSequence.toString();
                if(newPassword.equals(dialogPassword)) { pwdMatch = true; }
                else { pwdMatch = false; }
            }

            @Override
            public void afterTextChanged(Editable editable) { setSaveButtonEnabled(); }
        });

        checkPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dialogPassword = charSequence.toString();
                if (dialogPassword.equals(newPassword)) {
                    pwdMatch = true;
                    checkImage.setImageResource(R.drawable.checkmark);
                } else {
                    pwdMatch = false;
                    checkImage.setImageResource(R.drawable.cross);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { setSaveButtonEnabled(); }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                getTargetFragment().onActivityResult(getTargetRequestCode(), requestCancel, intent);
                dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("NEW_PWD", dialogPassword);
                intent.putExtra("CURRENT_PWD", currentPassword);
                getTargetFragment().onActivityResult(getTargetRequestCode(), requestSend, intent);
                dismiss();
            }
        });


        return view;
    }

    // Workaround for the SaveInstanceState support package bug. https://issuetracker.google.com/issues/36932872
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commit();
        } catch (IllegalStateException e) {
            Log.d("DIALOG_FRAG", "Committing after SavedInstanceState", e);
        }
    }

    public void setSaveButtonEnabled() {
        if (inputValid && pwdMatch && currentValid) {
            saveButton.setAlpha(1f);
            saveButton.setEnabled(true);
        }
        else {
            saveButton.setAlpha(0.5f);
            saveButton.setEnabled(false);
        }
    }
}
