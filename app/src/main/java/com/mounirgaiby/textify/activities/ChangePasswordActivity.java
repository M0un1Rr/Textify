package com.mounirgaiby.textify.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mounirgaiby.textify.databinding.ActivityChangePasswordBinding;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;

public class ChangePasswordActivity extends AppCompatActivity {
private ActivityChangePasswordBinding binding;
private FirebaseFirestore db;
private PreferenceManager preferenceManager;
private FirebaseUser auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    private void setListener(){
     binding.btnSuivant.setOnClickListener(v->validateOldPass());
     binding.btnConfirm.setOnClickListener(v->{
         binding.btnConfirm.setVisibility(View.GONE);
         binding.progressBar2.setVisibility(View.VISIBLE);
         if(validateNewPass()){
             auth.updatePassword(binding.newPass.getText().toString())
                     .addOnSuccessListener(unused -> {
                         showToast("Votre mot de pass est modifie");
                         onBackPressed();
                     })
                     .addOnFailureListener(unused ->{
                        showToast("Veuillez réessayer plus tard");
                         binding.btnConfirm.setVisibility(View.VISIBLE);
                         binding.progressBar2.setVisibility(View.GONE);
                     });
         }else{
             binding.btnConfirm.setVisibility(View.VISIBLE);
             binding.progressBar2.setVisibility(View.GONE);
         }
     });

    }
    private void validateOldPass(){
       binding.txtError1.setVisibility(View.GONE);
       binding.btnSuivant.setVisibility(View.GONE);
       binding.progressBar.setVisibility(View.VISIBLE);
        AuthCredential authCredential = EmailAuthProvider.getCredential(preferenceManager.getString(Constants.KEY_EMAIL),binding.oldPass.getText().toString());
       auth.reauthenticate(authCredential)
               .addOnSuccessListener(unused->{
                       binding.checkOldPassLayout.setVisibility(View.GONE);
                       binding.NewPassLayout.setVisibility(View.VISIBLE);
               })
       .addOnFailureListener(unused -> {
           binding.btnSuivant.setVisibility(View.VISIBLE);
           binding.progressBar.setVisibility(View.GONE);
           binding.txtError1.setVisibility(View.VISIBLE);
           binding.txtError1.setText("Le mot de passe ne correspond pas à votre mot de passe actuel");
       });

    }

    private Boolean validateNewPass(){

        if(binding.newPass.getText().toString().isEmpty()){
            showToast("Entrez votre nouveau mot de pass");
            return false;
        }
        if(binding.newPass.getText().toString().length() < 6){
            showToast("Le mot de pass doit etre superieure a 6 lettres ou nombre");
            return false;
        }
        if(!binding.confirmNewPass.getText().toString().equals(binding.newPass.getText().toString())){
            showToast("Les mot de pass sont different");
            return false;
        }
        if(binding.confirmNewPass.getText().toString()==binding.newPass.getText().toString() && binding.confirmNewPass.getText().toString()==binding.oldPass.getText().toString()){
            showToast("Votre nouveau mot de passe doit être différent de votre ancien mot de passe");
            return false;
        }
        return true;


    }

    private void init(){
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        auth = FirebaseAuth.getInstance().getCurrentUser();
    }




    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }


}