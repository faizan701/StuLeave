package com.example.stuleave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button studentLoginButton, teacherLoginButton;
    private TextView forgotPasswordTextView, registerTextView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        studentLoginButton = findViewById(R.id.studentLoginButton);
        teacherLoginButton = findViewById(R.id.teacherLoginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        registerTextView = findViewById(R.id.registerTextView);
        progressBar = findViewById(R.id.progressBar);

        // Setup click listeners
        studentLoginButton.setOnClickListener(v -> loginUser("student"));
        teacherLoginButton.setOnClickListener(v -> loginUser("teacher"));
        
        forgotPasswordTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
        
        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser(String userType) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check user type in Firestore
                            db.collection("users")
                                    .document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String storedUserType = documentSnapshot.getString("userType");
                                            if (storedUserType != null && storedUserType.equals(userType)) {
                                                // User type matches, proceed to appropriate dashboard
                                                if (userType.equals("student")) {
                                                    startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
                                                } else {
                                                    startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
                                                }
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, check their user type
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userType = documentSnapshot.getString("userType");
                            if (userType != null) {
                                // Navigate to appropriate dashboard
                                if (userType.equals("student")) {
                                    startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
                                } else {
                                    startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
                                }
                                finish();
                            }
                        }
                    });
        }
    }
} 