package com.example.tristan.inclass10;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tristan on 4/9/18.
 */

public class Contact implements Serializable {
    String id, name, phone, email, imagePath;

    public Contact() {

    }

    public Contact(String id, String name, String phone, String email, String imagePath) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.imagePath = imagePath;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("name", name);
        result.put("phone", phone);
        result.put("email", email);
        result.put("imagePath", imagePath);

        return result;
    }
}
