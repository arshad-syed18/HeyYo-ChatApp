package com.example.chatapp.Activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityOtpverifyBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;



public class OTPverify extends AppCompatActivity {

    ActivityOtpverifyBinding binding;
    FirebaseAuth auth;
    String verificationId;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpverifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding.OtpText.requestFocus();


        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneLbl.setText("Verify " + phoneNumber);


        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(OTPverify.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
            this.finish();
        } else {
            String phone = "+91" + phoneNumber;
            binding.progressBar2.setVisibility(View.VISIBLE);
            sendVerificationCode(phone);
            binding.progressBar2.setVisibility(View.GONE);
        }
        binding.verifyBtn.setOnClickListener(v -> {
            if (binding.OtpText.getText().length() < 6) {
                Toast.makeText(OTPverify.this, "Please enter OTP!", Toast.LENGTH_SHORT).show();
            } else {
                verifyCode(binding.OtpText.getText().toString());
            }
        });

    }

    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phone)         // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)         // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                binding.OtpText.setText(code);

                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OTPverify.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithCredential(credential);
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(OTPverify.this, "Verification Completed!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(OTPverify.this, setupProfile.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                    else {
                        Toast.makeText(OTPverify.this, "Wrong OTP!", Toast.LENGTH_LONG).show();
                    }
                });
    }
}