package com.example.languagetranslator;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class TnCActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tn_cactivity);

        // Initialize TextViews
        TextView textViewHeading = findViewById(R.id.textViewHeading);
        TextView textViewContent = findViewById(R.id.textViewContent);

        // Set Terms and Conditions text
        textViewHeading.setText("Terms and Conditions");
        textViewContent.setText("This app is currently under testing. Users acknowledge that they are participating in a testing phase, and as such, may encounter bugs, errors, or unexpected behavior. By using this app, users agree to provide feedback and report any issues encountered during testing to rjagadeesh2004@gmail.com\nJAGADEESH RALLABANDI");
    }
}
