package com.example.tristan.inclass10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by Tristan on 4/9/18.
 */

public class ContactAdapter extends ArrayAdapter<Contact> {

    private StorageReference reference;

    public ContactAdapter(@NonNull Context context, int resource, @NonNull List<Contact> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Contact contact = getItem(position);
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_contact, parent, false);

        TextView nameView = convertView.findViewById(R.id.contactListName);
        TextView emailView = convertView.findViewById(R.id.contactListEmail);
        TextView phoneView = convertView.findViewById(R.id.contactListPhone);
        final ImageView imageView = convertView.findViewById(R.id.contactImage);
        Button deleteButton = convertView.findViewById(R.id.deleteContactButton);
        Button editButton = convertView.findViewById(R.id.editContactButton);

        nameView.setText(contact.name);
        emailView.setText(contact.email);
        phoneView.setText(contact.phone);

        reference = FirebaseStorage.getInstance().getReference(contact.imagePath);

        final long ONE_MEGABYTE = 1024 * 1024;
        reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                imageView.setImageBitmap(bitmap);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() instanceof ContactsActivity) {
                    ((ContactsActivity) getContext()).deleteContact(position);
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() instanceof  ContactsActivity) {
                    ((ContactsActivity) getContext()).editContact(contact.id, contact.name, contact.email, contact.phone, contact.imagePath);
                }
            }
        });



        return convertView;
    }
}
