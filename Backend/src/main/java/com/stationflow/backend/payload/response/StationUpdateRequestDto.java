package com.stationflow.backend.payload.response;

import com.stationflow.backend.model.StationUpdateRequest;
import java.time.LocalDateTime;

public class StationUpdateRequestDto {
    private String id;
    private String stationId;
    private String stationName;
    private String userId;
    private String username;
    private int requestedAvailable;
    private StationUpdateRequest.RequestStatus status;
    private LocalDateTime createdAt;

    public StationUpdateRequestDto(StationUpdateRequest request, String stationName, String username) {
        this.id = request.getId();
        this.stationId = request.getStationId();
        this.stationName = stationName;
        this.userId = request.getUserId();
        this.username = username;
        this.requestedAvailable = request.getRequestedAvailable();
        this.status = request.getStatus();
        this.createdAt = request.getCreatedAt();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRequestedAvailable() { return requestedAvailable; }
    public void setRequestedAvailable(int requestedAvailable) { this.requestedAvailable = requestedAvailable; }

    public StationUpdateRequest.RequestStatus getStatus() { return status; }
    public void setStatus(StationUpdateRequest.RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
