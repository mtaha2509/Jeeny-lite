package com.example.ride_hailingapp.driver;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ride_hailingapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


import com.example.ride_hailingapp.R;

public class DriverInfoActivity extends AppCompatActivity {
    private ImageView profileImage;
    private Spinner vehicleTypeSpinner;
    private EditText vehicleModelInput, plateNumberInput;
    private MaterialButton submitDriverInfoBtn;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_info);
        // Firebase Init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // UI Bindings
        profileImage = findViewById(R.id.profileImage);
        vehicleTypeSpinner = findViewById(R.id.vehicleTypeSpinner);
        vehicleModelInput = findViewById(R.id.vehicleModelInput);
        plateNumberInput = findViewById(R.id.plateNumberInput);
        submitDriverInfoBtn = findViewById(R.id.submitDriverInfoBtn);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        profileImage.setImageURI(imageUri);
                    }
                }
        );

        // Set vehicle type spinner values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Car", "Rickshaw", "Bike"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleTypeSpinner.setAdapter(adapter);

        // Select Image
        profileImage.setOnClickListener(v -> openImageChooser());

        // Submit Form
        submitDriverInfoBtn.setOnClickListener(v -> {
            if (validateFields()) {
                uploadProfileImageAndSaveInfo();
            }
        });
    }
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }


    private boolean validateFields() {
        if (imageUri == null) {
            Toast.makeText(this, "Please upload your profile image", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (vehicleModelInput.getText().toString().trim().isEmpty()) {
            vehicleModelInput.setError("Enter vehicle model");
            return false;
        }
        if (plateNumberInput.getText().toString().trim().isEmpty()) {
            plateNumberInput.setError("Enter plate number");
            return false;
        }
        return true;
    }

    private void uploadProfileImageAndSaveInfo() {
        String driverId = mAuth.getCurrentUser().getUid();
        String filename = "drivers/" + driverId + "/profile.jpg";

        StorageReference fileRef = storageRef.child(filename);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveDriverInfoToFirestore(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDriverInfoToFirestore(String imageUrl) {
        String driverId = mAuth.getCurrentUser().getUid();

        Map<String, Object> driverData = new HashMap<>();
        driverData.put("vehicleType", vehicleTypeSpinner.getSelectedItem().toString());
        driverData.put("vehicleModel", vehicleModelInput.getText().toString().trim());
        driverData.put("plateNumber", plateNumberInput.getText().toString().trim());
        driverData.put("profileImageUrl", imageUrl);
        driverData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("drivers").document(driverId)
                .set(driverData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DriverInfoActivity.this, DriverRidesActivity.class));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}