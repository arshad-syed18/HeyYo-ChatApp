package com.example.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.chatapp.databinding.ActivityPhonenoBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class PhoneNumber extends AppCompatActivity {

    ActivityPhonenoBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhonenoBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            Intent intent = new Intent(PhoneNumber.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Objects.requireNonNull(getSupportActionBar()).hide();
        binding.phoneBox.requestFocus();
        binding.continueBTN.setOnClickListener(view -> {
            Intent intent = new Intent(PhoneNumber.this, OTPverify.class);
            intent.putExtra("phoneNumber",binding.phoneBox.getText().toString());
            startActivity(intent);
        });
    }
}