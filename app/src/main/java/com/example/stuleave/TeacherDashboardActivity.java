package com.example.stuleave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardActivity extends AppCompatActivity {
    private Button logoutButton;
    private RecyclerView applicationsRecyclerView;
    private ApplicationAdapter applicationAdapter;
    private List<LeaveApplication> applications;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView noApplicationsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        try {
            // Initialize Firebase instances
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Initialize views
            initializeViews();
            setupClickListeners();
            setupRecyclerView();
            loadApplications();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        logoutButton = findViewById(R.id.logoutButton);
        applicationsRecyclerView = findViewById(R.id.applicationsRecyclerView);
        noApplicationsTextView = findViewById(R.id.noApplicationsTextView);
        applications = new ArrayList<>();
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> {
            try {
                mAuth.signOut();
                startActivity(new Intent(TeacherDashboardActivity.this, LoginActivity.class));
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "Error signing out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        try {
            applicationAdapter = new ApplicationAdapter(this, applications, true);
            applicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            applicationsRecyclerView.setAdapter(applicationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadApplications() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "No user logged in. Please log in again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TeacherDashboardActivity.this, LoginActivity.class));
                finish();
                return;
            }
            
            // Using a simpler query without complex ordering to avoid index issues
            db.collection("leaveApplications")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            applications.clear();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    try {
                                        LeaveApplication application = document.toObject(LeaveApplication.class);
                                        application.setId(document.getId());
                                        applications.add(application);
                                    } catch (Exception e) {
                                        Toast.makeText(this, "Error parsing application: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                noApplicationsTextView.setVisibility(View.GONE);
                                applicationsRecyclerView.setVisibility(View.VISIBLE);
                            } else {
                                noApplicationsTextView.setVisibility(View.VISIBLE);
                                applicationsRecyclerView.setVisibility(View.GONE);
                            }
                            applicationAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error updating UI: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error loading applications: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void approveApplication(String applicationId) {
        try {
            db.collection("leaveApplications")
                    .document(applicationId)
                    .update("status", "approved")
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Application approved",
                            Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to approve application: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(this, "Error approving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void rejectApplication(String applicationId) {
        try {
            db.collection("leaveApplications")
                    .document(applicationId)
                    .update("status", "rejected")
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Application rejected",
                            Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject application: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(this, "Error rejecting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 