package com.mounirgaiby.textify.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mounirgaiby.textify.R;
import com.mounirgaiby.textify.databinding.ActivitySigninBinding;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;

import java.util.HashMap;

public class SigninActivity extends AppCompatActivity {
private ActivitySigninBinding binding;
private FirebaseAuth Auth;
private PreferenceManager preferenceManager;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        checkIfUserIsConnected();
        Auth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());

        setListeners();
    }
    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
              startActivity(new Intent(getApplicationContext(), SignupActivity.class)));

        binding.buttonSignin.setOnClickListener(v -> {
            if(isValidSignInDetails()){
                SignIn();
            }
        });

    }

    private void SignIn(){
        loading(true);
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPass.getText().toString().trim();
        Auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.User_UID,user.getUid())
                        .get()
                        .addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful() && task1.getResult() != null && task1.getResult().getDocuments().size()>0){
                                    DocumentSnapshot ds = task1.getResult().getDocuments().get(0);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                    preferenceManager.putString(Constants.User_UID,user.getUid());
                                    preferenceManager.putString(Constants.KEY_NAME,ds.getString(Constants.KEY_NAME));
                                    preferenceManager.putString(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
                                    preferenceManager.putString(Constants.KEY_IMAGE,ds.getString(Constants.KEY_IMAGE));
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                }else{
                                    loading(false);
                                    Log.w("Sign in","Problem:"+task.getException());
                                }

                        });

            }else{
                loading(false);
                Log.w("Sign in","Problem:"+task.getException());
                showToast("Email ou mot de pass est incorrect");
            }
            }
        });

    }


    private Boolean isValidSignInDetails(){
    if (binding.inputEmail.getText().toString().trim().isEmpty()) {
        showToast(getString(R.string.val_email_empty));
        binding.inputEmail.requestFocus();
        return false;}
    else if (binding.inputPass.getText().toString().isEmpty()) {
        showToast(getString(R.string.val_pass_empty));
        binding.inputPass.requestFocus();
        return false;}

    return true;
}

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);

        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignin.setVisibility(View.VISIBLE);
        }

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
}

   private void checkIfUserIsConnected(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
   }}
}