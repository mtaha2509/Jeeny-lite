package com.example.ride_hailingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ride_hailingapp.driver.DriverInfoActivity;
import com.example.ride_hailingapp.driver.DriverRidesActivity;
import com.example.ride_hailingapp.rider.RiderDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView logo = findViewById(R.id.logo);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        logo.startAnimation(anim);

        new Handler().postDelayed(() -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                String uid = auth.getCurrentUser().getUid();
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");

                                if ("rider".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(SplashActivity.this, RiderDashboardActivity.class));
                                } else if ("driver".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(SplashActivity.this, DriverRidesActivity.class));
                                } else {
                                    // Unknown role, fallback to role selection
                                    startActivity(new Intent(SplashActivity.this, RoleSelectActivity.class));
                                }
                            } else {
                                // User exists but role document doesn't exist
                                startActivity(new Intent(SplashActivity.this, RoleSelectActivity.class));
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Firestore error, fallback
                            startActivity(new Intent(SplashActivity.this, RoleSelectActivity.class));
                            finish();
                        });
            } else {
                // Not logged in
                startActivity(new Intent(SplashActivity.this, RoleSelectActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
