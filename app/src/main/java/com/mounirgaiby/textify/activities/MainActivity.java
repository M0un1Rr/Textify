package com.mounirgaiby.textify.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mounirgaiby.textify.adapters.RecentConversationsAdapter;
import com.mounirgaiby.textify.databases.DatabaseHelper;
import com.mounirgaiby.textify.databinding.ActivityMainBinding;
import com.mounirgaiby.textify.listeners.ConvoListener;
import com.mounirgaiby.textify.models.ChatMessage;
import com.mounirgaiby.textify.models.user;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;

import org.w3c.dom.Document;

import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConvoListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth auth;
    private List<ChatMessage> recentConvos;
    private RecentConversationsAdapter recentConversationsAdapter;
    private FirebaseFirestore DB;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        db = new DatabaseHelper(this);
        fillInfo();
        init();
        getToken();
        setListeners();
        settingsCheck();
        listenRecentConvos();
    }

    private void setListeners() {
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
            startActivity(intent);
        });
        binding.fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(intent);
        });


    }

    private void init() {
        recentConvos = new ArrayList<>();
        recentConversationsAdapter = new RecentConversationsAdapter(recentConvos,this);
        binding.usersRecyclerView.setAdapter(recentConversationsAdapter);
        DB = FirebaseFirestore.getInstance();
    }

    private void fillInfo() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    public void updateToken(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getUid())
                .update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> {
                })
                .addOnFailureListener(e -> {
                    showToast("Echec de la mis a jour de Token");
                });

    }
    private void listenRecentConvos(){
        DB.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.User_UID))
                .addSnapshotListener(eventListener);
        DB.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.User_UID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if(preferenceManager.getString(Constants.User_UID).equals(senderId)){
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                    }else{

                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    recentConvos.add(chatMessage);



                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i< recentConvos.size();i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(recentConvos.get(i).message.equals(senderId) && recentConvos.get(i).receiverId.equals(receiverId)){
                            recentConvos.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            recentConvos.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }

            }
            Collections.sort(recentConvos,(obj1,obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConversationsAdapter.notifyDataSetChanged();
            binding.usersRecyclerView.smoothScrollToPosition(0);
            binding.usersRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);


        }


    };





    public void settingsCheck() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.insertDataSettings(user.getUid(), 1);

    }




    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConvoClicked(user user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}