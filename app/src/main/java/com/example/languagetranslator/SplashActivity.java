package com.example.languagetranslator;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 4000;
    private static final int LETTER_DELAY = 80;
    private static final String SPLASH_TEXT = "Speech Translator";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animateText();

        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(SplashActivity.this, BiometricActivity.class);
            startActivity(mainIntent);
            finish();
        }, SPLASH_DURATION);
    }

    private void animateText() {
        TextView textView = findViewById(R.id.moving_text);

        StringBuilder sentenceBuilder = new StringBuilder();

        int totalAnimationDuration = SPLASH_TEXT.length() * LETTER_DELAY + 3000;

        for (int i = 0; i < SPLASH_TEXT.length(); i++) {
            char letter = SPLASH_TEXT.charAt(i);
            sentenceBuilder.append(letter);
            String sentence = sentenceBuilder.toString();

            final TextView fallingTextView = new TextView(this);
            fallingTextView.setText(String.valueOf(letter));
            fallingTextView.setTextSize(30);
            ViewGroup container = findViewById(R.id.container);
            container.addView(fallingTextView);

            float startY = -1000f;
            float endY = 1000f;
            float startX = i * 70f;
            float endX = startX;

            ObjectAnimator animatorY = ObjectAnimator.ofFloat(fallingTextView, "translationY", startY, endY);
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(fallingTextView, "translationX", startX, endX); // Move horizontally

            // Apply rotation effect
            ObjectAnimator rotation = ObjectAnimator.ofFloat(fallingTextView, "rotation", 0f, 360f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animatorY, animatorX, rotation);
            animatorSet.setStartDelay(i * LETTER_DELAY);
            animatorSet.setDuration(2000);
            animatorSet.start();

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    container.removeView(fallingTextView);

                    if (container.getChildCount() == 0) {
                        textView.setText(sentence);
                    }
                }
            });
        }

        new Handler().postDelayed(() -> {
            textView.setText(sentenceBuilder.toString());
        }, totalAnimationDuration);
    }




}