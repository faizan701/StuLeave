package com.example.stuleave;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeaveApplicationActivity extends AppCompatActivity {
    private EditText nameEditText, rollNoEditText, emailEditText, startDateEditText, endDateEditText, reasonEditText;
    private Button uploadDocumentButton, uploadImageButton, submitButton;
    private TextView documentNameTextView;
    private ProgressBar progressBar;
    private Uri documentUri;
    private String documentUrl;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    private static final int PICK_DOCUMENT_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_application);

        try {
            // Initialize Firebase instances
            db = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Initialize views
            initializeViews();
            setupClickListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        try {
            nameEditText = findViewById(R.id.nameEditText);
            rollNoEditText = findViewById(R.id.rollNoEditText);
            emailEditText = findViewById(R.id.emailEditText);
            startDateEditText = findViewById(R.id.startDateEditText);
            endDateEditText = findViewById(R.id.endDateEditText);
            reasonEditText = findViewById(R.id.reasonEditText);
            uploadDocumentButton = findViewById(R.id.uploadDocumentButton);
            uploadImageButton = findViewById(R.id.uploadImageButton);
            submitButton = findViewById(R.id.submitButton);
            documentNameTextView = findViewById(R.id.documentNameTextView);
            progressBar = findViewById(R.id.progressBar);
        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            uploadDocumentButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent, PICK_DOCUMENT_REQUEST);
                } catch (Exception e) {
                    Toast.makeText(this, "Error selecting document: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });

            uploadImageButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                } catch (Exception e) {
                    Toast.makeText(this, "Error selecting image: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });

            submitButton.setOnClickListener(v -> submitApplication());
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up listeners: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if ((requestCode == PICK_DOCUMENT_REQUEST || requestCode == PICK_IMAGE_REQUEST) 
                    && resultCode == RESULT_OK && data != null) {
                documentUri = data.getData();
                if (documentUri != null) {
                    documentNameTextView.setText("File selected: " + documentUri.getLastPathSegment());
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error processing selected file: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void submitApplication() {
        try {
            String name = nameEditText.getText().toString().trim();
            String rollNo = rollNoEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();
            String reason = reasonEditText.getText().toString().trim();

            if (name.isEmpty() || rollNo.isEmpty() || email.isEmpty() || 
                startDate.isEmpty() || endDate.isEmpty() || reason.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);

            if (documentUri != null) {
                uploadFile();
            } else {
                saveApplicationToFirestore(null);
            }
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Error submitting application: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile() {
        try {
            String fileName = UUID.randomUUID().toString();
            String fileExtension = getFileExtension(documentUri);
            String fullFileName = fileName + "." + fileExtension;
            
            StorageReference storageRef = storage.getReference().child("documents/" + fullFileName);

            storageRef.putFile(documentUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                documentUrl = uri.toString();
                                saveApplicationToFirestore(documentUrl);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                submitButton.setEnabled(true);
                                Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "File upload failed: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Error uploading file: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        try {
            String path = uri.getLastPathSegment();
            if (path != null && path.contains(".")) {
                return path.substring(path.lastIndexOf(".") + 1);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error getting file extension: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
        return "pdf"; // Default extension
    }

    private void saveApplicationToFirestore(String documentUrl) {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                Toast.makeText(this, "No user logged in. Please log in again.", 
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LeaveApplicationActivity.this, LoginActivity.class));
                finish();
                return;
            }
            
            String name = nameEditText.getText().toString().trim();
            String rollNo = rollNoEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();
            String reason = reasonEditText.getText().toString().trim();

            Map<String, Object> application = new HashMap<>();
            application.put("name", name);
            application.put("rollNo", rollNo);
            application.put("email", email);
            application.put("startDate", startDate);
            application.put("endDate", endDate);
            application.put("reason", reason);
            application.put("documentUrl", documentUrl);
            application.put("status", "pending");
            application.put("timestamp", System.currentTimeMillis());
            application.put("studentId", currentUser.getUid());

            db.collection("leaveApplications")
                    .add(application)
                    .addOnSuccessListener(documentReference -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "Application submitted successfully", 
                                Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "Failed to submit application: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Error saving application: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
} 