package com.example.ride_hailingapp.driver;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ride_hailingapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RideRequestAdapter extends RecyclerView.Adapter<RideRequestAdapter.RideViewHolder> {
    private Context context;
    private List<RideRequest> rides;
    private String driverId;

    public RideRequestAdapter(Context context, List<RideRequest> rides, String driverId) {
        this.context = context;
        this.rides = rides;
        this.driverId = driverId;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ride_request, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        RideRequest ride = rides.get(position);
        holder.pickupText.setText("Pickup: " + ride.getPickup());
        holder.dropoffText.setText("Dropoff: " + ride.getDropoff());

        holder.acceptBtn.setOnClickListener(v -> showSimulationBottomSheet(ride));
        holder.rejectBtn.setOnClickListener(v -> rejectRide(ride, position));
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    private void rejectRide(RideRequest ride, int position) {
        FirebaseFirestore.getInstance()
                .collection("rides")
                .document(ride.getId())
                .update("rejectedBy", FieldValue.arrayUnion(driverId),
                        "status", "rejected")
                .addOnSuccessListener(unused -> {
                    // Get the latest index of this ride in the list
                    int index = rides.indexOf(ride);
                    if (index != -1) {
                        rides.remove(index);
                        notifyItemRemoved(index);
                    } else {
                        // As fallback, do full refresh
                        notifyDataSetChanged();
                    }
                });
    }


    private void showSimulationBottomSheet(RideRequest ride) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_ride_status, null);
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false); // Prevent dismiss on touch outside

        TextView pickup = view.findViewById(R.id.pickupLocation);
        TextView dropoff = view.findViewById(R.id.dropoffLocation);
        TextView stepRequested = view.findViewById(R.id.stepRequested);
        TextView stepAccepted = view.findViewById(R.id.stepAccepted);
        TextView stepInProgress = view.findViewById(R.id.stepInProgress);
        TextView stepCompleted = view.findViewById(R.id.stepCompleted);
        MaterialButton actionButton = view.findViewById(R.id.statusActionButton);

        pickup.setText("Pickup: " + ride.getPickup());
        dropoff.setText("Dropoff: " + ride.getDropoff());

        String[] statusSteps = {"accepted", "in Progress", "completed"};
        TextView[] stepViews = {stepAccepted, stepInProgress, stepCompleted};
        final int[] currentStep = {0};

        highlightStep(stepRequested);

        // Capture the BottomSheetBehavior to control drag state
        final BottomSheetBehavior<View>[] behavior = new BottomSheetBehavior[1];
        dialog.setOnShowListener(dlg -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                behavior[0] = BottomSheetBehavior.from(bottomSheet);
                behavior[0].setDraggable(true);  // Initially allow dragging
            }
        });

        actionButton.setOnClickListener(v -> {
            if (currentStep[0] < statusSteps.length) {
                String newStatus = statusSteps[currentStep[0]];

                FirebaseFirestore.getInstance()
                        .collection("rides")
                        .document(ride.getId())
                        .update("status", newStatus)
                        .addOnSuccessListener(unused -> {
                            highlightStep(stepViews[currentStep[0]]);
                            currentStep[0]++;

                            if (currentStep[0] == 1) {
                                actionButton.setText("Start Ride");

                                // Lock the bottom sheet
                                if (behavior[0] != null) {
                                    behavior[0].setDraggable(false);
                                }
                                dialog.setCancelable(false);

                                // Add driverId once ride is accepted
                                FirebaseFirestore.getInstance()
                                        .collection("rides")
                                        .document(ride.getId())
                                        .update("driverId", driverId)
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Failed to set driver ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                            else if (currentStep[0] == 2) {
                                actionButton.setText("Complete Ride");
                            }
                            else {
                                // Ride Completed: dismiss with slide-down animation
                                if (behavior[0] != null) {
                                    behavior[0].setDraggable(true);
                                }

                                dialog.dismiss();

                                int index = rides.indexOf(ride);
                                if (index != -1) {
                                    rides.remove(index);
                                    notifyItemRemoved(index);
                                }
                            }
                        });
            }
        });

        dialog.show();
    }


    private void highlightStep(TextView stepView) {
        stepView.setTextColor(ContextCompat.getColor(context, R.color.primary));
        stepView.setTypeface(null, Typeface.BOLD);
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView pickupText, dropoffText;
        Button acceptBtn, rejectBtn;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupText = itemView.findViewById(R.id.pickupText);
            dropoffText = itemView.findViewById(R.id.dropoffText);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
        }
    }
}

