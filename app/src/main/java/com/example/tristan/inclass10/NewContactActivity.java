package com.example.tristan.inclass10;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
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

import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewContactActivity extends AppCompatActivity {

    private ArrayList<Contact> contacts;
    private EditText firstName, lastName, email, phone;
    private Button addContact, cancelButton;
    private ImageButton contactImageButton;
    private Bitmap userBitmap;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private int currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);

        firstName = (EditText) findViewById(R.id.contactFirstName);
        lastName = (EditText) findViewById(R.id.contactLastName);
        email = (EditText) findViewById(R.id.contactUserName);
        phone = (EditText) findViewById(R.id.contactPhone);
        addContact = (Button) findViewById(R.id.confirmContactButton);
        cancelButton = (Button) findViewById(R.id.cancelContactButton);
        contactImageButton = (ImageButton) findViewById(R.id.contactImageButton);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        getContactView();

        if (getIntent().getExtras().containsKey("NEWCONTACT")) {
            addContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createContact();
                }
            });
        } else if (getIntent().getExtras().containsKey("EDITCONTACT")) {
            //Retrieve Contact Info
            addContact.setText("Save");
            final Contact contact = (Contact) getIntent().getExtras().getSerializable("EDITCONTACT");
            String[] splited = contact.name.split("\\s+");
            firstName.setText(splited[0]);
            lastName.setText(splited[1]);
            email.setText(contact.email);
            phone.setText(contact.phone);
            String id = contact.id;

            //Retrieve Contact photo
            final String path = "contactAvatars/" + user.getUid() + "/id_" + currentId + "/" + user.getUid() + id + ".png";
            StorageReference reference = FirebaseStorage.getInstance().getReference(path);
            final long ONE_MEGABYTE = 1024 * 1024;

            reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    userBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                    contactImageButton.setImageBitmap(userBitmap);
                }
            });


            addContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Contact contact1 = new Contact(contact.id, firstName.getText().toString() + " " + lastName.getText().toString(), phone.getText().toString(), email.getText().toString(), path);
                    editContact(contact1);
                }
            });
        }

        contactImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewContactActivity.this, ContactsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void createContact() {
        if (!validate()) {
            Toast.makeText(this, "Please fill out the form", Toast.LENGTH_SHORT).show();
            return;
        }

        String path = "contactAvatars/" + user.getUid() + "/id_" + currentId + "/" + user.getUid() + "id_" + currentId + ".png";
        Contact contact = new Contact("id_" + currentId, firstName.getText().toString() + " " + lastName.getText().toString(), phone.getText().toString(), email.getText().toString(), path);
        mDatabase.child("contacts").child(user.getUid()).child("id_" + currentId).setValue(contact);
        uploadUserImageToStorage(userBitmap);

        Intent intent = new Intent(NewContactActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    private void editContact(Contact contact) {
        Map<String, Object> postValues = contact.toMap();

        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/contacts/" + user.getUid() + "/" + contact.id, postValues);

        String[] split = contact.id.split("_");
        currentId = Integer.parseInt(split[1]);
        mDatabase.updateChildren(childUpdate);
        uploadUserImageToStorage(userBitmap);

        Intent intent = new Intent(NewContactActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    private void getContactView() {
        contacts = new ArrayList<>();
        mDatabase.child("contacts").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Contact contact = postSnapshot.getValue(Contact.class);
                    contacts.add(contact);
                }
                currentId = contacts.size();
                currentId = updateId(currentId, contacts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int updateId(int id, ArrayList<Contact> contacts) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).id.equals("id_" + id)) {
                id++;
            }
        }

        return id;
    }

    private boolean validate() {
        boolean valid = true;

        String firstNameCheck = firstName.getText().toString();
        String lastNameCheck = lastName.getText().toString();
        String emailCheck = email.getText().toString();
        String phoneCheck = phone.getText().toString();

        if (TextUtils.isEmpty(firstNameCheck) || TextUtils.isEmpty(lastNameCheck)
                || TextUtils.isEmpty(emailCheck) || TextUtils.isEmpty(phoneCheck) || userBitmap == null) {
            valid = false;
        }


        return valid;
    }

    private void pickImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewContactActivity.this)
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
                    contactImageButton.setImageBitmap(userBitmap);
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri= data.getData();
                    try {
                        userBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        contactImageButton.setImageBitmap(userBitmap);
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
        String path = "contactAvatars/" + user.getUid() + "/id_" + currentId + "/" + user.getUid() + "id_" + currentId + ".png";
        byte[] bytes = baos.toByteArray();
        StorageReference reference = FirebaseStorage.getInstance().getReference(path);

        UploadTask uploadTask = reference.putBytes(bytes);

        uploadTask.addOnSuccessListener(NewContactActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("demo", "Contact Image Successfully Uploaded");
            }
        });
    }
}
