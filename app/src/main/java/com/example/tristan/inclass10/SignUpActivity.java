package com.example.tristan.inclass10;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText firstName, lastName, userName, password, repeatPassword;
    private Button signUpButton, cancelButton;
    private ImageButton userImage;
    private Bitmap userBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstName = (EditText) findViewById(R.id.signUpFirstName);
        lastName = (EditText) findViewById(R.id.signUpLastName);
        userName = (EditText) findViewById(R.id.signUpUserName);
        password = (EditText) findViewById(R.id.signUpPassword);
        repeatPassword = (EditText) findViewById(R.id.signUpRepeatPassword);
        signUpButton = (Button) findViewById(R.id.confirmSignUpButton);
        cancelButton = (Button) findViewById(R.id.cancelSignUpButton);
        userImage = (ImageButton) findViewById(R.id.imageButton);

        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp(userName.getText().toString(), password.getText().toString());
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    private void signUp(String email, String password) {
        if (!validate()) {
            Toast.makeText(this, "Fill Out Form", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("demo", "createUserWithEmailAndPassword:Success");
                            createUserSettings();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                            Log.d("demo", "Exception: " + task.getException());
                        }
                    }
                });
    }

    private void createUserSettings() {
        user = mAuth.getCurrentUser();
        uploadUserImageToStorage(userBitmap);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(firstName.getText().toString() + " " + lastName.getText().toString())
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("demo", "User Profile Successfully Created.");
                logIn();
            }
        });

    }

    private void logIn() {
        Intent intent = new Intent(SignUpActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    private void pickImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this)
                .setMessage("Pick picture from gallery or take photo?")
                .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 1);
                    }
                }).setPositiveButton("Take Photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePhoto, 0);
                    }
                });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    userBitmap = (Bitmap) data.getExtras().get("data");
                    userImage.setImageBitmap(userBitmap);
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri= data.getData();
                    try {
                        userBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        userImage.setImageBitmap(userBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void uploadUserImageToStorage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String path = "userAvatars/" + user.getUid() + "/" + UUID.randomUUID() + ".png";
        byte[] bytes = baos.toByteArray();
        StorageReference reference = FirebaseStorage.getInstance().getReference(path);

        UploadTask uploadTask = reference.putBytes(bytes);

        uploadTask.addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("demo", "User Image Successfully Uploaded");
            }
        });
    }

    private boolean validate() {
        boolean valid = true;

        String email = userName.getText().toString();
        String name = firstName.getText().toString() + " " + lastName.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || userBitmap == null) {
            valid = false;
        }

        String firstPassword = password.getText().toString();
        String secondPassword = repeatPassword.getText().toString();

        if (TextUtils.isEmpty(firstPassword) || !firstPassword.equals(secondPassword)) {
            valid = false;
        }

        return valid;
    }

}
