package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class YourDetailsActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvName;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_details);

        // Get username from intent extras
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Initialize TextViews
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);

        // Fetch user details from Firebase and set them to TextViews
        fetchUserDetails();
    }

    private void fetchUserDetails() {
        // Retrieve user details from Firebase Realtime Database using the obtained username
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve user details from the database
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);

                    // Set user details to TextViews
                    tvUsername.setText("Username: " + username); // Set actual username dynamically
                    tvEmail.setText("Email: " + email);
                    tvName.setText("Name: " + name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                // For simplicity, you can log the error here
            }
        });
    }
}
