package com.mounirgaiby.textify.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mounirgaiby.textify.R;
import com.mounirgaiby.textify.databinding.ActivitySignupBinding;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.textSignin.setOnClickListener(v -> onBackPressed());
        binding.buttonSignup.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        String email = binding.inputEmail.getText().toString().trim();
        String pass = binding.inputPass.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseUser FBuser = FirebaseAuth.getInstance().getCurrentUser() ;
                            HashMap<String,Object> user = new HashMap<>();
                            user.put(Constants.User_UID,FBuser.getUid());
                            user.put(Constants.KEY_NAME,binding.inputName.getText().toString().toLowerCase());
                            user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString().toLowerCase());
                            user.put(Constants.KEY_IMAGE,encodedImage);
                            db.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(FBuser.getUid())
                                    .set(user)

                                    .addOnSuccessListener(documentReference -> {
                                        loading(false);

                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                        preferenceManager.putString(Constants.User_UID,FBuser.getUid());
                                        preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString().toUpperCase());
                                        preferenceManager.putString(Constants.KEY_EMAIL,binding.inputEmail.getText().toString().toLowerCase());
                                        preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);


                                    })
                                    .addOnFailureListener(exception -> {
                                        loading(false);
                                        showToast(exception.getMessage());

                                    });
                        }else{
                            loading(false);
                            showToast(getString(R.string.val_email_already_used));

                        }
                    }
                });

    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream is = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }catch(FileNotFoundException ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
    );


    private boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast(getString(R.string.val_image));
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.val_name_empty));
            binding.inputName.requestFocus();
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.val_email_empty));
            binding.inputEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString().trim()).matches()) {
            showToast(getString(R.string.val_email_pattern));
            return false;
        } else if (binding.inputPass.getText().toString().isEmpty()) {
            showToast(getString(R.string.val_pass_empty));
            binding.inputPass.requestFocus();
            return false;
        } else if (binding.inputPass.getText().toString().length()<6) {
            showToast(getString(R.string.val_pass_length));
            binding.inputPass.requestFocus();
            return false;
        } else if (binding.inputPassConfirm.getText().toString().isEmpty()) {
            showToast(getString(R.string.val_cpass_empty));
            return false;
        } else if (!binding.inputPass.getText().toString().equals(binding.inputPassConfirm.getText().toString())) {
            showToast(getString(R.string.val_unmatching_pass));
            binding.inputPass.setText("");
            binding.inputPass.requestFocus();
            return false;
        }
        return true;
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignup.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);

        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignup.setVisibility(View.VISIBLE);
        }

    }
}

