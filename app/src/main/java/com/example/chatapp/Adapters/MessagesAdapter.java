package com.example.chatapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ItemReceiveBinding;
import com.example.chatapp.databinding.ItemSentBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Message> messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom){
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new sentViewHolder(view);
        } else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(Objects.equals(FirebaseAuth.getInstance().getUid(), message.getSenderId())){
            return ITEM_SENT;
        } else{
            return ITEM_RECEIVE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int[] reactions = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if(pos < 0)
                return false;

            if(holder.getClass() == sentViewHolder.class){
                sentViewHolder viewHolder = (sentViewHolder) holder;
                viewHolder.binding.feelingSent.setImageResource(reactions[pos]);
                viewHolder.binding.feelingSent.setVisibility(View.VISIBLE);
            } else{
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.feelingRecieve.setImageResource(reactions[pos]);
                viewHolder.binding.feelingRecieve.setVisibility(View.VISIBLE);
            }

            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass() == sentViewHolder.class){
            sentViewHolder viewHolder = (sentViewHolder) holder;
            viewHolder.binding.messageSent.setText(message.getMessage());//errormaybe int to reso

            if(message.getFeeling()>=0) {
                viewHolder.binding.feelingSent.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feelingSent.setVisibility(View.VISIBLE);
            } else{
                viewHolder.binding.feelingSent.setVisibility(View.GONE);
            }

            viewHolder.binding.messageSent.setOnTouchListener((view, motionEvent) -> {
                popup.onTouch(view, motionEvent);
                return false;
            });
        } else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.messageRecieve.setText(message.getMessage());

            if(message.getFeeling()>=0) {
                viewHolder.binding.feelingRecieve.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feelingRecieve.setVisibility(View.VISIBLE);
            } else{
                viewHolder.binding.feelingRecieve.setVisibility(View.GONE);
            }

            viewHolder.binding.messageRecieve.setOnTouchListener((view, motionEvent) -> {
                popup.onTouch(view, motionEvent);
                return false;
            });

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class sentViewHolder extends RecyclerView.ViewHolder{

        ItemSentBinding binding;

        public sentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ItemReceiveBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }

}
