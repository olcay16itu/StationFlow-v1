package com.stationflow.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "station_update_requests")
public class StationUpdateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    private String stationId;

    @NotNull
    private String userId;

    @Min(value = 0, message = "Mevcut sayı 0'dan küçük olamaz")
    private int requestedAvailable;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;

    public StationUpdateRequest() {
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    public StationUpdateRequest(String stationId, String userId, int requestedAvailable) {
        this.stationId = stationId;
        this.userId = userId;
        this.requestedAvailable = requestedAvailable;
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getRequestedAvailable() { return requestedAvailable; }
    public void setRequestedAvailable(int requestedAvailable) { this.requestedAvailable = requestedAvailable; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
