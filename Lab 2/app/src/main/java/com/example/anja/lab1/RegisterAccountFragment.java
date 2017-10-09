package com.example.anja.lab1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.soundcloud.android.crop.Crop;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Anja on 9/24/2017.
 * Edited by Jenny 10/02/2017.
 * Edited by Anja 10/05/2017.
 */

public class RegisterAccountFragment extends Fragment {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String URI_STATE_KEY = "saved_uri";
    private static final String PWD_STATE_KEY = "dialog_password";
    int requestCode = 123;
    private boolean passwordsMatch;
    private boolean inputValid;
    private String dialogPassword, fragmentPassword;
    private ImageView profilePhoto, check;
    private EditText charTxtEdit, nameTxtEdit, pwdTxtEdit;
    private Button clearButton, saveButton, profileButton, haveAccountButton;
    private Uri imageUri, croppedUri;
    private boolean isTakenFromCamera;
    private Fragment fragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.register_account_fragment, container,false);

        profilePhoto = view.findViewById(R.id.imageProfile);
        charTxtEdit = view.findViewById(R.id.character_edit_text);
        nameTxtEdit = view.findViewById(R.id.name_edit_text);
        pwdTxtEdit = view.findViewById(R.id.password_edit_text);
        clearButton = view.findViewById(R.id.clear_button);
        saveButton = view.findViewById(R.id.save_button);
        profileButton = view.findViewById(R.id.profile_button);
        haveAccountButton = view.findViewById(R.id.have_account_button);
        fragment = this;
        check = view.findViewById(R.id.nameCheck);


        clearButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        charTxtEdit.getText().clear();
                        nameTxtEdit.getText().clear();
                        pwdTxtEdit.getText().clear();
                        profilePhoto.setImageResource(R.drawable.shiba);

                        // disable buttons and reset variables
                        saveButton.setEnabled(false);
                        passwordsMatch = false;
                        clearButton.setVisibility(View.INVISIBLE);
                        croppedUri = null;
                    }
                });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onProfileClicked(); }
        });

        haveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onHaveAccountClicked(); }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onSaveClicked(); }
        });

        pwdTxtEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String password = pwdTxtEdit.getText().toString();

                // only show dialog if the user has typed in a password
                if (!hasFocus && password.length() != 0) {
                    FragmentManager manager = getFragmentManager();
                    ConfirmPasswordDialog myDialog = ConfirmPasswordDialog.newInstance(password);
                    myDialog.setTargetFragment(fragment, requestCode);
                    myDialog.show(manager, "ConfirmPasswordDialog");
                }
            }
        });

        charTxtEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            //String username =

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        loadProfile();
        loadPreferences();
        setSaveButtonEnabled();
        setClearButtonVisibility();

        return view;
    }

    private void onHaveAccountClicked() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        getActivity().startActivity(intent);
    }

    // onProfileClicked: send image capture intent to the camera
    // NOTE: some code has been taken from the camera example discussed in class.
    public void onProfileClicked() {
        Log.d("STATE", "onProfileClicked");
        // TODO: implement front-facing camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Construct temporary image path and name to save the taken photo
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        imageUri = getActivity().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        takePictureIntent.putExtra("return-data", true);

        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // onSaveClicked: save data to sharedPreferences and close application
    public void onSaveClicked() {
        Log.d("STATE", "onSaveClicked");
        savePreferences();
        saveProfile();
        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.profileSaveText),
                Toast.LENGTH_SHORT).show();

        // Supposed to automatically log the user in and take them to the main activity.
        // TODO: Fix - Clicking the back button from here closes the app.

        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(intent);
        Log.d("ACTIVITY", getActivity().toString());
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // for handing the password check
        String dialogPassword = ConfirmPasswordDialog.dialogPassword;
        fragmentPassword = pwdTxtEdit.getText().toString();
        if (requestCode == ConfirmPasswordDialog.requestCode
                && fragmentPassword.equals(dialogPassword)) {
            Toast.makeText(getActivity(), R.string.password_status, Toast.LENGTH_SHORT).show();
            this.dialogPassword = dialogPassword;
            passwordsMatch = true;
            setSaveButtonEnabled();
        }
        else if (resultCode != RESULT_OK)
            return;

        /**
         * Crop code taken from camera example by Varun
         * deleting temporary image was taken out because the file was needed for saving the state.
         */
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                // Send image taken from camera for cropping
                beginCrop(imageUri);
                break;

            case Crop.REQUEST_CROP: //We changed the RequestCode to the one being used by the library.
                // Update image view after image crop
                handleCrop(resultCode, data);

                // Delete temporary image taken by camera after crop.
                if (isTakenFromCamera) {
                    File f = new File(imageUri.getPath());
                    if (f.exists())
                        f.delete();
                }

                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("STATE", "onSaveState");

        if (croppedUri != null) {
            outState.putParcelable(URI_STATE_KEY, croppedUri);
        }
        if (dialogPassword != null) {
            outState.putString(PWD_STATE_KEY, dialogPassword);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("STATE", "onRestoreState");
        if(savedInstanceState != null) {
            croppedUri = savedInstanceState.getParcelable(URI_STATE_KEY);
            dialogPassword = savedInstanceState.getString(PWD_STATE_KEY);
            if (croppedUri != null) {
                profilePhoto.setImageURI(croppedUri);
            }
        }
        else {
            Log.d("EXCEPTION", "no saved instance");
        }
    }

    // **---------- private helper functions ----------**

    private void setSaveButtonEnabled() {
        if (passwordsMatch && inputValid ) {
            saveButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
        }
    }

    private void setClearButtonVisibility() {
        charTxtEdit.addTextChangedListener(new TextWatcher() {

            private Timer timer = new Timer();
            private final long DELAY = 500;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInput();

                if (charTxtEdit.getText().length() == 0) {
                    inputValid = false;
                }

                setSaveButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String username = charTxtEdit.getText().toString();

                if (username.length() == 0) {
                    check.setVisibility(View.INVISIBLE);
                }
                // Got timer stuff from:
                // https://stackoverflow.com/questions/12142021/how-can-i-do-something-0-5-second-after-text-changed-in-my-edittext

                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                if(username.length() > 0) {
                                    // only check if available if the user typed something
                                    checkUsernameAvailability(username);
                                }
                            }
                        },
                        DELAY
                );
            }
        });


        nameTxtEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInput();

                if (nameTxtEdit.getText().length() == 0) {
                    inputValid = false;
                }

                setSaveButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        pwdTxtEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInput();

                fragmentPassword = pwdTxtEdit.getText().toString();


                if (pwdTxtEdit.getText().length() == 0) {
                    inputValid = false;
                }

                if (dialogPassword.equals(fragmentPassword)) {
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

    private void checkUsernameAvailability(String username) {
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        String url ="http://cs65.cs.dartmouth.edu/nametest.pl?name=";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url + username, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            setUsernameStatus(response);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //TODO: what's the proper way to handle this?
                        }
                    });
            queue.add(jsObjRequest);
    }

    private void setUsernameStatus(JSONObject response) {

        try {
            if (response != null) {
                String avail = response.getString("avail");

                check.setVisibility(View.VISIBLE);

                if (avail.equals("true"))
                    check.setImageResource(R.drawable.checkmark);
                else
                    check.setImageResource(R.drawable.cross);
            }
        }

        catch (JSONException e){
            Toast.makeText(getActivity().getApplicationContext(),
                    "Unable to parse response", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkInput() {
        if (charTxtEdit.getText().length() == 0 && nameTxtEdit.getText().length() == 0 && pwdTxtEdit.getText().length() == 0) {
            clearButton.setVisibility(View.INVISIBLE);
            inputValid = false;
        }
        else {
            inputValid = true;
            clearButton.setVisibility(View.VISIBLE);
        }
    }

    private void loadProfile() {
        try {
            FileInputStream inputImage = getContext().openFileInput(getString(R.string.profileFileName));
            Bitmap profile = BitmapFactory.decodeStream(inputImage);
            profilePhoto.setImageBitmap(profile);
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
        Bitmap profile = profilePhoto.getDrawingCache();
        try {
            FileOutputStream fos = getContext().openFileOutput(
                    getString(R.string.profileFileName), MODE_PRIVATE);
            profile.compress(Bitmap.CompressFormat.PNG, 100, fos);
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
        inputValid = sp.getBoolean("input valid", false);
        passwordsMatch = sp.getBoolean("passwords match", false);
        dialogPassword = sp.getString("dialog password", "");

        if (sp != null) {
            clearButton.setVisibility(View.VISIBLE);
        }
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
        editor.putString("dialog password", dialogPassword);

        editor.commit();
    }

    /** Method to start Crop activity using the library
     * (code taken from camera example discussed by Varun in class)
     *  **/
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getActivity().getApplicationContext(),this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            profilePhoto.setImageURI(Crop.getOutput(result));
            clearButton.setVisibility(View.VISIBLE);
            isTakenFromCamera = true;

            // save uri for bundle
            Uri targetUri = Crop.getOutput(result);
            croppedUri = targetUri;

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity().getApplicationContext(),
                    Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
