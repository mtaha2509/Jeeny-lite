package com.example.ride_hailingapp.rider;

import static com.example.ride_hailingapp.R.id.driverText;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ride_hailingapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class RiderDashboardActivity extends AppCompatActivity {

    EditText pickupInput, dropoffInput;
    CardView carCard, bikeCard, rickshawCard;
    MaterialButton confirmRideBtn;
    String selectedRideType = null;

    FirebaseFirestore db;

    // UI references for status
    CardView rideStatusCard;
    LinearLayout bottomSheetLayout, driverInfoLayout;
    TextView statusText, driverText, driverName, driverVehicle, driverRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_dashboard);

        db = FirebaseFirestore.getInstance();

        // View bindings
        pickupInput = findViewById(R.id.pickupInput);
        dropoffInput = findViewById(R.id.dropoffInput);
        carCard = findViewById(R.id.carCard);
        bikeCard = findViewById(R.id.bikeCard);
        rickshawCard = findViewById(R.id.rickshawCard);
        confirmRideBtn = findViewById(R.id.confirmRideBtn);

        // Status + Bottom Sheet
        bottomSheetLayout = findViewById(R.id.bottomSheetLayout);
        rideStatusCard = findViewById(R.id.rideStatusCard);
        statusText = findViewById(R.id.statusText);
        driverText = findViewById(R.id.driverText);
        driverInfoLayout = findViewById(R.id.driverInfoLayout);
        driverName = findViewById(R.id.driverName);
        driverVehicle = findViewById(R.id.driverVehicle);
        driverRating = findViewById(R.id.driverRating);

        setRideTypeListeners();

        confirmRideBtn.setOnClickListener(view -> {
            requestRide();
        });
    }

    private void setRideTypeListeners() {
        carCard.setOnClickListener(v -> selectRide("Car"));
        bikeCard.setOnClickListener(v -> selectRide("Bike"));
        rickshawCard.setOnClickListener(v -> selectRide("Rickshaw"));
    }

    private void selectRide(String type) {
        selectedRideType = type;

        carCard.setCardBackgroundColor(type.equals("Car") ? getColor(R.color.selected_ride_bg) : getColor(R.color.white));
        bikeCard.setCardBackgroundColor(type.equals("Bike") ? getColor(R.color.selected_ride_bg) : getColor(R.color.white));
        rickshawCard.setCardBackgroundColor(type.equals("Rickshaw") ? getColor(R.color.selected_ride_bg) : getColor(R.color.white));
    }

    private void requestRide() {
        String pickup = pickupInput.getText().toString().trim();
        String dropoff = dropoffInput.getText().toString().trim();

        if (pickup.isEmpty() || dropoff.isEmpty() || selectedRideType == null) {
            Toast.makeText(this, "Please enter all fields and select a ride type", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> rideData = new HashMap<>();
        rideData.put("pickup", pickup);
        rideData.put("dropoff", dropoff);
        rideData.put("rideType", selectedRideType);
        rideData.put("status", "requested");
        rideData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("rides")
                .add(rideData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Ride requested successfully!", Toast.LENGTH_SHORT).show();
                    showRideStatusUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error requesting ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRideStatusUI() {
        // Hide bottom sheet
        bottomSheetLayout.setVisibility(View.GONE);

        // Show ride status card
        rideStatusCard.setVisibility(View.VISIBLE);
        statusText.setText("Status: Requested");
        driverText.setText("Waiting for driver assignment...");

        // Hide driver info until driver is assigned
        driverInfoLayout.setVisibility(View.GONE);

        // Simulate driver assigned (you will replace this with Firestore listener)
        rideStatusCard.postDelayed(() -> {
            statusText.setText("Status: Driver Assigned");
            driverText.setVisibility(View.GONE);
            driverInfoLayout.setVisibility(View.VISIBLE);

            driverName.setText("Ali Raza");
            driverVehicle.setText("Suzuki Alto – LEX-987");
            driverRating.setText("★ 4.9");
        }, 3000); // Replace with real driver assignment later
    }
}
