package com.example.anja.lab1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Anja on 9/24/2017.
 * Edited by Jenny 9/27/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;

    private static final String IMAGE_UNSPECIFIED = "image/*";
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    public static String INTERNAL_FILE = "internal-file";
    private static final String TAG = "Settings";

    private ConfirmPasswordDialog dialog;
    private ImageView profilePhoto;
    private EditText charTxtEdit;
    private EditText nameTxtEdit;
    private EditText pwdTxtEdit;
    Bitmap bitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container,false);
        ImageView check = view.findViewById(R.id.nameCheck);
        check.setImageResource(R.drawable.checkmark);

        final Button clearButton = (Button) view.findViewById(R.id.clear_button);
        final Button profileButton = view.findViewById(R.id.profile_button);
        final Button saveButton = view.findViewById(R.id.save_button);

        profilePhoto = view.findViewById(R.id.imageProfile);
        charTxtEdit = view.findViewById(R.id.character_edit_text);
        nameTxtEdit = view.findViewById(R.id.name_edit_text);
        pwdTxtEdit = view.findViewById(R.id.password_edit_text);


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

        profileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onProfileClicked(v);
                }
            });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked(v);
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

        loadProfile();
        loadPreferences();
        setClearButtonVisibility(view);

        return view;
    }

    public void onProfileClicked(View v) {
        Log.d("MAIN", "moved to onProfileClicked");
        // TODO: implement front-facing camera

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

//        // Construct temporary image path and name to save the take photo
//        ContentValues values = new ContentValues(1);
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
//        mImageCaptureUri = getActivity().getContentResolver().insert(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
//        takePictureIntent.putExtra("return-data", true);

//        try {
//            // Start a camera capturing activity
//            // REQUEST_CODE_TAKE_FROM_CAMERA is an integer tag you
//            // defined to identify the activity in onActivityResult()
//            // when it returns
//            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_FROM_CAMERA);
//        } catch (ActivityNotFoundException e) {
//            e.printStackTrace();
//        }
//        isTakenFromCamera = true;

        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            profilePhoto.setImageBitmap(bitmap);
        }
    }


    public void onSaveClicked(View v) {
        Log.d("MAIN", "moved to onSaveClicked");
        savePreferences();
        saveProfile();

        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.profileSaveText),
                Toast.LENGTH_SHORT).show();

        // Close the activity
        getActivity().finish();
    }

    // **---------- private helper functions ----------**

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

    private void loadProfile() {
        try {
            FileInputStream inputImage = getContext().openFileInput(getString(R.string.profileFileName));
            Bitmap bmap = BitmapFactory.decodeStream(inputImage);
            profilePhoto.setImageBitmap(bmap);
            inputImage.close();
        }
        // get default profile photo if photo file not found
        catch (IOException e) {
            profilePhoto.setImageResource(R.drawable.shiba);
        }
    }

    private void saveProfile() {
        // Save profile image into internal storage.
        profilePhoto.buildDrawingCache();
        Bitmap bmap = profilePhoto.getDrawingCache();
        try {
            FileOutputStream fos = getContext().openFileOutput(
                    getString(R.string.profileFileName), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void loadPreferences() {
        Context context = getActivity();
        SharedPreferences sp = context.getSharedPreferences(
                getString(R.string.saved_info), Context.MODE_PRIVATE);
        Log.d("CHARNAME", sp.getString("character name", ""));
        Log.d("FULLNAME", sp.getString("full name", ""));
        Log.d("PASSWORD", sp.getString("password", ""));
//
        charTxtEdit.setText(sp.getString("character name", ""));
        nameTxtEdit.setText(sp.getString("full name", ""));
        pwdTxtEdit.setText(sp.getString("password", ""));

    }

    private void savePreferences() {
        Context context = getActivity();
        SharedPreferences sp = context.getSharedPreferences(
                getString(R.string.saved_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("character name", charTxtEdit.getText().toString());
        editor.putString("full name", nameTxtEdit.getText().toString());
        editor.putString("password", pwdTxtEdit.getText().toString());
        editor.commit();
    }

}
