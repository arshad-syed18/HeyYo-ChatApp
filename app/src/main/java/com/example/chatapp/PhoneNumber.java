package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chatapp.databinding.ActivityPhonenoBinding;

import java.util.Objects;


public class PhoneNumber extends AppCompatActivity {

    ActivityPhonenoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhonenoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding.phoneBox.requestFocus();
        binding.continueBTN.setOnClickListener(view -> {
            Intent intent = new Intent(PhoneNumber.this, OTPverify.class);
            intent.putExtra("phoneNumber",binding.phoneBox.getText().toString());
            startActivity(intent);
        });
    }
}