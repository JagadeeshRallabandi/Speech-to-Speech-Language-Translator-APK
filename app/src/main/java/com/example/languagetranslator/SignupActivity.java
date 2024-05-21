package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

import com.google.firebase.database.ValueEventListener;


import org.mindrot.jbcrypt.BCrypt;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    CheckBox checkBox;
    GoogleApiClient googleApiClient;
    // String SiteKey ="6Lcn98kpAAAAAAYePOHLN7qZCM58nvtTchXzMXpy";
    //String SiteKey ="6Lc798kpAAAAAAOArR9P5wCPki3BxEVfeuTCoyXM";
    //String SiteKey ="6LdM_ckpAAAAANSUV-7PcgrnA302UtqTspUVvWXc";
    String SiteKey = "6LcjCMspAAAAAC40_dxQjdS_iDcYYBRwqVrcYOaT";
    boolean passwordVisible;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);
        checkBox = findViewById(R.id.CheckBox_id);
        passwordVisible = false;

        setupPasswordVisibilityToggle();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        //your implementation
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        //your implementation
                    }
                })
                .build();
        googleApiClient.connect();

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()) {
                    SafetyNet.SafetyNetApi.verifyWithRecaptcha(googleApiClient, SiteKey)
                            .setResultCallback(new ResultCallback<SafetyNetApi.RecaptchaTokenResult>() {
                                @Override
                                public void onResult(@NonNull SafetyNetApi.RecaptchaTokenResult recaptchaTokenResult) {

                                    Status status = recaptchaTokenResult.getStatus();
                                    if ((status != null && status.isSuccess())) {
                                        Toast.makeText(SignupActivity.this, "Verification Successful",
                                                Toast.LENGTH_SHORT).show();
                                        checkBox.setTextColor(Color.WHITE);
                                    }

                                }
                            });
                } else {
                    checkBox.setTextColor(Color.RED);
                    Toast.makeText(SignupActivity.this, "Verification is not done",
                            Toast.LENGTH_SHORT).show();

                }
            }

        });



        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser();

            }
        });

        loginRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }

    private void setupPasswordVisibilityToggle() {
        signupPassword.setOnTouchListener((v, event) -> {
            final int Right = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= signupPassword.getRight() - signupPassword.getCompoundDrawables()[Right].getBounds().width()) {
                    int selection = signupPassword.getSelectionEnd();
                    if (passwordVisible) {
                        signupPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_24, 0);
                        signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordVisible = false;
                    } else {
                        signupPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_disabled_visible_24, 0);
                        signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordVisible = true;
                    }
                    signupPassword.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }

    private void registerNewUser() {
        final String name = signupName.getText().toString().trim();
        final String email = signupEmail.getText().toString().trim();
        final String username = signupUsername.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignupActivity.this, "All fields are required!", Toast.LENGTH_LONG).show();
            return;
        } else if(!validateRecaptcha()) {
            Toast.makeText(SignupActivity.this, "verify you are not a robot!", Toast.LENGTH_LONG).show();
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SignupActivity.this, "Username already exists!!", Toast.LENGTH_LONG).show();
                } else {
                    // Hash the password before storing it
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        sendEmailVerification(user, name, username, hashedPassword); // Pass hashed password
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Failed to create user.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(SignupActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public boolean validateRecaptcha() {
        if (checkBox.isChecked()) {
            return true; // Recaptcha checkbox is checked
        } else {
            Toast.makeText(this, "Please verify that you are not a robot", Toast.LENGTH_SHORT).show();
            return false; // Recaptcha checkbox is not checked
        }
    }

    private void sendEmailVerification(FirebaseUser user, String name, String username, String hashedPassword) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Redirect user to check their email
                            Intent intent = new Intent(SignupActivity.this, CheckEmailActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("username", username);
                            intent.putExtra("email", user.getEmail());
                            intent.putExtra("password", hashedPassword); // Pass hashed password
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}