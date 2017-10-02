package com.example.anja.lab1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.soundcloud.android.crop.Crop;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Anja on 9/24/2017.
 * Edited by Jenny 10/01/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;

    private static final String IMAGE_UNSPECIFIED = "image/*";
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    public static String INTERNAL_FILE = "internal-file";
    int requestCode = 123;
    private boolean passwordsMatch;
    private boolean inputValid;
    private String dialogPassword;
    private String fragmentPassword;


    private ConfirmPasswordDialog dialog;
    private ImageView profilePhoto;
    private EditText charTxtEdit;
    private EditText nameTxtEdit;
    private EditText pwdTxtEdit;
    private Button clearButton;
    private Button saveButton;
    private Bitmap bitmap;
    private Uri mUri;
    private boolean isTakenFromCamera;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_fragment, container,false);
        ImageView check = view.findViewById(R.id.nameCheck);
        check.setImageResource(R.drawable.checkmark);

        final Button profileButton = view.findViewById(R.id.profile_button);

        profilePhoto = view.findViewById(R.id.imageProfile);
        charTxtEdit = view.findViewById(R.id.character_edit_text);
        nameTxtEdit = view.findViewById(R.id.name_edit_text);
        pwdTxtEdit = view.findViewById(R.id.password_edit_text);
        clearButton = view.findViewById(R.id.clear_button);
        saveButton = view.findViewById(R.id.save_button);

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
                        profilePhoto.setImageResource(R.drawable.shiba);

                        // disable save button if all input was clear set the passwordsMatch boolean back to false
                        saveButton.setEnabled(false);
                        passwordsMatch = false;
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
        final Fragment fragment = this;

        passwordTxtEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String password = passwordTxtEdit.getText().toString();

                // only show dialog if the user has typed in a password
                if (!hasFocus && password.length() != 0) {
                    FragmentManager manager = getFragmentManager();
                    ConfirmPasswordDialog myDialog = ConfirmPasswordDialog.newInstance(password);
                    myDialog.setTargetFragment(fragment, requestCode);
                    myDialog.show(manager, "ConfirmPasswordDialog");
                }
            }
        });

        loadProfile();
        loadPreferences();
        setClearButtonVisibility(view);
        setSaveButtonEnabled();

        return view;
    }

    public void onProfileClicked(View v) {
        Log.d("STATE", "onProfileClicked");
        // TODO: implement front-facing camera

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Construct temporary image path and name to save the taken photo
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        mUri = getActivity().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        takePictureIntent.putExtra("return-data", true);

        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            isTakenFromCamera = true;
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String dialogPassword = ConfirmPasswordDialog.dialogPassword;

        if (requestCode == ConfirmPasswordDialog.requestCode && fragmentPassword.equals(dialogPassword)) {
            Toast.makeText(getActivity(), "Passwords Match", Toast.LENGTH_SHORT).show();
            this.dialogPassword = dialogPassword;
            passwordsMatch = true;
            setSaveButtonEnabled();
        }

//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
////            Crop.of(source, destination).asSquare().start(getActivity().getApplicationContext(),this);
////            Bundle extras = data.getExtras();
////            bitmap = (Bitmap) extras.get("data");
//            Uri targetUri = data.getData();
//            mUri = targetUri;
//            Log.d("URI_Create", mUri.toString());
//            profilePhoto.setImageBitmap(bitmap);
//        }
        else if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                // Send image taken from camera for cropping
                beginCrop(mUri);
                break;

            case Crop.REQUEST_CROP: //We changed the RequestCode to the one being used by the library.
                // Update image view after image crop
                handleCrop(resultCode, data);

                // Delete temporary image taken by camera after crop.
                if (isTakenFromCamera) {
                    File f = new File(mUri.getPath());
                    if (f.exists())
                        f.delete();
                }

                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the bitmap
        Log.d("STATE", "onSaveState");
        outState.putParcelable("IMG", bitmap);
        if (mUri != null) {
            outState.putParcelable("uri", mUri);
            Log.d("URI_Save", mUri.toString());
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("STATE", "onRestoreState");
        if(savedInstanceState != null) {
            if (mUri != null) {
                mUri = savedInstanceState.getParcelable("uri");
                profilePhoto.setImageURI(mUri);
            }
        }
        else {
            Log.d("EXCEPTION", "no saved instance");
        }
    }

    public void onSaveClicked(View v) {
        Log.d("STATE", "onSaveClicked");
        savePreferences();
        saveProfile();

        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.profileSaveText),
                Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }

    // **---------- private helper functions ----------**

    private void setSaveButtonEnabled() {
        if (passwordsMatch && inputValid) {
            saveButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
        }
    }

    private void setClearButtonVisibility(final View view) {

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
                if (charTxtEdit.getText().length() == 0 && nameTxtEdit.getText().length()
                        == 0 && passwordTxtEdit.getText().length() == 0) {
                    clearButton.setVisibility(View.INVISIBLE);
                    inputValid = false;
                }
                else {
                    inputValid = true;
                    clearButton.setVisibility(View.VISIBLE);
                }

                if (charTxtEdit.getText().length() == 0) {
                    inputValid = false;
                }

                setSaveButtonEnabled();
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
                if (charTxtEdit.getText().length() == 0 && nameTxtEdit.getText().length() == 0 && passwordTxtEdit.getText().length() == 0) {
                    clearButton.setVisibility(View.INVISIBLE);
                    inputValid = false;
                }
                else {
                    inputValid = true;
                    clearButton.setVisibility(View.VISIBLE);
                }

                if (nameTxtEdit.getText().length() == 0) {
                    inputValid = false;
                }

                setSaveButtonEnabled();
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
                fragmentPassword = passwordTxtEdit.getText().toString();

                if (charTxtEdit.getText().length() == 0 && nameTxtEdit.getText().length() == 0 && passwordTxtEdit.getText().length() == 0) {
                    inputValid = false;
                    clearButton.setVisibility(View.INVISIBLE);
                }
                else {
                    inputValid = true;
                    clearButton.setVisibility(View.VISIBLE);
                }

                if (passwordTxtEdit.getText().length() == 0) {
                    inputValid = false;
                }

                if (dialogPassword == fragmentPassword) {
                    passwordsMatch = true;
                }

                else {
                    passwordsMatch = false;
                }

                setSaveButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable editable) {}

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
        SharedPreferences sp = getActivity().getSharedPreferences(
                getString(R.string.saved_info), Context.MODE_PRIVATE);
        charTxtEdit.setText(sp.getString("character name", ""));
        nameTxtEdit.setText(sp.getString("full name", ""));
        pwdTxtEdit.setText(sp.getString("password", ""));
        inputValid = sp.getBoolean("input valid", true);
        passwordsMatch = sp.getBoolean("passwords match", true);
        if (sp != null) {
            clearButton.setVisibility(View.VISIBLE);
        }
        setSaveButtonEnabled();
    }

    private void savePreferences() {
        SharedPreferences sp = getActivity().getSharedPreferences(
                getString(R.string.saved_info), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("character name", charTxtEdit.getText().toString());
        editor.putString("full name", nameTxtEdit.getText().toString());
        editor.putString("password", pwdTxtEdit.getText().toString());
        editor.putBoolean("input valid", inputValid);
        editor.putBoolean("passwords match", passwordsMatch);

        editor.commit();
    }

    /** Method to start Crop activity using the library
     *	Earlier the code used to start a new intent to crop the image,
     *	but here the library is handling the creation of an Intent, so you don't
     * have to.
     *  **/
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getActivity().getApplicationContext(),this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            profilePhoto.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity().getApplicationContext(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
