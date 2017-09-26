package com.example.anja.lab1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        return view;
    }
}
