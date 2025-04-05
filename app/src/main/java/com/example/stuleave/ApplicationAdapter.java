package com.example.stuleave;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<LeaveApplication> applications;
    private Context context;
    private boolean isTeacher;
    private FirebaseFirestore db;

    public ApplicationAdapter(Context context, List<LeaveApplication> applications, boolean isTeacher) {
        this.context = context;
        this.applications = applications;
        this.isTeacher = isTeacher;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_application, parent, false);
            return new ApplicationViewHolder(view);
        } catch (Exception e) {
            Toast.makeText(context, "Error creating view holder: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            // Fallback to a simple view in case of error
            View fallbackView = new View(parent.getContext());
            fallbackView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ApplicationViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        try {
            LeaveApplication application = applications.get(position);
            
            if (holder.studentNameTextView != null)
                holder.studentNameTextView.setText("Name: " + application.getName());
            
            if (holder.rollNoTextView != null)
                holder.rollNoTextView.setText("Roll No: " + application.getRollNo());
            
            if (holder.dateRangeTextView != null)
                holder.dateRangeTextView.setText("Period: " + application.getStartDate() + " to " + application.getEndDate());
            
            if (holder.reasonTextView != null)
                holder.reasonTextView.setText("Reason: " + application.getReason());
            
            if (holder.statusTextView != null)
                holder.statusTextView.setText("Status: " + application.getStatus());
            
            // Show/hide buttons based on user type and application status
            if (isTeacher && application.getStatus().equals("pending")) {
                if (holder.approveButton != null) 
                    holder.approveButton.setVisibility(View.VISIBLE);
                if (holder.rejectButton != null)
                    holder.rejectButton.setVisibility(View.VISIBLE);
            } else {
                if (holder.approveButton != null)
                    holder.approveButton.setVisibility(View.GONE);
                if (holder.rejectButton != null)
                    holder.rejectButton.setVisibility(View.GONE);
            }
            
            // Show download document button only if document URL exists
            if (application.getDocumentUrl() != null && !application.getDocumentUrl().isEmpty()) {
                if (holder.viewDocumentButton != null)
                    holder.viewDocumentButton.setVisibility(View.VISIBLE);
            } else {
                if (holder.viewDocumentButton != null)
                    holder.viewDocumentButton.setVisibility(View.GONE);
            }
            
            // Set click listeners
            if (holder.approveButton != null) {
                holder.approveButton.setOnClickListener(v -> {
                    updateApplicationStatus(application.getId(), "approved");
                });
            }
            
            if (holder.rejectButton != null) {
                holder.rejectButton.setOnClickListener(v -> {
                    updateApplicationStatus(application.getId(), "rejected");
                });
            }
            
            if (holder.viewDocumentButton != null) {
                holder.viewDocumentButton.setOnClickListener(v -> {
                    try {
                        if (application.getDocumentUrl() != null && !application.getDocumentUrl().isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(application.getDocumentUrl()));
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "No document available", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Error opening document: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error binding data: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateApplicationStatus(String applicationId, String status) {
        try {
            db.collection("leaveApplications")
                    .document(applicationId)
                    .update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        String message = status.equals("approved") ? "Application approved" : "Application rejected";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update status: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(context, "Error updating status: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return applications != null ? applications.size() : 0;
    }

    static class ApplicationViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameTextView, rollNoTextView, dateRangeTextView, reasonTextView, statusTextView;
        Button approveButton, rejectButton, viewDocumentButton;

        ApplicationViewHolder(View itemView) {
            super(itemView);
            try {
                studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
                rollNoTextView = itemView.findViewById(R.id.rollNoTextView);
                dateRangeTextView = itemView.findViewById(R.id.dateRangeTextView);
                reasonTextView = itemView.findViewById(R.id.reasonTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                approveButton = itemView.findViewById(R.id.approveButton);
                rejectButton = itemView.findViewById(R.id.rejectButton);
                viewDocumentButton = itemView.findViewById(R.id.viewDocumentButton);
            } catch (Exception e) {
                // Views might not be found if using fallback view
            }
        }
    }
} 