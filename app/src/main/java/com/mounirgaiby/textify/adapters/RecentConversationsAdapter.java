package com.mounirgaiby.textify.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mounirgaiby.textify.databinding.ItemContainerRecentConverationBinding;
import com.mounirgaiby.textify.listeners.ConvoListener;
import com.mounirgaiby.textify.listeners.UserListener;
import com.mounirgaiby.textify.models.ChatMessage;
import com.mounirgaiby.textify.models.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecentConversationsAdapter extends  RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder> {

    private final List<ChatMessage> chatMessages;
    public final ConvoListener convoListener;

    public RecentConversationsAdapter (List<ChatMessage> chatMessages, ConvoListener convoListener){
        this.chatMessages = chatMessages;
        this.convoListener = convoListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConverationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConverationBinding binding;
        ConversationViewHolder(ItemContainerRecentConverationBinding itemContainerRecentConverationBinding){
            super(itemContainerRecentConverationBinding.getRoot());
            binding = itemContainerRecentConverationBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getCoversationImage(chatMessage.conversationImage));
            binding.textName.setText(chatMessage.conversationName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.txtTime.setText(formatDate(chatMessage.dateObject));
            binding.getRoot().setOnClickListener(v -> {
                user user = new user();
                user.id = chatMessage.conversationId;
                user.name= chatMessage.conversationName;
                user.image = chatMessage.conversationImage;
                convoListener.onConvoClicked(user);
            } );
        }
    }
    private String formatDate(Date date){
        DateFormat format = new SimpleDateFormat("hh:mm a");
        return format.format(date);
    }
    private Bitmap getCoversationImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

}
