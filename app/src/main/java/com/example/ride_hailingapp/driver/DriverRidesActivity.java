package com.example.ride_hailingapp.driver;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ride_hailingapp.R;
import com.example.ride_hailingapp.RoleSelectActivity;
import com.example.ride_hailingapp.rider.RideHistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import android.view.View;

public class DriverRidesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RideRequestAdapter adapter;
    List<RideRequest> rideList = new ArrayList<>();

    FirebaseFirestore db;
    FirebaseAuth auth;
    String driverVehicleType;
    String driverId;
    ImageView logoutIcon;
    private ListenerRegistration rideListener;
    TextView noRidesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_rides);

        ImageView logoutIcon = findViewById(R.id.logoutIcon);
        ImageView viewHistory = findViewById(R.id.viewHistory);
        viewHistory.setOnClickListener(v -> showDriverHistory());


        logoutIcon.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(DriverRidesActivity.this, RoleSelectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);
        });


        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        driverId = auth.getCurrentUser().getUid();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.rideRequestsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideRequestAdapter(this, rideList, driverId);
        recyclerView.setAdapter(adapter);

        fetchDriverVehicleType();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void showDriverHistory() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialogue_ride_history_modal);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        RecyclerView historyRecycler = dialog.findViewById(R.id.historyRecycler);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));

        List<RideRequest> historyList = new ArrayList<>();
        RideHistoryAdapter adapter = new RideHistoryAdapter(this, historyList);
        historyRecycler.setAdapter(adapter);

        db.collection("rides")
                .whereEqualTo("driverId", driverId)  // driverId from FirebaseAuth
                .whereEqualTo("status", "completed") // only completed rides
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        RideRequest ride = doc.toObject(RideRequest.class);
                        historyList.add(ride);
                    }
                    adapter.notifyDataSetChanged();
                });

        dialog.show();
    }


    private void fetchDriverVehicleType() {
        db.collection("drivers")
                .document(driverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        driverVehicleType = documentSnapshot.getString("vehicleType");
                        fetchMatchingRides();
                    } else {
                        Toast.makeText(this, "Driver info not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load vehicle type.", Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchMatchingRides() {
        noRidesText = findViewById(R.id.noRidesText);
        if (rideListener != null) {
            rideListener.remove(); // Prevent multiple listeners
        }

        rideListener = db.collection("rides")
                .whereEqualTo("rideType", driverVehicleType)
                .whereEqualTo("status", "requested")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) {
                        Toast.makeText(this, "Error listening for ride requests.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    rideList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> rejectedBy = (List<String>) doc.get("rejectedBy");

                        if (rejectedBy == null || !rejectedBy.contains(driverId)) {
                            RideRequest ride = doc.toObject(RideRequest.class);
                            if (ride != null) {
                                ride.setId(doc.getId());
                                rideList.add(ride);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (rideList.isEmpty()) {
                        noRidesText.setVisibility(View.VISIBLE);
                    } else {
                        noRidesText.setVisibility(View.GONE);
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rideListener != null) {
            rideListener.remove();
        }
    }

}
