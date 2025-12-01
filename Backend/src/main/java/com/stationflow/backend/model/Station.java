package com.stationflow.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stations")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @jakarta.validation.constraints.NotBlank(message = "İstasyon adı boş olamaz")
    private String name;

    @jakarta.validation.constraints.NotNull(message = "Ulaşım türü seçilmelidir")
    @Enumerated(EnumType.STRING)
    private TransportType type;

    @jakarta.validation.constraints.NotNull(message = "Konum bilgisi zorunludur")
    @Embedded
    private Location location;

    @jakarta.validation.constraints.Min(value = 0, message = "Kapasite 0'dan küçük olamaz")
    private int capacity;
    
    @jakarta.validation.constraints.Min(value = 0, message = "Mevcut sayı 0'dan küçük olamaz")
    private int available;

    @jakarta.validation.constraints.NotNull(message = "Durum bilgisi zorunludur")
    @Enumerated(EnumType.STRING)
    private StationStatus status;

    private LocalDateTime lastUpdate;
    
    private boolean isCustom;
    private String ownerId;
    
    @PrePersist
    protected void onCreate() {
        lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdate = LocalDateTime.now();
    }

    public Station() {
    }

    public Station(String id, String name, TransportType type, Location location, int capacity, int available, StationStatus status, LocalDateTime lastUpdate, boolean isCustom, String ownerId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.capacity = capacity;
        this.available = available;
        this.status = status;
        this.lastUpdate = lastUpdate;
        this.isCustom = isCustom;
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransportType getType() {
        return type;
    }

    public void setType(TransportType type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
