package com.stationflow.backend.repository;

import com.stationflow.backend.model.StationUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationUpdateRequestRepository extends JpaRepository<StationUpdateRequest, String> {
    List<StationUpdateRequest> findByStatus(StationUpdateRequest.RequestStatus status);
    List<StationUpdateRequest> findByStationIdAndStatus(String stationId, StationUpdateRequest.RequestStatus status);
    long countByUserIdAndCreatedAtAfter(String userId, java.time.LocalDateTime date);
    List<StationUpdateRequest> findByUserIdOrderByCreatedAtDesc(String userId);
}
