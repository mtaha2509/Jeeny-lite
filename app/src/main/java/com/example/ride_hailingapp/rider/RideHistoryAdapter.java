package com.example.ride_hailingapp.rider;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ride_hailingapp.R;
import com.example.ride_hailingapp.driver.RideRequest;


import java.util.List;


public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<RideRequest> completedRides;

    public RideHistoryAdapter(Context context, List<RideRequest> completedRides) {
        this.context = context;
        this.completedRides = completedRides;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ride_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        RideRequest ride = completedRides.get(position);
        holder.pickupText.setText("Pickup: " + ride.getPickup());
        holder.dropoffText.setText("Dropoff: " + ride.getDropoff());
        holder.rideTypeText.setText("Ride Type: " + ride.getRideType());
    }

    @Override
    public int getItemCount() {
        return completedRides.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView pickupText, dropoffText, rideTypeText;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupText = itemView.findViewById(R.id.pickupText);
            dropoffText = itemView.findViewById(R.id.dropoffText);
            rideTypeText = itemView.findViewById(R.id.rideTypeText);
        }
    }
}
