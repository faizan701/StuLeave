package com.example.stuleave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup userTypeRadioGroup;
    private RadioButton studentRadioButton, teacherRadioButton;
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize views
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        studentRadioButton = findViewById(R.id.studentRadioButton);
        teacherRadioButton = findViewById(R.id.teacherRadioButton);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> registerUser());
        
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
    
    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        
        // Get selected user type
        String userType = studentRadioButton.isChecked() ? "student" : "teacher";
        
        // Validate input fields
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }
        
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
        
        if (password.length() < 6) {
            passwordEditText.setError("Password should be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);
        
        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    
                    if (task.isSuccessful()) {
                        // Save additional user data in Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("userType", userType);
                        
                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, 
                                            "Registration successful", Toast.LENGTH_SHORT).show();
                                    
                                    // Navigate to appropriate dashboard based on user type
                                    if (userType.equals("student")) {
                                        startActivity(new Intent(RegisterActivity.this, 
                                                StudentDashboardActivity.class));
                                    } else {
                                        startActivity(new Intent(RegisterActivity.this, 
                                                TeacherDashboardActivity.class));
                                    }
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this,
                                            "Failed to save user data: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this,
                                    "Email is already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
} 