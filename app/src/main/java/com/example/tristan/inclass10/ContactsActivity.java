package com.example.tristan.inclass10;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;

    private ArrayList<Contact> contacts;
    private ListView listView;
    private ContactAdapter adapter;
    private TextView currentUser;
    private Button addContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        currentUser = (TextView) findViewById(R.id.currentUserView);
        addContact = (Button) findViewById(R.id.addContactButton);
        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.currentUserView));

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();
        currentUser.setText(user.getDisplayName());

        getContactView();
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactsActivity.this, NewContactActivity.class);
                intent.putExtra("NEWCONTACT", "NEWCONTACT");
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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

                adapter = new ContactAdapter(ContactsActivity.this, 0, contacts);
                listView.setAdapter(adapter);
                //currentUser.setText("Hello! " + userName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteContact(int position) {
        final int mPosition = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
        builder.setTitle("Delete Contact.");
        builder.setMessage("Are you sure you want to delete this contact?");

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Contact contact = (Contact) listView.getItemAtPosition(mPosition);
                mDatabase.child("contacts").child(user.getUid()).child(contact.id).removeValue();
                String path = "contactAvatars/" + user.getUid() + "/" + contact.id + "/" + user.getUid() + contact.id + ".png";
                StorageReference reference = FirebaseStorage.getInstance().getReference(path);
                reference.delete();
                getContactView();
            }
        });

        builder.show();
    }

    public void editContact(String id, String name, String email, String phone, String path) {
        Contact contact = new Contact(id, name, phone, email, path);
        Intent intent = new Intent(ContactsActivity.this, NewContactActivity.class);
        intent.putExtra("EDITCONTACT", contact);
        startActivity(intent);
    }
}
