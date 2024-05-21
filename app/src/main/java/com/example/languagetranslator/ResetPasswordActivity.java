package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword;
    private Button btnSave;
    private String username;
    private String currentBcryptPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveNewPassword());
    }

    private void saveNewPassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter both current and new password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve current password from database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(username);
        userRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentBcryptPassword = dataSnapshot.getValue(String.class);
                    // Check if the current password matches
                    if (BCrypt.checkpw(currentPassword, currentBcryptPassword)) {
                        // Update password
                        updatePassword(newPassword);
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Incorrect current password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Failed to retrieve current password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ResetPasswordActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword(String newPassword) {
        // Encrypt the new password using bcrypt
        String bcryptPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // Update the password in Firebase database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(username);
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", bcryptPassword);
        userRef.updateChildren(updates)
                .addOnSuccessListener(task -> {
                    Toast.makeText(ResetPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to SettingsActivity
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResetPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                });
    }
}
