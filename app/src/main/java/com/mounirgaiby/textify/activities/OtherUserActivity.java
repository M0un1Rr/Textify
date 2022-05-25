package com.mounirgaiby.textify.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mounirgaiby.textify.databinding.ActivityOtherUserBinding;
import com.mounirgaiby.textify.models.user;
import com.mounirgaiby.textify.utilities.Constants;

import java.util.HashMap;
import java.util.Locale;

public class OtherUserActivity extends AppCompatActivity {
private ActivityOtherUserBinding binding;
private user receiverUser;
private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fillInfo();
        setListener();

    }


    private void setListener(){
        binding.back.setOnClickListener(v -> onBackPressed());
    }

    private void fillInfo(){
        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra(Constants.KEY_USER);
        db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.User_UID,hashMap.get("id"))
                .get()
                 .addOnSuccessListener(task -> {
                 binding.txtEmail.setText(task.getDocuments().get(0).getString(Constants.KEY_EMAIL));
                 });
        byte[] bytes = Base64.decode(hashMap.get("image"),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.txtName.setText(hashMap.get("nom"));


    }
    private void showToast(String m){
        Toast.makeText(getApplicationContext(),m,Toast.LENGTH_SHORT).show();

    }
}