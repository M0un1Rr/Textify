package com.mounirgaiby.textify.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mounirgaiby.textify.R;
import com.mounirgaiby.textify.databinding.ActivityForgatPasswordBinding;
import com.mounirgaiby.textify.utilities.Constants;

public class ForgatPasswordActivity extends AppCompatActivity {
private ActivityForgatPasswordBinding binding;
private FirebaseAuth auth;
private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgatPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        setListeners();
    }

    private void setListeners(){
        binding.buttonResetPass.setOnClickListener(v->{
            binding.buttonResetPass.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            auth = FirebaseAuth.getInstance();
            String emailAddress = binding.inputEmail.getText().toString();
            showToast(emailAddress);
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL,emailAddress)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                            auth.setLanguageCode("fr");
                            auth.sendPasswordResetEmail(emailAddress).addOnSuccessListener(unused -> {
                                binding.buttonResetPass.setVisibility(View.VISIBLE);
                                binding.progressBar.setVisibility(View.GONE);
                                binding.txtResult.setText("Email envoyé! vérifier votre boîte de réception");
                                binding.txtResult.setTextColor(getResources().getColor(R.color.primary));
                                binding.txtResult.setVisibility(View.VISIBLE);
                            })
                                    .addOnFailureListener(unused -> {
                                        binding.buttonResetPass.setVisibility(View.VISIBLE);
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.txtResult.setText("Echec! Réessayer plus tard");
                                        binding.txtResult.setTextColor(getResources().getColor(R.color.error));
                                        binding.txtResult.setVisibility(View.VISIBLE);
                                    });
                        }else if(task.isSuccessful() && task.getResult() == null){
                            binding.buttonResetPass.setVisibility(View.VISIBLE);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.txtResult.setText("L'email n'exist pas!");
                            binding.txtResult.setTextColor(getResources().getColor(R.color.error));
                            binding.txtResult.setVisibility(View.VISIBLE);
                        }else{
                            binding.buttonResetPass.setVisibility(View.VISIBLE);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.txtResult.setText("Echec! Réessayer plus tard");
                            binding.txtResult.setTextColor(getResources().getColor(R.color.error));
                            binding.txtResult.setVisibility(View.VISIBLE);
                        }
                    });
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

}