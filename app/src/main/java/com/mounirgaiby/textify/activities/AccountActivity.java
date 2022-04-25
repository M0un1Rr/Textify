
package com.mounirgaiby.textify.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mounirgaiby.textify.R;
import com.mounirgaiby.textify.databinding.ActivityAccountBinding;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import java.util.HashMap;

public class AccountActivity extends AppCompatActivity {
private  ActivityAccountBinding binding;
private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        fillInfo();
        setListener();
    }
    public void setListener(){
        binding.btnSignOut.setOnClickListener(v->{
            signOut();
        });
        binding.back.setOnClickListener(v->onBackPressed());
    }


    private void fillInfo(){
        String name = preferenceManager.getString(Constants.KEY_NAME).substring(0,1).toUpperCase()+preferenceManager.getString(Constants.KEY_NAME).substring(1).toLowerCase();
        String email = preferenceManager.getString(Constants.KEY_EMAIL);
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.txtName.setText(name);
        binding.txtEmail.setText(email.toLowerCase());
    }

    private void signOut(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference dr = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getUid());
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        dr.update(updates)
                .addOnSuccessListener(unused -> {
                    FirebaseAuth.getInstance().signOut();
                    showToast(getString(R.string.Disconnected));
                    Intent intent = new Intent(getApplicationContext(),SigninActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> showToast(getString(R.string.unable_disconnect)));





    }
    public void showToast(String m){
        Toast.makeText(getApplicationContext(),m,Toast.LENGTH_SHORT).show();
    }


}