package com.example.chatapp.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.Adapters.MessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;

    String receiverUid, senderUid;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Message");
        dialog.setCancelable(false);

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this,messages, senderRoom, receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen){
                binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });





        String name = getIntent().getStringExtra("name");


        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                                .addValueEventListener(new ValueEventListener() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        messages.clear();
                                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                            Message message = snapshot1.getValue(Message.class);
                                            assert message != null;
                                            message.setMessageId(snapshot1.getKey());
                                            messages.add(message);
                                        }
                                        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

        binding.sendBtn.setOnClickListener(view -> {
            String messageTxt = binding.messaageBox.getText().toString();
            Date date = new Date();
            Message message = new Message(messageTxt, senderUid, date.getTime());
            binding.messaageBox.setText("");
            String randomKey = database.getReference().push().getKey();

            assert randomKey != null;
            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomKey)
                    .setValue(message).addOnSuccessListener(unused -> database.getReference().child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(unused1 -> {

                            }));
            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg",message.getMessage());
            lastMsgObj.put("lastMsgTime", date.getTime());

            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

        });

        binding.attachmentBtn.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25){
            if(data!=null){
                if(data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(task -> {
                        dialog.dismiss();
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String filePath = uri.toString();


                                String messageTxt = binding.messaageBox.getText().toString();
                                Date date = new Date();
                                Message message = new Message(messageTxt, senderUid, date.getTime());
                                message.setImageUrl(filePath);
                                message.setMessage("photo");
                                binding.messaageBox.setText("");
                                String randomKey = database.getReference().push().getKey();

                                assert randomKey != null;
                                database.getReference().child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(unused -> database.getReference().child("chats")
                                                .child(receiverRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(unused1 -> {

                                                }));
                                HashMap<String, Object> lastMsgObj = new HashMap<>();
                                lastMsgObj.put("lastMsg",message.getMessage());
                                lastMsgObj.put("lastMsgTime", date.getTime());

                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                            });
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}