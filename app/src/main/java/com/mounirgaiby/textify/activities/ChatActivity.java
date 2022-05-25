package com.mounirgaiby.textify.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.ConfigurationStats;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.autofill.AutofillService;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mounirgaiby.textify.R;
import com.mounirgaiby.textify.adapters.ChatAdapter;
import com.mounirgaiby.textify.databinding.ActivityChatBinding;
import com.mounirgaiby.textify.models.ChatMessage;
import com.mounirgaiby.textify.models.user;
import com.mounirgaiby.textify.utilities.Constants;
import com.mounirgaiby.textify.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private user receiverUser;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String ConvoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(null);
        init();
        loadReceiverDetails();
        listenMessages();
        setListeners();
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v->onBackPressed());
       binding.sendBtn.setOnClickListener(v -> sendMessage());
    }
    private void init(){
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    preferenceManager = new PreferenceManager(getApplicationContext());
    chatMessages = new ArrayList<>();
    chatAdapter = new ChatAdapter(
            chatMessages,
            preferenceManager.getString(Constants.User_UID)
    );
    binding.chatRV.setAdapter(chatAdapter);


    }

    private void sendMessage(){
        if(binding.chatMessage.getText().length()>0) {
            HashMap<String,Object> sender = new HashMap<>();
        sender.put(Constants.KEY_EMAIL,receiverUser.id);
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,user.getUid());
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.chatMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(ConvoId != null){
            updateConversion(binding.chatMessage.getText().toString());
        }else{
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.User_UID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.chatMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }}
        binding.chatMessage.setText(null);



    }

    private void listenMessages(){
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.User_UID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);

        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.User_UID))
                .addSnapshotListener(eventListener);


    }

    private final EventListener<QuerySnapshot> eventListener = (value,error) -> {
      if(error != null){
          return;
      }
      if(value != null){
          int count = chatMessages.size();
          for(DocumentChange documentChange : value.getDocumentChanges()){
              if(documentChange.getType() == DocumentChange.Type.ADDED){
                  ChatMessage chatMessage = new ChatMessage();
                  chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                  chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                  chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                  chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                  chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                  chatMessages.add(chatMessage);
              }

          }
          Collections.sort(chatMessages,(obj1,obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
          if(count == 0){
              chatAdapter.notifyDataSetChanged();
          }else{
              chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
              binding.chatRV.smoothScrollToPosition(chatMessages.size()-1);

          }
          binding.chatRV.setVisibility(View.VISIBLE);

      }
        binding.progressBar.setVisibility(View.GONE);
        if(ConvoId == null){
            checkForConversion();
        }


    };

    private void loadReceiverDetails(){
        receiverUser = (user) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.friendName.setText(receiverUser.name);
        binding.imageProfile.setImageBitmap(getUserImage(receiverUser.image));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("nom", receiverUser.name);
                hashMap.put("email", receiverUser.email);
                hashMap.put("image", receiverUser.image);
                hashMap.put("id",receiverUser.id);
                Intent intent = new Intent(getApplicationContext(),OtherUserActivity.class);
                intent.putExtra(Constants.KEY_USER,hashMap);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("dd MMMM yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void makeToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void addConversion(HashMap<String,Object> conversion){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> ConvoId = documentReference.getId());

    }

    private void updateConversion(String message){
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(ConvoId);
        documentReference.update(
          Constants.KEY_LAST_MESSAGE,message,
          Constants.KEY_TIMESTAMP,new Date()
        );

    }

    private void checkForConversion(){
        if(chatMessages.size()!=0){
            checkForConversionRemotly(preferenceManager.getString(Constants.User_UID)
            ,receiverUser.id);
            checkForConversionRemotly(receiverUser.id,preferenceManager.getString(Constants.User_UID));
        }



    }

    private void checkForConversionRemotly(String senderId,String receiverId){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    public final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task ->{
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            ConvoId = documentSnapshot.getId();
        }
    };
}