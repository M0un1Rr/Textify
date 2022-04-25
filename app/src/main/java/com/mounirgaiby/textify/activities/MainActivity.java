package com.mounirgaiby.textify.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mounirgaiby.textify.databinding.ActivityMainBinding;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;

import org.w3c.dom.Document;

import android.util.Base64;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
private ActivityMainBinding binding;
private PreferenceManager preferenceManager;
private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        fillInfo();
        getToken();
        setListeners();
    }

    private void setListeners() {
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),AccountActivity.class);
            startActivity(intent);
        });
        binding.fabNewChat.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),UsersActivity.class);
            startActivity(intent);
        });


    }

    private void fillInfo(){
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    public void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    public void updateToken(String token){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getUid())
        .update(Constants.KEY_FCM_TOKEN,token)
        .addOnSuccessListener(unused -> {
        })
        .addOnFailureListener(e -> {
            showToast("Echec de la mis a jour de Token");
        });

    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
}