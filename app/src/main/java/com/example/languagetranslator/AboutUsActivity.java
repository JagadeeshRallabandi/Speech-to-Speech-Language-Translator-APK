package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the layout file that contains your ScrollView
        setContentView(R.layout.activity_about_us);

        // Setting up a click listener for the Home TextView
        TextView homeTextView = findViewById(R.id.HomeText);
        homeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go to the HomeActivity when the Home text is clicked
                startActivity(new Intent(AboutUsActivity.this, HomeActivity.class));
                // Finish this activity to remove it from the back stack
                finish();
            }
        });
    }
}
