package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CheckEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private int verificationAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_email);

        mAuth = FirebaseAuth.getInstance();

        Button buttonEmailVerified = findViewById(R.id.buttonEmailVerified);
        buttonEmailVerified.setOnClickListener(view -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.reload().addOnCompleteListener(task -> {
                    if (user.isEmailVerified()) {
                        saveUserInfo();
                    } else {
                        if (verificationAttempts < 3) {
                            // Increment verification attempts counter
                            verificationAttempts++;
                            Toast.makeText(CheckEmailActivity.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Delete the unverified user if the email was not sent
                            if (!user.isEmailVerified() && !user.isEmailVerified()) {
                                deleteUnverifiedUser(user);
                                Toast.makeText(CheckEmailActivity.this, "Email verification failed. Account deleted.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CheckEmailActivity.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void saveUserInfo() {
        String name = getIntent().getStringExtra("name");
        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        // Initialize Firebase database reference
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Create a new user object
        HelperClass user = new HelperClass(name, email, username, password);

        // Save user data to the database
        usersRef.child(username).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CheckEmailActivity.this, "Successfully saved your data to database.", Toast.LENGTH_SHORT).show();
                        // User data saved successfully, redirect to login page
                        startActivity(new Intent(CheckEmailActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        // Error occurred while saving user data
                        Toast.makeText(CheckEmailActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUnverifiedUser(FirebaseUser user) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Unverified user deleted from authentication.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete unverified user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
