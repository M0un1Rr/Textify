package com.mounirgaiby.textify.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mounirgaiby.textify.adapters.UsersAdapter;
import com.mounirgaiby.textify.models.user;

import android.os.Bundle;
import android.service.autofill.AutofillService;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mounirgaiby.textify.R;
import com.mounirgaiby.textify.databinding.ActivityUsersBinding;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
private ActivityUsersBinding binding;
private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();


    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
          loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->{
                   loading(false);
                   String currentUID = preferenceManager.getString(Constants.User_UID);
                   if(task.isSuccessful() && task.getResult() != null){
                       List<user> users = new ArrayList<>();
                       for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                           if(currentUID.equals(queryDocumentSnapshot.getId())){
                               continue;
                           }
                           user user = new user();
                           user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                           user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                           user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                           user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                           users.add(user);
                       }
                       if(users.size()>0){
                           UsersAdapter usersAdapter = new UsersAdapter(users);
                           binding.usersRecyclerView.setAdapter(usersAdapter);
                           binding.usersRecyclerView.setVisibility(View.VISIBLE);
                       }else{
                           showErrorMessage();
                       }
                   }
                });


    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","Aucun utilisateur est disponible"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }


    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);

        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    }
}