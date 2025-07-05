package com.example.ride_hailingapp.driver;

public class RideRequest {
    private String id;
    private String pickup;
    private String dropoff;
    private String rideType;
    private String status;

    public RideRequest() {}  // Needed for Firebase

    public RideRequest(String id, String pickup, String dropoff, String rideType, String status) {
        this.id = id;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.rideType = rideType;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getPickup() { return pickup; }
    public String getDropoff() { return dropoff; }
    public String getRideType() { return rideType; }
    public String getStatus() { return status; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setPickup(String pickup) { this.pickup = pickup; }
    public void setDropoff(String dropoff) { this.dropoff = dropoff; }
    public void setRideType(String rideType) { this.rideType = rideType; }
    public void setStatus(String status) { this.status = status; }
}
