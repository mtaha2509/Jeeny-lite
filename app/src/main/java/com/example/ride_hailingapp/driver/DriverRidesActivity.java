package com.example.ride_hailingapp.driver;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ride_hailingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class DriverRidesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RideRequestAdapter adapter;
    List<RideRequest> rideList = new ArrayList<>();

    FirebaseFirestore db;
    FirebaseAuth auth;
    String driverVehicleType;
    String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_rides);

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
        db.collection("rides")
                .whereEqualTo("rideType", driverVehicleType)
                .whereEqualTo("status", "requested")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    rideList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Filter out rides already rejected by this driver
                        List<String> rejectedBy = (List<String>) doc.get("rejectedBy");
                        if (rejectedBy == null || !rejectedBy.contains(driverId)) {
                            RideRequest ride = doc.toObject(RideRequest.class);
                            ride.setId(doc.getId());
                            rideList.add(ride);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching rides.", Toast.LENGTH_SHORT).show()
                );
    }
}
