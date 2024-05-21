package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

public class DeleteAccountActivity extends AppCompatActivity {

    private EditText etPassword;
    private Button btnCancel, btnConfirm;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        // Get username from intent extras
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Initialize views
        etPassword = findViewById(R.id.etPassword);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> {
            // Cancel button clicked, navigate back to SettingsActivity
            onBackPressed();
        });

        btnConfirm.setOnClickListener(v -> {
            // Confirm button clicked, verify password and delete account
            deleteAccount();
        });
    }

    private void deleteAccount() {
        String password = etPassword.getText().toString().trim();

        // Check if password is empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get reference to the user node in Firebase database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(username);

        // Retrieve the hashed password from the database and compare with the entered password
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String hashedPassword = dataSnapshot.child("password").getValue(String.class);
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        // Password matches, delete account
                        deleteUserAccount(userRef);
                    } else {
                        // Wrong password
                        Toast.makeText(DeleteAccountActivity.this, "Wrong password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User does not exist in the database
                    Toast.makeText(DeleteAccountActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error occurred while retrieving data from database
                Toast.makeText(DeleteAccountActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserAccount(DatabaseReference userRef) {
        // Remove the user node from Firebase database
        userRef.removeValue();

        // Delete user from Firebase Authentication
        FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnSuccessListener(aVoid -> {
                    // Logout user
                    FirebaseAuth.getInstance().signOut();

                    // Redirect to LoginActivity
                    Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Finish current activity
                })
                .addOnFailureListener(e -> {
                    // Handle failure to delete user from Firebase Authentication
                    Toast.makeText(DeleteAccountActivity.this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
