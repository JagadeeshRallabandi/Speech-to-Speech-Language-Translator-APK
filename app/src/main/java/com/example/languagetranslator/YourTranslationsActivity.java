package com.example.languagetranslator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YourTranslationsActivity extends AppCompatActivity {
    private ListView listViewTranslations;
    private List<String> recentTranslations;
    private DatabaseReference userRef;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_translations);

        listViewTranslations = findViewById(R.id.listViewTranslations);
        recentTranslations = new ArrayList<>();

        // Retrieve username from intent extras
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Initialize DatabaseReference for the specific user
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(username);

        // Load recent translations
        loadRecentTranslations();
    }

    private void loadRecentTranslations() {
        userRef.child("recentTranslations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recentTranslations.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    recentTranslations.add(snapshot.getValue(String.class));
                }
                displayRecentTranslations();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(YourTranslationsActivity.this, "Failed to load translations: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecentTranslations() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recentTranslations);
        listViewTranslations.setAdapter(adapter);
    }
    public void onGeneratePDFClick(View view) {
        generatePDF(recentTranslations);
    }
    private void generatePDF(List<String> translations) {
        try {
            // Create a new PDF file
            File file = new File(getExternalFilesDir(null), "translations.pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // Load the custom font for Hindi characters
            PdfFont font = PdfFontFactory.createFont("res/font/hindi.ttf", PdfEncodings.IDENTITY_H, true);


            // Add content to the PDF
            for (String translation : translations) {
                document.add(new Paragraph(translation).setFont(font));
            }

            // Close the document
            document.close();

            // Show a message indicating the PDF was created successfully
            Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show();

            // Open the generated PDF using an Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, "com.example.languagetranslator.fileprovider", file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (IOException e) {
            // Handle exceptions
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



}
