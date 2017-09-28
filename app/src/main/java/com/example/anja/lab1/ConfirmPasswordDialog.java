package com.example.anja.lab1;

import android.app.Activity;
import android.app.DialogFragment;
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

    private boolean passwordsMatch = false;

    public interface DialogListener {
        void onPasswordsMatch(String passwordStatus);
    }

    DialogListener mListener;

    public String getMatchStatus(){
        return Boolean.toString(passwordsMatch);
    } //TODO: Send value of this property back to SettingsFragment. Save button should only be enabled if passwords match.

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
                    passwordsMatch = true;

                    dismiss();
                    Toast.makeText(getActivity(),"Passwords match", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        Log.d("DIALOG", "attached");
    }
}
