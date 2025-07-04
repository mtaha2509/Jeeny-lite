package com.example.ride_hailingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ride_hailingapp.R;
import com.google.android.material.button.MaterialButton;

public class RoleSelectActivity extends AppCompatActivity {

    private MaterialButton riderButton, driverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        riderButton = findViewById(R.id.riderButton);
        driverButton = findViewById(R.id.driverButton);

        riderButton.setOnClickListener(view -> {
            navigateToRegister("rider");
        });

        driverButton.setOnClickListener(view -> {
            navigateToRegister("driver");
        });
    }

    private void navigateToRegister(String role) {
        Intent intent = new Intent(RoleSelectActivity.this, RegisterActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }
}
