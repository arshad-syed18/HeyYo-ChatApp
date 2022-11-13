package com.example.chatapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.Models.User;
import com.example.chatapp.databinding.RowConversationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>{

    Context context;
    ArrayList<User> users;

    public UsersAdapter(Context context,ArrayList<User> users){
        this.context = context;
        this.users = users;
    }


    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversations, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(senderRoom)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            @SuppressLint("SimpleDateFormat")
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                    long time = snapshot.child("lastMsgTime").getValue(Long.class);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    holder.binding.lastMsg.setText(lastMsg);
                                    holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                                }else{
                                    holder.binding.lastMsg.setText(R.string.tap_to_chat);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        holder.binding.useranme.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imageView2);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("uid", user.getUid());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        RowConversationsBinding binding;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationsBinding.bind(itemView);

        }
    }
}
