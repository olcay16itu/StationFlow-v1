package com.stationflow.backend.repository;

import com.stationflow.backend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime date);
}
