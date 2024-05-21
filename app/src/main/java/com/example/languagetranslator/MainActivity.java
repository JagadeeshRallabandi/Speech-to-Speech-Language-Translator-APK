/*package com.example.languagetranslator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView tvResult;
    private TextView tvTranslatedText;
    private TextToSpeech textToSpeech;
    private Spinner spinnerLanguage;
    private Translate translate;
    private Button btnLogout;
    private Button btnSelectVoice;
    private TextView tvRecentTranslations;

    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private Spinner spinnerVoice;
    private TextView tvSelectVoice; // Changed from Button to TextView

    private String userUsername;
    public ArrayList<String> recentTranslations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userUsername = intent.getStringExtra("username");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("users").child(userUsername);
        tvResult = findViewById(R.id.tvResult);
        tvTranslatedText = findViewById(R.id.tvTranslatedText);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        Button btnSpeak = findViewById(R.id.btnSpeak);
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        tvRecentTranslations = findViewById(R.id.tvRecentTranslations);

        // Initialize TextToSpeech object
        textToSpeech = new TextToSpeech(this, this);

        // Initialize spinner and button for voice selection
         btnSelectVoice = findViewById(R.id.btnSelectVoice);
        spinnerVoice = findViewById(R.id.spinnerVoice);// Changed from Button to TextView reverse

        // Setup voice selection button click listener
        btnSelectVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected voice from spinner
                String selectedVoice = spinnerVoice.getSelectedItem().toString();

                // Set the selected voice for TextToSpeech engine
                setVoice(selectedVoice);
            }
        });



        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("username", userUsername);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        List<String> languages = new ArrayList<>();
        languages.add("Hindi");
        languages.add("English");
        languages.add("French");
        languages.add("Spanish");
        languages.add("Telugu");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        loadRecentTranslations();
    }

    private void loadRecentTranslations() {
        userRef.child("recentTranslations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recentTranslations = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    recentTranslations.add(snapshot.getValue(String.class));
                }
                displayRecentTranslations();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DB", "Database error", databaseError.toException());
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Load available locales
            Locale[] availableLocales = Locale.getAvailableLocales();
            List<Locale> locales = new ArrayList<>();
            for (Locale locale : availableLocales) {
                if (textToSpeech.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
                    locales.add(locale);
                }
            }

            // Populate spinner with available locales
            ArrayAdapter<Locale> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locales);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVoice.setAdapter(adapter);
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "auto");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Sorry! Your device does not support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String recognizedText = result.get(0);
            tvResult.setText(recognizedText);
            translateAndSpeak(recognizedText);
        }
    }

    /*private void translateAndSpeak(final String textToTranslate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (translate == null) {
                        GoogleCredentials credentials = GoogleCredentials.fromStream(getResources().openRawResource(R.raw.credentials));
                        translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
                    }

                    String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
                    Translation translation = translate.translate(
                            textToTranslate,
                            Translate.TranslateOption.targetLanguage(getLanguageCode(selectedLanguage)),
                            Translate.TranslateOption.format("text")
                    );

                    final String translatedText = translation.getTranslatedText();

                    // Run on UI thread to update UI components
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTranslatedText.setText(translatedText);
                            updateRecentTranslations(translatedText);
                        }
                    });
                } catch (IOException e) {
                    Log.e("Translation", "Error authenticating with Google Cloud Translation API", e);
                } catch (Exception e) {
                    Log.e("Translation", "Error translating text", e);
                }
            }
        }).start();
    }*/

      /*private void translateAndSpeak(final String textToTranslate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (translate == null) {
                        GoogleCredentials credentials = GoogleCredentials.fromStream(getResources().openRawResource(R.raw.credentials));
                        translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
                    }

                    String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
                    Translation translation = translate.translate(
                            textToTranslate,
                            Translate.TranslateOption.targetLanguage(getLanguageCode(selectedLanguage)),
                            Translate.TranslateOption.format("text")
                    );

                    final String translatedText = translation.getTranslatedText();

                    // Run on UI thread to update UI components
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTranslatedText.setText(translatedText);

                            // Get the selected voice
                            Voice selectedVoice = (Voice) spinnerVoice.getSelectedItem();
                            // Set the selected voice for TextToSpeech engine
                            textToSpeech.setVoice(selectedVoice);

                            // Speak the translated text using the selected voice
                            textToSpeech.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null);

                            updateRecentTranslations(translatedText);
                        }
                    });
                } catch (IOException e) {
                    Log.e("Translation", "Error authenticating with Google Cloud Translation API", e);
                } catch (Exception e) {
                    Log.e("Translation", "Error translating text", e);
                }
            }
        }).start();
    }

private String getLanguageCode(String language) {
        switch (language) {
            case "English": return "en";
            case "Hindi": return "hi";
            case "French": return "fr";
            case "Spanish": return "es";
            case "Telugu": return "te";
            default: return "hi";
        }
    }

    private void updateRecentTranslations(String translation) {
        recentTranslations.add(0, translation);
        userRef.child("recentTranslations").setValue(recentTranslations);
        displayRecentTranslations();
    }

    private void displayRecentTranslations() {
        StringBuilder recentTranslationsText = new StringBuilder();
        int count = 0;
        for (String translation : recentTranslations) {
            if (count < 5) {
                recentTranslationsText.append(translation).append("\n");
                count++;
            } else {
                break;
            }
        }
        tvRecentTranslations.setText(recentTranslationsText.toString());
    }
    // Method to set the selected voice for TextToSpeech engine
    private void setVoice(String selectedVoice) {
        // Find the selected voice from the spinner adapter
        for (int i = 0; i < spinnerVoice.getAdapter().getCount(); i++) {
            Voice voice = (Voice) spinnerVoice.getItemAtPosition(i);
            if (voice.getName().equals(selectedVoice)) {
                // Set the selected voice
                textToSpeech.setVoice(voice);
                break;
            }
        }
    }


    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

}*/
package com.example.languagetranslator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.util.Locale;
import android.net.Uri;
import java.io.InputStream;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView tvResult;
    private TextView tvTranslatedText;
    private TextToSpeech textToSpeech;
    private Spinner spinnerLanguage;
    private Translate translate;
    private Button btnLogout;
    private TextView tvRecentTranslations;

    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private Spinner spinnerVoice;
    private Button btnSelectVoice;
    private static final int REQUEST_CODE_FILE_PICKER = 123;


    // TextToSpeech object
    // private TextToSpeech textToSpeech;

    private String userUsername;
    public ArrayList<String> recentTranslations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userUsername = intent.getStringExtra("username");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("users").child(userUsername);
        tvResult = findViewById(R.id.tvResult);
        tvTranslatedText = findViewById(R.id.tvTranslatedText);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        Button btnSpeak = findViewById(R.id.btnSpeak);
        btnLogout = findViewById(R.id.btnLogout);
        tvRecentTranslations = findViewById(R.id.tvRecentTranslations);
        ImageButton btnProfile = findViewById(R.id.btnProfile);
       // textToSpeech = new TextToSpeech(this, this);


        // Initialize TextToSpeech object
        textToSpeech = new TextToSpeech(this, this);

        // Initialize spinner and button for voice selection
        btnSelectVoice = findViewById(R.id.btnSelectVoice);
        spinnerVoice = findViewById(R.id.spinnerVoice);


        // Setup voice selection button click listener
        btnSelectVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected voice from spinner
                String selectedVoice = spinnerVoice.getSelectedItem().toString();

                // Set the selected voice for TextToSpeech engine
                setVoice(selectedVoice);
            }
        });




        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("username", userUsername);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        List<String> languages = new ArrayList<>();
        languages.add("Hindi");
        languages.add("English");
        languages.add("French");
        languages.add("Spanish");
        languages.add("Telugu");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        loadRecentTranslations();
    }



    private void loadRecentTranslations() {
        userRef.child("recentTranslations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recentTranslations = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    recentTranslations.add(snapshot.getValue(String.class));
                }
                displayRecentTranslations();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DB", "Database error", databaseError.toException());
            }
        });
    }

   /* public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.forLanguageTag("hin"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }*/

   /* @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Load available voices
            Set<Voice> voiceSet = textToSpeech.getVoices();
            List<Voice> voices = new ArrayList<>(voiceSet);
            int result = textToSpeech.setLanguage(Locale.forLanguageTag("hin"));

            // Populate spinner with available voices
            ArrayAdapter<Voice> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, voices);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVoice.setAdapter(adapter);
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }*/
  @Override
  public void onInit(int status) {
       if (status == TextToSpeech.SUCCESS) {
           // Load available voices
           Set<Voice> voiceSet = textToSpeech.getVoices();
           List<Voice> voices = new ArrayList<>(voiceSet);

           // Populate spinner with available voices
           ArrayAdapter<Voice> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, voices);
           adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
           spinnerVoice.setAdapter(adapter);

           // Set language for TextToSpeech engine
           int result = textToSpeech.setLanguage(Locale.forLanguageTag("hin"));
           if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
               Log.e("TTS", "Language not supported");
           }
       } else {
           Log.e("TTS", "Initialization failed");
       }
   }



    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "auto");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Sorry! Your device does not support speech input", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String recognizedText = result.get(0);
            tvResult.setText(recognizedText);
            translateAndSpeak(recognizedText);
        }

    }



    private void translateAndSpeak(final String textToTranslate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (translate == null) {
                        GoogleCredentials credentials = GoogleCredentials.fromStream(getResources().openRawResource(R.raw.credentials));
                        translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
                    }

                    String selectedLanguage = spinnerLanguage.getSelectedItem().toString();
                    Translation translation = translate.translate(
                            textToTranslate,
                            Translate.TranslateOption.targetLanguage(getLanguageCode(selectedLanguage)),
                            Translate.TranslateOption.format("text")
                    );

                    final String translatedText = translation.getTranslatedText();

                    // Run on UI thread to update UI components
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTranslatedText.setText(translatedText);

                            // Get the selected voice
                            Voice selectedVoice = (Voice) spinnerVoice.getSelectedItem();
                            // Set the selected voice for TextToSpeech engine
                            textToSpeech.setVoice(selectedVoice);

                            // Speak the translated text using the selected voice
                            textToSpeech.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null);

                            updateRecentTranslations(translatedText);
                        }
                    });
                } catch (IOException e) {
                    Log.e("Translation", "Error authenticating with Google Cloud Translation API", e);
                } catch (Exception e) {
                    Log.e("Translation", "Error translating text", e);
                }
            }
        }).start();
    }


    private String getLanguageCode(String language) {
        switch (language) {
            case "English": return "en";
            case "Hindi": return "hi";
            case "French": return "fr";
            case "Spanish": return "es";
            case "Telugu": return "te";
            default: return "hi";
        }
    }

   /* private void updateRecentTranslations(String translation) {
        recentTranslations.add(0, translation);
        if (recentTranslations.size() > 5) {
            recentTranslations.remove(5);
        }
        userRef.child("recentTranslations").setValue(recentTranslations);
        displayRecentTranslations();
    }*/
   private void updateRecentTranslations(String translation) {
       recentTranslations.add(0, translation);
       userRef.child("recentTranslations").setValue(recentTranslations);
       displayRecentTranslations();
   }

    private void displayRecentTranslations() {
        StringBuilder recentTranslationsText = new StringBuilder();
        int count = 0;
        for (String translation : recentTranslations) {
            if (count < 5) {
                recentTranslationsText.append(translation).append("\n");
                count++;
            } else {
                break;
            }
        }
        tvRecentTranslations.setText(recentTranslationsText.toString());
    }

    /*private void displayRecentTranslations() {
        StringBuilder recentTranslationsText = new StringBuilder();
        for (String translation : recentTranslations) {
            recentTranslationsText.append(translation).append("\n");
        }
        tvRecentTranslations.setText(recentTranslationsText.toString());
    }*/

    // Method to set the selected voice for TextToSpeech engine
    private void setVoice(String selectedVoice) {
        // Find the selected voice from the spinner adapter
        for (int i = 0; i < spinnerVoice.getAdapter().getCount(); i++) {
            Voice voice = (Voice) spinnerVoice.getItemAtPosition(i);
            if (voice.getName().equals(selectedVoice)) {
                // Set the selected voice
                textToSpeech.setVoice(voice);
                break;
            }
        }
    }


    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

}

