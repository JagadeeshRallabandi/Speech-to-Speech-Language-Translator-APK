package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeleteTranslationsActivity extends AppCompatActivity {

    private Button btnCancel, btnYes;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_translations);

        btnCancel = findViewById(R.id.btnCancel);
        btnYes = findViewById(R.id.btnYes);

        // Retrieve username from intent extras
        username = getIntent().getStringExtra("username");

        btnCancel.setOnClickListener(v -> navigateBackToSettings());
        btnYes.setOnClickListener(v -> clearRecentTranslations());
    }

    private void navigateBackToSettings() {
        // Navigate back to SettingsActivity
        startActivity(new Intent(DeleteTranslationsActivity.this, SettingsActivity.class));
        finish();
    }

    private void clearRecentTranslations() {
        // Get the reference to the recentTranslations node for the specific user
        DatabaseReference userTranslationsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(username).child("recentTranslations");

        // Clear the recentTranslations by setting the node to null
        userTranslationsRef.setValue(null, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    // Clearing successful
                    Toast.makeText(DeleteTranslationsActivity.this, "Translation history cleared successfully!\nPlease Login again", Toast.LENGTH_SHORT).show();
                    logoutUser(); // Logout after clearing translations
                } else {
                    // Clearing failed
                    Toast.makeText(DeleteTranslationsActivity.this, "Failed to clear translation history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut(); // Firebase sign out
        Intent intent = new Intent(DeleteTranslationsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // This will clear all the activities on top of LoginActivity
    }
}
