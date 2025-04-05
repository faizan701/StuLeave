package com.example.stuleave;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;

public class StuLeaveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            
            // Initialize Firebase App Check
            FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
            firebaseAppCheck.installAppCheckProviderFactory(
                    SafetyNetAppCheckProviderFactory.getInstance());
        } catch (Exception e) {
            // Catch any initialization errors to prevent app crashes
            e.printStackTrace();
        }
    }
} 