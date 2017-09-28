package com.example.anja.lab1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Anja on 9/24/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {

    private static final String TAG = "Settings";
    private ConfirmPasswordDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container,false);
        final Button clearButton = (Button) view.findViewById(R.id.clear_button);
        final Button saveButton = (Button) view.findViewById(R.id.save_button);

        clearButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText charTxtEdit = (EditText) getActivity().findViewById(R.id.character_edit_text);
                        final EditText nameTxtEdit = (EditText) getActivity().findViewById(R.id.name_edit_text);
                        final EditText passwordTxtEdit = (EditText) getActivity().findViewById(R.id.password_edit_text);

                        charTxtEdit.getText().clear();
                        nameTxtEdit.getText().clear();
                        passwordTxtEdit.getText().clear();

                        // disable save button if all input was clear. Also need to set the passwordsMatch boolean back to false
                        saveButton.setEnabled(false);
                    }
                });

        final EditText passwordTxtEdit = (EditText) view.findViewById(R.id.password_edit_text);

        passwordTxtEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String password = passwordTxtEdit.getText().toString();

                // only show dialog if the user has typed in a password
                if (!hasFocus && password.length() != 0) {
                    FragmentManager manager = getFragmentManager();
                    ConfirmPasswordDialog myDialog = ConfirmPasswordDialog.newInstance(password);
                    myDialog.show(manager, "ConfirmPasswordDialog");
                    Log.d("MAIN", "focus lost in edittext");
                }
            }
        });

        setClearButtonVisibility(view);

        return view;
    }

    private void setClearButtonVisibility(View view) {

        //TODO: We should clean this up if we can. I didn't know how to create a TextWatcher for multiple EditTexts so I just left it like this for now.

        final EditText charTxtEdit = (EditText) view.findViewById(R.id.character_edit_text);
        final EditText nameTxtEdit = (EditText) view.findViewById(R.id.name_edit_text);
        final EditText passwordTxtEdit = (EditText) view.findViewById(R.id.password_edit_text);
        final Button clearButton = view.findViewById(R.id.clear_button);

        charTxtEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charTxtEdit.getText().length() == 0 && nameTxtEdit.getText().length() == 0 && passwordTxtEdit.getText().length() == 0)
                    clearButton.setVisibility(View.INVISIBLE);
                else
                    clearButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        nameTxtEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charTxtEdit.getText().length() == 0 && nameTxtEdit.getText().length() == 0 && passwordTxtEdit.getText().length() == 0)
                    clearButton.setVisibility(View.INVISIBLE);
                else
                    clearButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordTxtEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charTxtEdit.getText().length() == 0 && nameTxtEdit.getText().length() == 0 && passwordTxtEdit.getText().length() == 0)
                    clearButton.setVisibility(View.INVISIBLE);
                else
                    clearButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            public void setSaveButtonStatus(String matchStatus) {
                if (matchStatus == "true")
                    clearButton.setVisibility(View.VISIBLE);
                else
                    clearButton.setVisibility(View.INVISIBLE);
            }
        });
    }
}
